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
  private static String version;
  public static boolean COMMAND_BALANCE;
  public static boolean COMMAND_BALANCETOP;
  public static boolean COMMAND_ECONOMY;
  public static boolean COMMAND_PAY;
  public static boolean BANKNOTES;
  public static int DECIMAL_PLACES;
  
  public static void loadConfiguration() {
    File file = new File(QualityEconomy.getPluginFolder(), "config.yml");
    if (!file.exists())
      QualityEconomy.getInstance().saveResource("config.yml", false);
    configuration = YamlConfiguration.loadConfiguration(file);
    version = configuration.getString("version", "0");
    COMMAND_BALANCE = configuration.getBoolean("commands.balance", true);
    COMMAND_BALANCETOP = configuration.getBoolean("commands.balancetop", true);
    COMMAND_ECONOMY = configuration.getBoolean("commands.economy", true);
    COMMAND_PAY = configuration.getBoolean("commands.pay", true);
    BANKNOTES = configuration.getBoolean("banknotes", false);
    DECIMAL_PLACES = configuration.getInt("decimal-places", 4);
    if (DECIMAL_PLACES < 0)
      DECIMAL_PLACES = 0;
  }
  
  public static String getStorageType() {
    return configuration.getString("storage-type", "sqlite").toLowerCase();
  }
  
  public static double getBackupInterval() {
    return configuration.getDouble("backup-interval", 0);
  }
  
  public static List<String> getMySQL() {
    return List.of(
      configuration.getString("MySQL.port", "3306"),
      configuration.getString("MySQL.user", "root"),
      configuration.getString("MySQL.password", "root")
    );
  }
  
  public static String getVersion() {
    return version;
  }
  
  public static void updateConfiguration() {
    if (QualityEconomy.getInstance().getPluginMeta().getVersion().equals(version))
      return;
    File configFile = new File(QualityEconomy.getPluginFolder(), "config.yml");
    Map<String, Object> values = new HashMap<>();
    configuration.getKeys(true).forEach(key -> {
      if (!key.equals("version"))
        values.putIfAbsent(key, configuration.get(key));
    });
    QualityEconomy.getInstance().saveResource("config.yml", true);
    configuration = YamlConfiguration.loadConfiguration(configFile);
    values.forEach(configuration::set);
    try {
      configuration.save(configFile);
    } catch (IOException exception) {
      new Error("Failed to update configuration", exception).log();
    }
    loadConfiguration();
    
    values.clear();
    File messagesFile = new File(QualityEconomy.getPluginFolder(), "messages.yml");
    final YamlConfiguration tempMessages = YamlConfiguration.loadConfiguration(messagesFile);
    tempMessages.getKeys(true).forEach(key -> values.putIfAbsent(key, tempMessages.get(key)));
    QualityEconomy.getInstance().saveResource("messages.yml", true);
    YamlConfiguration messages = YamlConfiguration.loadConfiguration(messagesFile);
    values.forEach(messages::set);
    try {
      messages.save(messagesFile);
    } catch (IOException exception) {
      new Error("Failed to update configuration", exception).log();
    }
    Messages.loadMessages();
  }
  
}
