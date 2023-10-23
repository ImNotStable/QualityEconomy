package com.imnotstable.qualityeconomy.configuration;

import com.imnotstable.qualityeconomy.QualityEconomy;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class Configuration {
  
  private static final String RESOURCE_NAME = "config.yml";
  private static final String PATH = QualityEconomy.getInstance().getDataFolder().getPath();
  private static YamlConfiguration configuration;
  private static String version;
  public static boolean COMMAND_BALANCE;
  public static boolean COMMAND_BALANCETOP;
  public static boolean COMMAND_ECONOMY;
  public static boolean COMMAND_PAY;
  
  public static void loadConfiguration() {
    File file = new File(PATH, RESOURCE_NAME);
    if (!file.exists())
      QualityEconomy.getInstance().saveResource(RESOURCE_NAME, false);
    configuration = YamlConfiguration.loadConfiguration(file);
    version = configuration.getString("version");
    COMMAND_BALANCE = configuration.getBoolean("commands.balance");
    COMMAND_BALANCETOP = configuration.getBoolean("commands.balancetop");
    COMMAND_ECONOMY = configuration.getBoolean("commands.economy");
    COMMAND_PAY = configuration.getBoolean("commands.pay");
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
  
}
