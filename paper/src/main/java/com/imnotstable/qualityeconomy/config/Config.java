package com.imnotstable.qualityeconomy.config;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class Config extends BaseConfig {
  
  public String STORAGE_TYPE;
  public boolean CUSTOM_EVENTS;
  public boolean TRANSACTION_LOGGING;
  public long BACKUP_INTERVAL;
  public long LEADERBOARD_RELOAD_INTERVAL;
  public long AUTO_SAVE_ACCOUNTS_INTERVAL;
  public Map<String, String> DATABASE_INFORMATION = new HashMap<>();
  public Map<String, Object> DATABASE_INFORMATION_ADVANCED_SETTINGS;
  public boolean UPDATE_NOTIFICATIONS;
  
  public Config(@NotNull QualityEconomy plugin) {
    super(plugin, "config.yml");
    load();
  }
  
  public void load() {
    super.load(true);
    makeSafe();
    STORAGE_TYPE = config.getString("storage-type", "h2").toLowerCase();
    CUSTOM_EVENTS = config.getBoolean("custom-events", false);
    TRANSACTION_LOGGING = config.getBoolean("transaction-logging", false);
    BACKUP_INTERVAL = config.getLong("backup-interval", 21600) * 20;
    LEADERBOARD_RELOAD_INTERVAL = config.getLong("leaderboard-reload-interval", 5) * 20;
    AUTO_SAVE_ACCOUNTS_INTERVAL = config.getLong("auto-save-accounts-interval", 60) * 20;
    config.getConfigurationSection("database-information").getValues(false).forEach((key, value) -> DATABASE_INFORMATION.put(key, value.toString()));
    DATABASE_INFORMATION_ADVANCED_SETTINGS = config.getConfigurationSection("database-information.advanced-settings").getValues(false);
    UPDATE_NOTIFICATIONS = config.getBoolean("update-notifications", true);
  }
  
  private void makeSafe() {
    boolean save = false;
    int BACKUP_INTERVAL = config.getInt("backup-interval");
    if (BACKUP_INTERVAL < 1800) {
      config.set("backup-interval", BACKUP_INTERVAL * 3600);
      save = true;
    }
    
    if (!save)
      return;
    
    try {
      config.save(getFile());
    } catch (IOException exception) {
      Logger.logError("Failed to update config.yml during safety analysis", exception);
    }
  }
  
}
