package com.imnotstable.qualityeconomy.config;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Debug;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class Config extends BaseConfig {
  
  public String STORAGE_TYPE;
  public int DECIMAL_PLACES;
  public boolean BANKNOTES;
  public boolean CUSTOM_EVENTS;
  public boolean TRANSACTION_LOGGING;
  public boolean COMMANDS_BALANCE;
  public boolean COMMANDS_BALANCETOP;
  public boolean COMMANDS_ECONOMY;
  public boolean COMMANDS_PAY;
  public boolean COMMANDS_REQUEST;
  public boolean COMMANDS_CUSTOMBALANCE;
  public boolean COMMANDS_CUSTOMECONOMY;
  public boolean CUSTOM_CURRENCIES;
  public long BACKUP_INTERVAL;
  public long BALANCETOP_INTERVAL;
  public long AUTO_SAVE_ACCOUNTS_INTERVAL;
  public Map<String, String> DATABASE_INFORMATION = new HashMap<>();
  public Map<String, Object> DATABASE_INFORMATION_ADVANCED_SETTINGS;
  public boolean UPDATE_NOTIFICATIONS;
  
  public Config(QualityEconomy plugin) {
    super(plugin, "config.yml");
    load();
  }
  
  public void load() {
    YamlConfiguration config = makeSafe();
    STORAGE_TYPE = config.getString("storage-type", "sqlite").toLowerCase();
    DECIMAL_PLACES = Math.max(config.getInt("decimal-places", 4), 0);
    CUSTOM_EVENTS = config.getBoolean("custom-events", false);
    TRANSACTION_LOGGING = config.getBoolean("transaction-logging", false);
    BANKNOTES = config.getBoolean("banknotes", false);
    COMMANDS_BALANCE = config.getBoolean("commands.balance", true);
    COMMANDS_BALANCETOP = config.getBoolean("commands.balancetop", true);
    COMMANDS_ECONOMY = config.getBoolean("commands.economy", true);
    COMMANDS_PAY = config.getBoolean("commands.pay", true);
    COMMANDS_REQUEST = config.getBoolean("commands.request", true);
    COMMANDS_CUSTOMBALANCE = config.getBoolean("commands.custombalance", true);
    COMMANDS_CUSTOMECONOMY = config.getBoolean("commands.customeconomy", true);
    CUSTOM_CURRENCIES = config.getBoolean("custom-currencies", false);
    BACKUP_INTERVAL = config.getLong("backup-interval", 21600) * 20;
    BALANCETOP_INTERVAL = config.getLong("balancetop-interval", 5) * 20;
    AUTO_SAVE_ACCOUNTS_INTERVAL = config.getLong("auto-save-accounts-interval", 60) * 20;
    config.getConfigurationSection("database-information").getValues(false).forEach((key, value) -> DATABASE_INFORMATION.put(key, value.toString()));
    DATABASE_INFORMATION_ADVANCED_SETTINGS = config.getConfigurationSection("database-information.advanced-settings").getValues(false);
    UPDATE_NOTIFICATIONS = config.getBoolean("update-notifications", true);
  }
  
  private YamlConfiguration makeSafe() {
    YamlConfiguration config = super.baseLoad();
    boolean save = false;
    int BACKUP_INTERVAL = config.getInt("backup-interval");
    if (BACKUP_INTERVAL < 1800) {
      config.set("backup-interval", BACKUP_INTERVAL * 3600);
      save = true;
    }
    if (save)
      try {
        config.save(file);
      } catch (IOException exception) {
        new Debug.QualityError("Failed to update config.yml during safety analysis", exception).log();
      }
    return config;
  }
  
}
