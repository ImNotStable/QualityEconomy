package com.imnotstable.qualityeconomy.configuration;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.QualityError;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {
  
  private static final File file = new File(QualityEconomy.getInstance().getDataFolder(), "config.yml");
  private static String storageType;
  private static int decimalPlaces;
  private static boolean banknotes;
  private static final List<String> enabledCommands = new ArrayList<>();
  private static boolean customCurrencies;
  private static long backupInterval;
  private static long balancetopInterval;
  private static List<String> connectionInfo;
  
  public static void load() {
    File file = new File(QualityEconomy.getInstance().getDataFolder(), "config.yml");
    if (!file.exists())
      QualityEconomy.getInstance().saveResource("config.yml", false);
    else
      update();
    reload();
  }
  
  public static void reload() {
    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
    storageType = configuration.getString("storage-type", "sqlite").toLowerCase();
    decimalPlaces = Math.max(configuration.getInt("decimal-places", 4), 0);
    banknotes = configuration.getBoolean("banknotes", false);
    enabledCommands.clear();
    List.of("balance", "balancetop", "economy", "pay", "custombalance", "customeconomy").forEach(command -> {
      if (configuration.getBoolean("commands." + command, false))
        enabledCommands.add(command);
    });
    customCurrencies = configuration.getBoolean("custom-currencies", false);
    backupInterval = (long) (configuration.getDouble("backup-interval", 1) * 20 * 60 * 60);
    balancetopInterval = configuration.getInt("balancetop-inverval", 5) * 20L;
    connectionInfo = List.of(
      configuration.getString("database.address", "localhost:3306"),
      configuration.getString("database.name", "qualityeconomy"),
      configuration.getString("database.user", "root"),
      configuration.getString("database.password", "root")
    );
  }
  
  public static void update() {
    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
    Map<String, Object> values = new HashMap<>();
    configuration.getKeys(true).forEach(key -> values.putIfAbsent(key, configuration.get(key)));
    QualityEconomy.getInstance().saveResource("config.yml", true);
    YamlConfiguration finalConfiguration = YamlConfiguration.loadConfiguration(file);
    values.forEach((key, value) -> {
      if (finalConfiguration.contains(key))
        finalConfiguration.set(key, value);
    });
    try {
      finalConfiguration.save(file);
    } catch (IOException exception) {
      new QualityError("Failed to update config.yml", exception).log();
    }
  }
  
  public static String getStorageType() {
    return storageType;
  }
  
  public static int getDecimalPlaces() {
    return decimalPlaces;
  }
  
  public static boolean areBanknotesEnabled() {
    return banknotes;
  }
  
  public static boolean isCommandEnabled(String command) {
    return enabledCommands.contains(command);
  }
  
  public static boolean isBalanceCommandEnabled() {
    return enabledCommands.contains("balance");
  }
  
  public static boolean isBalancetopCommandEnabled() {
    return enabledCommands.contains("balancetop");
  }
  
  public static boolean isEconomyCommandEnabled() {
    return enabledCommands.contains("economy");
  }
  
  public static boolean isPayCommandEnabled() {
    return enabledCommands.contains("pay");
  }
  
  public static boolean isCustomBalanceCommandEnabled() {
    return enabledCommands.contains("custombalance");
  }
  
  public static boolean isCustomEconomyCommandEnabled() {
    return enabledCommands.contains("customeconomy");
  }
  
  public static boolean areCustomCurrenciesEnabled() {return customCurrencies;}
  
  public static long getBackupInterval() {
    return backupInterval;
  }
  
  public static long getBalancetopInterval() {
    return balancetopInterval;
  }
  
  public static List<String> getConnectionInfo() {
    return connectionInfo;
  }
  
}
