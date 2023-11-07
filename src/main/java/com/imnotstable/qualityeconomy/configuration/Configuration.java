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
  private static YamlConfiguration configuration;
  public static String STORAGE_TYPE;
  public static int DECIMAL_PLACES;
  public static boolean BANKNOTES;
  public static boolean COMMAND_BALANCE;
  public static boolean COMMAND_BALANCETOP;
  public static boolean COMMAND_ECONOMY;
  public static boolean COMMAND_PAY;
  public static boolean CUSTOM_ECONOMY_COMMANDS;
  public static double BACKUP_INTERVAL;
  public static long BALANCETOP_INTERVAL;
  public static List<String> MYSQL_INFO;
  public static String VERSION;
  
  public static void loadConfiguration() {
    File file = new File(QualityEconomy.getPluginFolder(), "config.yml");
    if (!file.exists())
      QualityEconomy.getInstance().saveResource("config.yml", false);
    configuration = YamlConfiguration.loadConfiguration(file);
    STORAGE_TYPE = configuration.getString("storage-type", "sqlite").toLowerCase();
    DECIMAL_PLACES = configuration.getInt("decimal-places", 4);
    if (DECIMAL_PLACES < 0)
      DECIMAL_PLACES = 0;
    BANKNOTES = configuration.getBoolean("banknotes", false);
    COMMAND_BALANCE = configuration.getBoolean("commands.balance", true);
    COMMAND_BALANCETOP = configuration.getBoolean("commands.balancetop", true);
    COMMAND_ECONOMY = configuration.getBoolean("commands.economy", true);
    COMMAND_PAY = configuration.getBoolean("commands.pay", true);
    CUSTOM_ECONOMY_COMMANDS = configuration.getBoolean("custom-economy-commands", false);
    BACKUP_INTERVAL = configuration.getDouble("backup-interval", 1);
    BALANCETOP_INTERVAL = configuration.getInt("balancetop-inverval", 5);
    MYSQL_INFO = List.of(
      configuration.getString("MySQL.port", "3306"),
      configuration.getString("MySQL.user", "root"),
      configuration.getString("MySQL.password", "root")
    );
    VERSION = configuration.getString("version", "0.0.0");
  }
  
  public static void updateConfiguration() {
    Map<String, Object> values = new HashMap<>();
    File file = new File(QualityEconomy.getPluginFolder(), "config.yml");
    configuration.getKeys(true).stream().filter((key) -> !key.equals("version")).forEach(key -> values.putIfAbsent(key, configuration.get(key)));
    QualityEconomy.getInstance().saveResource("config.yml", true);
    configuration = YamlConfiguration.loadConfiguration(file);
    values.forEach((key, value) -> {
      if (configuration.isSet(key)) configuration.set(key, value);
    });
    try {
      configuration.save(file);
    } catch (IOException exception) {
      new Error("Failed to update configuration", exception).log();
    }
    loadConfiguration();
  }
  
}
