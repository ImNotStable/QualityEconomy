package com.imnotstable.qualityeconomy.storage;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.storage.storageformats.SQLStorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.StorageType;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import com.imnotstable.qualityeconomy.util.debug.Timer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

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
      case "h2" -> activeStorageType = new SQLStorageType(1);
      case "sqlite" -> activeStorageType = new SQLStorageType(2);
      case "mysql" -> activeStorageType = new SQLStorageType(3);
      case "mariadb" -> activeStorageType = new SQLStorageType(4);
      case "postgresql" -> activeStorageType = new SQLStorageType(5);
      default -> {
        Logger.logError("Unexpected Storage Type: " + QualityEconomy.getQualityConfig().STORAGE_TYPE, "Defaulting to H2");
        activeStorageType = new SQLStorageType(1);
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
        () -> exportDatabase("plugins/QualityEconomy/backups/"), interval, interval).getTaskId();
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
  
  public static CompletableFuture<Boolean> importDatabase(String fileName) {
    return new CompletableFuture<>();
  }
  
  public static void exportDatabase(final String path) {
  }
  
  @EventHandler
  public void on(AsyncPlayerPreLoginEvent event) {
    Timer timer = new Timer("onAsyncPlayerPreLoginEvent()");
    AccountManager.getAccount(event.getUniqueId()).setUsername(event.getPlayerProfile().getName());
    timer.end();
  }
  
}
