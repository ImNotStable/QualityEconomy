package com.imnotstable.qualityeconomy.storage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.commands.CommandManager;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.storage.storageformats.JsonStorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.MongoStorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.SQLStorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.StorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.YamlStorageType;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.Misc;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StorageManager implements Listener {
  
  @Getter
  private static StorageType activeStorageType;
  private static Integer backupSchedulerID = null;
  private static Integer accountSchedulerID = null;
  
  public static void initStorageProcesses() {
    if (activeStorageType != null)
      return;
    Debug.Timer timer = new Debug.Timer("initStorageProcesses()");
    switch (Configuration.getStorageType()) {
      case "h2" -> activeStorageType = new SQLStorageType(1);
      case "sqlite" -> activeStorageType = new SQLStorageType(2);
      case "mysql" -> activeStorageType = new SQLStorageType(3);
      case "mariadb" -> activeStorageType = new SQLStorageType(4);
      case "postgresql" -> activeStorageType = new SQLStorageType(5);
      case "mongodb" -> activeStorageType = new MongoStorageType();
      case "json" -> activeStorageType = new JsonStorageType();
      case "yaml" -> activeStorageType = new YamlStorageType();
      default -> {
        new Debug.QualityError("Unexpected Storage Type: " + Configuration.getStorageType()).log();
        timer.interrupt();
        Bukkit.getPluginManager().disablePlugin(QualityEconomy.getInstance());
        return;
      }
    }
    if (!activeStorageType.initStorageProcesses()) {
      new Debug.QualityError("Failed to initiate storage processes").log();
      timer.interrupt();
      return;
    }
    AccountManager.setupAccounts();
    if (Configuration.getAutoSaveAccountsInterval() > 0)
      accountSchedulerID = Bukkit.getScheduler().runTaskTimerAsynchronously(QualityEconomy.getInstance(),
        AccountManager::saveAllAccounts,
        Configuration.getAutoSaveAccountsInterval(),
        Configuration.getAutoSaveAccountsInterval()
      ).getTaskId();
    if (Configuration.getBackupInterval() > 0)
      backupSchedulerID = Bukkit.getScheduler().runTaskTimerAsynchronously(QualityEconomy.getInstance(),
        () -> exportDatabase("plugins/QualityEconomy/backups/"),
        Configuration.getBackupInterval(),
        Configuration.getBackupInterval()
      ).getTaskId();
    timer.end();
  }
  
  public static void endStorageProcesses() {
    if (activeStorageType == null)
      return;
    Debug.Timer timer = new Debug.Timer("endStorageProcesses()");
    AccountManager.saveAllAccounts();
    
    if (accountSchedulerID != null) {
      Bukkit.getScheduler().cancelTask(accountSchedulerID);
      accountSchedulerID = null;
    }
    if (backupSchedulerID != null) {
      Bukkit.getScheduler().cancelTask(backupSchedulerID);
      backupSchedulerID = null;
    }
    
    activeStorageType.endStorageProcesses();
    activeStorageType = null;
    timer.end();
  }
  
  public static CompletableFuture<Boolean> importDatabase(String fileName) {
    return CompletableFuture.supplyAsync(() -> {
      Debug.Timer timer = new Debug.Timer("importDatabase()");
      AccountManager.clearAccounts();
      getActiveStorageType().wipeDatabase();
      Collection<Account> accounts = new ArrayList<>();
      try {
        String content = new String(Files.readAllBytes(Paths.get(String.format("plugins/QualityEconomy/%s", fileName))));
        JsonObject rootJson = new Gson().fromJson(content, JsonObject.class);
        
        List<String> customCurrencies = new ArrayList<>();
        if (rootJson.has("CUSTOM-CURRENCIES")) {
          JsonArray currenciesJSON = rootJson.getAsJsonArray("CUSTOM-CURRENCIES");
          for (JsonElement currencyJSON : currenciesJSON) {
            String currency = currencyJSON.getAsString();
            customCurrencies.add(currency);
            addCurrency(currency);
          }
          rootJson.remove("CUSTOM-CURRENCIES");
        }
        rootJson.entrySet().stream()
          .filter(entry -> !entry.getKey().equals("CUSTOM-CURRENCIES"))
          .forEach(entry -> {
            JsonObject accountJSON = entry.getValue().getAsJsonObject();
            UUID uuid = UUID.fromString(entry.getKey());
            String name = accountJSON.get("NAME").getAsString();
            double balance = accountJSON.get("BALANCE").getAsDouble();
            boolean payable = accountJSON.get("PAYABLE").getAsBoolean();
            boolean requestable = accountJSON.get("REQUESTABLE").getAsBoolean();
            Map<String, Double> balanceMap = new HashMap<>();
            for (String currency : customCurrencies)
              balanceMap.put(currency, accountJSON.get(currency).getAsDouble());
            accounts.add(new Account(uuid).setUsername(name).setBalance(balance).setPayable(payable).setRequestable(requestable).setCustomBalances(balanceMap));
          });
        getActiveStorageType().createAccounts(accounts);
        AccountManager.setupAccounts();
      } catch (IOException exception) {
        new Debug.QualityError("Error while importing playerdata", exception).log();
        return false;
      }
      timer.end();
      return true;
    });
  }
  
  public static void exportDatabase(final String path) {
    Misc.runAsync(() -> {
      Debug.Timer timer = new Debug.Timer("exportDatabase()");
      AccountManager.saveAllAccounts();
      File dir = new File(path);
      if (!dir.exists() || !dir.isDirectory())
        if (!dir.mkdir()) {
          new Debug.QualityError("Failed to create directory \"" + path + "\"").log();
          return;
        }
      Gson gson = new Gson();
      JsonObject root = new JsonObject();
      if (Configuration.isCustomCurrenciesEnabled())
        root.add("CUSTOM-CURRENCIES", gson.toJsonTree(getActiveStorageType().getCurrencies()));
      getActiveStorageType().getAllAccounts().forEach((uuid, account) -> {
        JsonObject accountJson = new JsonObject();
        accountJson.addProperty("NAME", account.getUsername());
        accountJson.addProperty("BALANCE", account.getBalance());
        accountJson.addProperty("PAYABLE", account.isPayable());
        accountJson.addProperty("REQUESTABLE", account.isRequestable());
        account.getCustomBalances().forEach(accountJson::addProperty);
        root.add(uuid.toString(), accountJson);
      });
      File file = new File(String.format("%sQualityEconomy %s.json", path, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH-mm"))));
      try (FileWriter writer = new FileWriter(file)) {
        writer.write(gson.toJson(root));
      } catch (IOException exception) {
        new Debug.QualityError("Error while exporting database", exception).log();
      }
      timer.end();
    });
  }
  
  public static void addCurrency(String currency) {
    currency = currency.toUpperCase();
    if (!Configuration.isCustomCurrenciesEnabled()) {
      new Debug.QualityError("This feature is disabled within QualityEconomy's configuration").log();
      return;
    }
    if (List.of("UUID", "NAME", "BALANCE", "PAYABLE", "REQUESTABLE").contains(currency)) {
      new Debug.QualityError("Failed to create currency \"" + currency + "\"", "Name cannot be \"UUID\", \"NAME\", \"BALANCE\", \"PAYABLE\", \"REQUESTABLE\"").log();
      return;
    }
    if (getActiveStorageType().getCurrencies().contains(currency)) {
      new Debug.QualityError("Failed to create currency \"" + currency + "\"", "Currency already exists").log();
      return;
    }
    if (getActiveStorageType().addCurrency(currency)) {
      CommandManager.getCommand("custombalance").register();
      CommandManager.getCommand("customeconomy").register();
    }
  }
  
  public static void removeCurrency(String currency) {
    currency = currency.toUpperCase();
    if (!Configuration.isCustomCurrenciesEnabled()) {
      new Debug.QualityError("This feature is disabled within QualityEconomy's configuration").log();
      return;
    }
    if (!getActiveStorageType().getCurrencies().contains(currency)) {
      new Debug.QualityError("Failed to delete currency \"" + currency + "\"", "Currency doesn't exist").log();
      return;
    }
    if (getActiveStorageType().removeCurrency(currency))
      if (getActiveStorageType().getCurrencies().isEmpty()) {
        CommandManager.getCommand("custombalance").unregister();
        CommandManager.getCommand("customeconomy").unregister();
      }
  }
  
  @EventHandler
  public void on(AsyncPlayerPreLoginEvent event) {
    Debug.Timer timer = new Debug.Timer("onAsyncPlayerPreLoginEvent()");
    AccountManager.getAccount(event.getUniqueId()).setUsername(event.getPlayerProfile().getName());
    timer.end();
  }
  
}
