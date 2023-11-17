package com.imnotstable.qualityeconomy.configuration;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Error;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {
  
  private static final File file = new File(QualityEconomy.getPluginFolder(), "config.yml");
  private static String storageType;
  private static int decimalPlaces;
  private static boolean banknotes;
  private static boolean balanceCommand;
  private static boolean balancetopCommand;
  private static boolean economyCommand;
  private static boolean payCommand;
  private static boolean custombalanceCommand;
  private static boolean customeconomyCommand;
  private static long backupInterval;
  private static long balancetopInterval;
  private static List<String> mysqlInfo;
  private static String version;
  
  public static void load() {
    File file = new File(QualityEconomy.getPluginFolder(), "config.yml");
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
    balanceCommand = configuration.getBoolean("commands.balance", true);
    balancetopCommand = configuration.getBoolean("commands.balancetop", true);
    economyCommand = configuration.getBoolean("commands.economy", true);
    payCommand = configuration.getBoolean("commands.pay", true);
    custombalanceCommand = configuration.getBoolean("custom-economy-commands.custombalance", true);
    customeconomyCommand = configuration.getBoolean("custom-economy-commands.customeconomy", true);
    backupInterval = (long) (configuration.getDouble("backup-interval", 1) * 20 * 60 * 60);
    balancetopInterval = configuration.getInt("balancetop-inverval", 5) * 20L;
    mysqlInfo = List.of(
      configuration.getString("MySQL.address", "localhost:3306"),
      configuration.getString("MySQL.name", "qualityeconomy"),
      configuration.getString("MySQL.user", "root"),
      configuration.getString("MySQL.password", "root")
    );
    version = configuration.getString("version", QualityEconomy.getInstance().getDescription().getVersion());
  }
  
  public static void update() {
    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
    Map<String, Object> values = new HashMap<>();
    configuration.getKeys(true).stream().filter((key) -> !key.equals("version")).forEach(key -> values.putIfAbsent(key, configuration.get(key)));
    QualityEconomy.getInstance().saveResource("config.yml", true);
    YamlConfiguration finalConfiguration = YamlConfiguration.loadConfiguration(file);
    values.forEach((key, value) -> {
      if (finalConfiguration.contains(key))
        finalConfiguration.set(key, value);
    });
    try {
      finalConfiguration.save(file);
    } catch (IOException exception) {
      new Error("Failed to update config.yml", exception).log();
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
  
  public static boolean isBalanceCommandEnabled() {
    return balanceCommand;
  }
  
  public static boolean isBalancetopCommandEnabled() {
    return balancetopCommand;
  }
  
  public static boolean isEconomyCommandEnabled() {
    return economyCommand;
  }
  
  public static boolean isPayCommandEnabled() {
    return payCommand;
  }
  
  public static boolean isCustomBalanceCommandEnabled() {
    return custombalanceCommand;
  }
  
  public static boolean isCustomEconomyCommandEnabled() {
    return customeconomyCommand;
  }
  
  public static long getBackupInterval() {
    return backupInterval;
  }
  
  public static long getBalancetopInterval() {
    return balancetopInterval;
  }
  
  public static List<String> getMySQLInfo() {
    return mysqlInfo;
  }
  
  public static String getVersion() {
    return version;
  }
}
