package com.imnotstable.qualityeconomy.storage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.economy.Account;
import com.imnotstable.qualityeconomy.economy.BalanceEntry;
import com.imnotstable.qualityeconomy.storage.storageformats.SQLStorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.StorageType;
import com.imnotstable.qualityeconomy.util.Misc;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import com.imnotstable.qualityeconomy.util.debug.Timer;
import com.imnotstable.qualityeconomy.util.storage.SQLDriver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StorageManager implements Listener {
  
  @Getter
  private static StorageType activeStorageType;
  private static Integer backupSchedulerID = null;
  private static Integer accountSchedulerID = null;
  
  public static void initStorageProcesses(QualityEconomy plugin) {
    if (activeStorageType != null)
      return;
    Timer timer = new Timer("initStorageProcesses()");
    switch (QualityEconomy.getQualityConfig().STORAGE_TYPE) {
      case "h2" -> activeStorageType = new SQLStorageType(SQLDriver.H2);
      case "sqlite" -> activeStorageType = new SQLStorageType(SQLDriver.SQLITE);
      case "mysql" -> activeStorageType = new SQLStorageType(SQLDriver.MYSQL);
      case "mariadb" -> activeStorageType = new SQLStorageType(SQLDriver.MARIADB);
      default -> {
        Logger.logError("Unexpected Storage Type: " + QualityEconomy.getQualityConfig().STORAGE_TYPE, "Defaulting to H2");
        activeStorageType = new SQLStorageType(SQLDriver.H2);
      }
    }
    if (!activeStorageType.initStorageProcesses()) {
      Logger.logError("Failed to initiate storage processes");
      timer.interrupt();
      return;
    }
    AccountManager.setupAccounts();
    long interval = QualityEconomy.getQualityConfig().AUTO_SAVE_ACCOUNTS_INTERVAL;
    if (interval > 0)
      accountSchedulerID = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
        AccountManager::saveAllAccounts, interval, interval).getTaskId();
    interval = QualityEconomy.getQualityConfig().BACKUP_INTERVAL;
    if (interval > 0)
      backupSchedulerID = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
        () -> exportData(ExportType.BACKUP), interval, interval).getTaskId();
    timer.end();
  }
  
  public static void endStorageProcesses() {
    if (activeStorageType == null)
      return;
    Timer timer = new Timer("endStorageProcesses()");
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
  
  public static CompletableFuture<Boolean> importData(String information) {
    if (information.equalsIgnoreCase("essentials")) {
      return CompletableFuture.supplyAsync(() -> {
        
        Collection<Account> accounts = new ArrayList<>();
        File[] userdata = new File("plugins/Essentials/userdata").listFiles((dir, name) -> Misc.isUUID(name.split("\\.")[0]).isPresent());
        if (userdata == null || userdata.length == 0)
          return false;
        for (File userfile : userdata) {
          YamlConfiguration user = YamlConfiguration.loadConfiguration(userfile);
          UUID uuid = UUID.fromString(userfile.getName().split("\\.")[0]);
          String username = user.getString("last-account-name", uuid.toString());
          double balance = Double.parseDouble(user.getString("money", "0"));
          accounts.add(new Account(uuid).setUsername(username).setDefaultBalance(balance));
        }
        StorageManager.getActiveStorageType().wipeDatabase();
        StorageManager.getActiveStorageType().createAccounts(accounts);
        AccountManager.setupAccounts();
        return true;
      });
    }
    return CompletableFuture.supplyAsync(() -> {
      JsonObject data;
      try {
        String rawJSON = new String(Files.readAllBytes(Path.of("plugins/QualityEconomy/" + information)));
        data = new Gson().fromJson(rawJSON, JsonObject.class);
      } catch (IOException exception) {
        Logger.logError("Error while importing playerdata", exception);
        return false;
      } catch (InvalidPathException exception) {
        Logger.logError("Invalid Path found", exception);
        return false;
      }
      if (!data.has("VERSION"))
        return legacyImportData(data);
      if (data.get("VERSION").getAsString().equalsIgnoreCase("1.5.0"))
        return importDataV1_5(data);
      return true;
    });
  }
  
  private static boolean legacyImportData(JsonObject rootJSON) {
    Timer timer = new Timer("importDatabase()");
    AccountManager.clearAccounts();
    getActiveStorageType().wipeDatabase();
    Collection<Account> accounts = new ArrayList<>();
    List<String> currencies = new ArrayList<>();
    if (rootJSON.has("CUSTOM-CURRENCIES")) {
      JsonArray currenciesJSON = rootJSON.getAsJsonArray("CUSTOM-CURRENCIES");
      YamlConfiguration currenciesYAML = YamlConfiguration.loadConfiguration(QualityEconomy.getCurrencyConfig().getFile());
      for (JsonElement currencyJSON : currenciesJSON) {
        String currency = currencyJSON.getAsString();
        currencies.add(currency);
        currenciesYAML.set("currencies.admin-commands", List.of());
        currenciesYAML.set("currencies.view-commands", List.of());
        currenciesYAML.set("currencies.transfer-commands", List.of());
        currenciesYAML.set("currencies.leaderboard-commands", List.of());
        currenciesYAML.set("currencies.singular-name", currency);
        currenciesYAML.set("currencies.plural-name", currency + "s");
        currenciesYAML.set("currencies.symbol", currency.toCharArray()[0]);
        currenciesYAML.set("currencies.symbol-position", "before");
        currenciesYAML.set("currencies.decimal-places", 2);
        currenciesYAML.set("currencies.starting-balance", 0.0);
      }
      rootJSON.remove("CUSTOM-CURRENCIES");
    }
    rootJSON.entrySet().forEach(entry -> {
      JsonObject accountJSON = entry.getValue().getAsJsonObject();
      UUID uuid = UUID.fromString(entry.getKey());
      String name = accountJSON.get("NAME").getAsString();
      Collection<BalanceEntry> balances = new ArrayList<>();
      balances.add(new BalanceEntry("default", accountJSON.get("BALANCE").getAsDouble(), accountJSON.get("PAYABLE").getAsBoolean()));
      accountJSON.entrySet().forEach(rawEntry -> {
        if (currencies.contains(rawEntry.getKey()))
          balances.add(new BalanceEntry(rawEntry.getKey(), rawEntry.getValue().getAsDouble(), true));
      });
      accounts.add(new Account(uuid).setUsername(name).updateBalanceEntries(balances));
    });
    StorageManager.getActiveStorageType().wipeDatabase();
    getActiveStorageType().createAccounts(accounts);
    AccountManager.setupAccounts();
    timer.end();
    return true;
  }
  
  private static boolean importDataV1_5(JsonObject rootJSON) {
    // Currency Import
    JsonObject currenciesJSON = rootJSON.getAsJsonObject("CURRENCIES");
    YamlConfiguration currenciesYAML = YamlConfiguration.loadConfiguration(QualityEconomy.getCurrencyConfig().getFile());
    currenciesJSON.entrySet().forEach(currencyJSON -> {
      String currency = currencyJSON.getKey();
      JsonObject currencyData = currenciesJSON.getAsJsonObject(currency);
      for (String commandKey : new String[]{"admin-commands", "view-commands", "transfer-commands", "leaderboard-commands"}) {
        List<String> commands = new ArrayList<>();
        currencyData.getAsJsonArray(commandKey.toUpperCase()).forEach(command -> commands.add(command.getAsString()));
        currenciesYAML.set("currencies." + currency + "." + commandKey, commands);
      }
      currenciesYAML.set("currencies." + currency + ".singular-name", currencyData.get("SINGULAR").getAsString());
      currenciesYAML.set("currencies." + currency + ".plural-name", currencyData.get("PLURAL").getAsString());
      currenciesYAML.set("currencies." + currency + ".symbol", currencyData.get("SYMBOL").getAsString());
      currenciesYAML.set("currencies." + currency + ".symbol-position", currencyData.get("SYMBOL-POSITION").getAsString());
      currenciesYAML.set("currencies." + currency + ".decimal-places", currencyData.get("DECIMAL-PLACES").getAsInt());
      currenciesYAML.set("currencies." + currency + ".starting-balance", currencyData.get("DEFAULT-BALANCE").getAsDouble());
    });
    try {
      currenciesYAML.save(QualityEconomy.getCurrencyConfig().getFile());
    } catch (IOException exception) {
      Logger.logError("Failed to save currency config while importing", exception);
      return false;
    }
    // Account Import
    JsonObject accountsJSON = rootJSON.getAsJsonObject("ACCOUNTS");
    Collection<Account> accounts = new ArrayList<>();
    accountsJSON.entrySet().forEach(accountJSON -> {
      Account account = new Account(UUID.fromString(accountJSON.getKey()));
      JsonObject accountData = accountJSON.getValue().getAsJsonObject();
      account.setUsername(accountData.get("USERNAME").getAsString());
      Collection<BalanceEntry> balances = new ArrayList<>();
      accountData.getAsJsonObject("BALANCES").entrySet().forEach(balanceJSON -> {
        JsonObject balanceData = balanceJSON.getValue().getAsJsonObject();
        balances.add(new BalanceEntry(balanceJSON.getKey(), balanceData.get("BALANCE").getAsDouble(), balanceData.get("PAYABLE").getAsBoolean()));
      });
      accounts.add(account.updateBalanceEntries(balances));
    });
    StorageManager.getActiveStorageType().wipeDatabase();
    getActiveStorageType().createAccounts(accounts);
    AccountManager.setupAccounts();
    return true;
  }
  
  public static CompletableFuture<String> exportData(@NotNull ExportType exportType) {
    return CompletableFuture.supplyAsync(() -> {
      File exportFolder = new File(exportType.getPath());
      File dataFile = new File(exportFolder, "QualityEconomy " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH-mm")) + ".json");
      try {
        if (!exportFolder.exists() && !exportFolder.mkdirs()) {
          Logger.logError("Failed to create export folder: " + exportFolder.getName());
          return null;
        }
        if (!dataFile.exists() && !dataFile.createNewFile()) {
          Logger.logError("Failed to create export file: " + dataFile.getName());
          return null;
        }
      } catch (IOException exception) {
        Logger.logError("Failed to create export file: " + dataFile.getName(), exception);
        return null;
      }
      JsonObject rootJSON = new JsonObject();
      rootJSON.addProperty("VERSION", QualityEconomy.getInstance().getPluginMeta().getVersion());
      // Currency Export
      JsonObject currenciesJSON = new JsonObject();
      QualityEconomy.getCurrencyConfig().getCurrencies().forEach(currency -> {
        JsonObject currencyJSON = new JsonObject();
        currencyJSON.addProperty("DEFAULT-BALANCE", currency.getDefaultBalance());
        currencyJSON.addProperty("DECIMAL-PLACES", currency.getDecimalPlaces());
        JsonArray viewCommands = new JsonArray();
        viewCommands.add(currency.getViewCommand());
        for (String command : currency.getViewAliases())
          viewCommands.add(command);
        currencyJSON.add("VIEW-COMMANDS", viewCommands);
        JsonArray adminCommands = new JsonArray();
        adminCommands.add(currency.getAdminCommand());
        for (String command : currency.getAdminAliases())
          adminCommands.add(command);
        currencyJSON.add("ADMIN-COMMANDS", adminCommands);
        JsonArray transferCommands = new JsonArray();
        transferCommands.add(currency.getTransferCommand());
        for (String command : currency.getTransferAliases())
          transferCommands.add(command);
        currencyJSON.add("TRANSFER-COMMANDS", transferCommands);
        JsonArray leaderboardCommands = new JsonArray();
        leaderboardCommands.add(currency.getLeaderboardCommand());
        for (String command : currency.getLeaderboardAliases())
          leaderboardCommands.add(command);
        currencyJSON.add("LEADERBOARD-COMMANDS", leaderboardCommands);
        currencyJSON.addProperty("SYMBOL", currency.getSymbol());
        currencyJSON.addProperty("SYMBOL-POSITION", currency.getSymbolPosition() == 1 ? "after" : "before");
        currencyJSON.addProperty("SINGULAR", currency.getSingular());
        currencyJSON.addProperty("PLURAL", currency.getPlural());
        JsonObject messagesJSON = new JsonObject();
        currency.getMessages().forEach((type, message) -> messagesJSON.addProperty(type.getKey(), message));
        currenciesJSON.add(currency.getName(), currencyJSON);
      });
      rootJSON.add("CURRENCIES", currenciesJSON);
      // Account Export
      JsonObject accountsJSON = new JsonObject();
      AccountManager.saveAllAccounts();
      activeStorageType.getAllAccounts().values().forEach(account -> {
        JsonObject accountJSON = new JsonObject();
        accountJSON.addProperty("USERNAME", account.getUsername());
        JsonObject balancesJSON = new JsonObject();
        account.getBalanceEntries().forEach(entry -> {
          JsonObject balanceJSON = new JsonObject();
          balanceJSON.addProperty("BALANCE", entry.getBalance());
          balanceJSON.addProperty("PAYABLE", entry.isPayable());
          balancesJSON.add(entry.getCurrency(), balanceJSON);
        });
        accountJSON.add("BALANCES", balancesJSON);
        accountsJSON.add(account.getUniqueId().toString(), accountJSON);
      });
      rootJSON.add("ACCOUNTS", accountsJSON);
      try (FileWriter writer = new FileWriter(dataFile)) {
        writer.write(new Gson().toJson(rootJSON));
      } catch (IOException exception) {
        Logger.logError("Error while exporting database", exception);
        return null;
      }
      return dataFile.getName();
    });
  }
  
  @EventHandler
  public void on(AsyncPlayerPreLoginEvent event) {
    Timer timer = new Timer("onAsyncPlayerPreLoginEvent()");
    AccountManager.getAccount(event.getUniqueId()).setUsername(event.getPlayerProfile().getName());
    timer.end();
  }
  
  @AllArgsConstructor
  @Getter
  public enum ExportType {
    BACKUP("plugins/QualityEconomy/backups/"),
    NORMAL("plugins/QualityEconomy/exports/");
    
    private final String path;
    
  }
  
}
