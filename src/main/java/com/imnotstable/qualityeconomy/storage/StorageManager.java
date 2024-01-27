package com.imnotstable.qualityeconomy.storage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.storage.storageformats.JsonStorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.MongoStorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.SQLStorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.StorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.YamlStorageType;
import com.imnotstable.qualityeconomy.util.Debug;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

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

public class StorageManager implements Listener {
  private static StorageType activeStorageType;
  private static Integer backupSchedulerID = null;
  private static Integer accountSchedulerID = null;
  
  public static StorageType getActiveStorageFormat() {
    return activeStorageType;
  }
  
  public static void initStorageProcesses() {
    if (activeStorageType != null)
      return;
    Debug.Timer timer = new Debug.Timer("initStorageProcesses()");
    switch (Configuration.getStorageType()) {
      case "h2" -> activeStorageType = new SQLStorageType(1);
      case "sqlite" -> activeStorageType = new SQLStorageType(2);
      case "mysql" -> activeStorageType = new SQLStorageType(3);
      case "mariadb" -> activeStorageType = new SQLStorageType(4);
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
        Configuration.getAutoSaveAccountsInterval()).getTaskId();
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
  
  public static void importDatabase(String fileName) {
    new BukkitRunnable() {
      @Override
      public void run() {
        Debug.Timer timer = new Debug.Timer("importDatabase()");
        AccountManager.clearAccounts();
        getActiveStorageFormat().wipeDatabase();
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
              getActiveStorageFormat().addCurrency(currency);
            }
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
              for (String currency : customCurrencies) {
                balanceMap.put(currency, accountJSON.get(currency).getAsDouble());
              }
              accounts.add(new Account(uuid).setUsername(name).setBalance(balance).setPayable(payable).setRequestable(requestable).setCustomBalances(balanceMap));
            });
          getActiveStorageFormat().createAccounts(accounts);
          AccountManager.setupAccounts();
        } catch (IOException exception) {
          new Debug.QualityError("Error while importing playerdata", exception).log();
        }
        timer.end();
      }
    }.runTaskAsynchronously(QualityEconomy.getInstance());
  }
  
  public static void exportDatabase(final String path) {
    new BukkitRunnable() {
      @Override
      public void run() {
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
        root.add("CUSTOM-CURRENCIES", gson.toJsonTree(getActiveStorageFormat().getCurrencies()));
        getActiveStorageFormat().getAllAccounts().forEach((uuid, account) -> {
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
      }
    }.runTaskAsynchronously(QualityEconomy.getInstance());
  }
  
  @EventHandler
  public void on(PlayerJoinEvent event) {
    Debug.Timer timer = new Debug.Timer("onPlayerJoinEvent()");
    AccountManager.createAccount(event.getPlayer().getUniqueId());
    timer.end();
  }
  
}
