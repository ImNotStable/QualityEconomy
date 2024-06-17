package com.imnotstable.qualityeconomy.storage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.economy.Account;
import com.imnotstable.qualityeconomy.economy.BalanceEntry;
import com.imnotstable.qualityeconomy.storage.importdata.ImportDataManager;
import com.imnotstable.qualityeconomy.storage.storageformats.StorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.sql.SQLDriver;
import com.imnotstable.qualityeconomy.storage.storageformats.sql.SQLStorageType;
import com.imnotstable.qualityeconomy.util.FileUtils;
import com.imnotstable.qualityeconomy.util.Misc;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import com.imnotstable.qualityeconomy.util.debug.Timer;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
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
          boolean payable = user.getBoolean("accepting-pay", true);
          accounts.add(new Account(uuid).setUsername(username).initializeBalanceEntry(new BalanceEntry("default", balance, payable)));
        }
        StorageManager.getActiveStorageType().wipeDatabase();
        StorageManager.getActiveStorageType().createAccounts(accounts);
        AccountManager.setupAccounts();
        return true;
      });
    }
    return CompletableFuture.supplyAsync(() -> ImportDataManager.importData(new File("plugins/QualityEconomy/" + information)));
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
        
        currencyJSON.add("ADMIN-COMMAND", currency.getAdminCommand().toJson());
        currencyJSON.add("VIEW-COMMAND", currency.getViewCommand().toJson());
        currencyJSON.add("TRANSFER-COMMAND", currency.getTransferCommand().toJson());
        currencyJSON.add("LEADERBOARD-COMMAND", currency.getLeaderboardCommand().toJson());
        
        currencyJSON.addProperty("LEADERBOARD-REFRESH-INTERVAL", currency.getLeaderboardRefreshInterval());
        currencyJSON.addProperty("STARTING-BALANCE", currency.getStartingBalance());
        currencyJSON.addProperty("SINGULAR-NAME", currency.getSingular());
        currencyJSON.addProperty("PLURAL-NAME", currency.getPlural());
        currencyJSON.addProperty("FORMAT-TYPE", currency.getFormatType().toString());
        currencyJSON.addProperty("DECIMAL-PLACES", currency.getDecimalPlaces());
        currencyJSON.addProperty("SYMBOL", currency.getSymbol());
        currencyJSON.addProperty("SYMBOL-POSITION", currency.getSymbolPosition() == 1 ? "after" : "before");
        currencyJSON.addProperty("CUSTOM-EVENTS", currency.isCustomEvents());
        currencyJSON.addProperty("TRANSACTION-LOGGING", currency.isTransactionLogging());
        JsonObject messagesJSON = new JsonObject();
        currency.getMessages().forEach((type, message) -> messagesJSON.addProperty(type.getKey(), message));
        currencyJSON.add("MESSAGES", messagesJSON);
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
      return FileUtils.compress(dataFile);
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
