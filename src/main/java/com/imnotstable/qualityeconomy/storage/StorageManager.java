package com.imnotstable.qualityeconomy.storage;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.storage.storageformats.JsonStorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.SQLStorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.StorageType;
import com.imnotstable.qualityeconomy.util.QualityError;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class StorageManager implements Listener {
  public static final Object lock = new Object();
  private static StorageType activeStorageType;
  private static int backupSchedulerID = 0;
  
  public static StorageType getActiveStorageFormat() {
    return activeStorageType;
  }
  
  public static void initStorageProcesses() {
    if (activeStorageType != null)
      return;
    synchronized (lock) {
      TestToolkit.Timer timer = new TestToolkit.Timer("Initiating storage processes...");
      switch (Configuration.getStorageType()) {
        case "h2" -> activeStorageType = new SQLStorageType(1);
        case "sqlite" -> activeStorageType = new SQLStorageType(2);
        case "mysql" -> activeStorageType = new SQLStorageType(3);
        case "mariadb" -> activeStorageType = new SQLStorageType(4);
        case "json" -> activeStorageType = new JsonStorageType();
        default -> {
          new QualityError("Unexpected Storage Type: " + Configuration.getStorageType()).log();
          timer.interrupt("Failed to initiate storage processes");
          Bukkit.getPluginManager().disablePlugin(QualityEconomy.getInstance());
          return;
        }
      }
      if (!activeStorageType.initStorageProcesses()) {
        new QualityError("Failed to initiate storage processes").log();
        timer.interrupt("Failed to initiate storage processes");
        return;
      }
      AccountManager.setupAccounts();
      Bukkit.getScheduler().scheduleSyncRepeatingTask(QualityEconomy.getInstance(), AccountManager::saveAllAccounts, 1200, 1200);
      if (Configuration.getBackupInterval() > 0)
        backupSchedulerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
          QualityEconomy.getInstance(),
          () -> DBTransferUtils.exportDatabase("plugins/QualityEconomy/backups/"),
          Configuration.getBackupInterval(),
          Configuration.getBackupInterval()
        );
      timer.end("Initiated storage processes");
    }
  }
  
  public static void endStorageProcesses() {
    if (activeStorageType == null)
      return;
    synchronized (lock) {
      TestToolkit.Timer timer = new TestToolkit.Timer("Terminating storage processes...");
      AccountManager.saveAllAccounts();
      
      if (backupSchedulerID != 0) {
        Bukkit.getScheduler().cancelTask(backupSchedulerID);
        backupSchedulerID = 0;
      }
      
      activeStorageType.endStorageProcesses();
      activeStorageType = null;
      timer.end("Terminated storage processes");
    }
  }
  
  @EventHandler
  public void onPlayerJoinEvent(PlayerJoinEvent event) {
    TestToolkit.Timer timer = new TestToolkit.Timer("Running PlayerJoinEvent...");
    AccountManager.createAccount(event.getPlayer().getUniqueId());
    timer.end("Ran PlayerJoinEvent");
  }
  
}
