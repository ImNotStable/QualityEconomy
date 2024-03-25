package com.imnotstable.qualityeconomy.config;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Debug;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Config {
  
  private final QualityEconomy plugin;
  private final File file;
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
  
  
  public Config(QualityEconomy plugin) {
    this.plugin = plugin;
    this.file = new File(plugin.getDataFolder(), "config.yml");
    load();
  }
  
  public void load() {
    if (!file.exists())
      plugin.saveResource("config.yml", false);
    else
      update();
    makeSafe();
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
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
    BACKUP_INTERVAL = config.getLong("backup-interval", 21600);
    BALANCETOP_INTERVAL = config.getLong("balancetop-interval", 5);
    AUTO_SAVE_ACCOUNTS_INTERVAL = config.getLong("auto-save-accounts-interval", 60);
    config.getConfigurationSection("database-information").getValues(false).forEach((key, value) -> DATABASE_INFORMATION.put(key, value.toString()));
    DATABASE_INFORMATION_ADVANCED_SETTINGS = config.getConfigurationSection("database-information.advanced-settings").getValues(false);
  }
  
  private void update() {
    boolean save = false;
    YamlConfiguration internalConfig;
    YamlConfiguration config;
    try (InputStream inputStream = QualityEconomy.getInstance().getResource(file.getName());
         InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
      internalConfig = YamlConfiguration.loadConfiguration(inputStreamReader);
    } catch (IOException exception) {
      new Debug.QualityError("Failed to load internal config.yml", exception).log();
      return;
    }
    config = YamlConfiguration.loadConfiguration(file);
    
    for (String key : internalConfig.getKeys(true))
      if (!config.contains(key)) {
        config.set(key, internalConfig.get(key));
        save = true;
      }
    
    for (String key : config.getKeys(true)) {
      if (!internalConfig.contains(key)) {
        config.set(key, null);
        save = true;
      }
      
      if (!save)
        return;
      try {
        config.save(file);
      } catch (IOException exception) {
        new Debug.QualityError("Failed to update config.yml", exception).log();
      }
    }
  }
  
  private void makeSafe() {
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
    boolean save = false;
    int BACKUP_INTERVAL = config.getInt("backup-interval");
    if (BACKUP_INTERVAL < 1800) {
      config.set("backup-interval", BACKUP_INTERVAL * 3600);
      save = true;
    }
    if (!save)
      return;
    try {
      config.save(file);
    } catch (IOException exception) {
      new Debug.QualityError("Failed to update config.yml during safety analysis", exception).log();
    }
  }
  
}
