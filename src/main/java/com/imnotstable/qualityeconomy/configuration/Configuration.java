package com.imnotstable.qualityeconomy.configuration;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Debug;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class Configuration {
  
  private static final File file = new File(QualityEconomy.getInstance().getDataFolder(), "config.yml");
  private static final Set<String> enabledCommands = new HashSet<>();
  @Getter
  private static final Map<String, Integer> advancedSettings = new HashMap<>();
  @Getter
  private static String storageType;
  @Getter
  private static int decimalPlaces;
  private static boolean banknotes;
  private static boolean customCurrencies;
  @Getter
  private static long backupInterval;
  @Getter
  private static long balancetopInterval;
  @Getter
  private static long autoSaveAccountsInterval;
  private static List<String> databaseInfo;
  
  public static void load() {
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
    for (String command : new String[]{"balance", "balancetop", "economy", "pay", "request", "custombalance", "customeconomy"})
      if (configuration.getBoolean("commands." + command, Debug.DEBUG_MODE))
        enabledCommands.add(command);
    customCurrencies = configuration.getBoolean("custom-currencies", false);
    if (!customCurrencies) {
      enabledCommands.remove("custombalance");
      enabledCommands.remove("customeconomy");
    }
    backupInterval = (long) (configuration.getDouble("backup-interval", 1) * 72000);
    balancetopInterval = configuration.getInt("balancetop-inverval", 5) * 20L;
    autoSaveAccountsInterval = configuration.getInt("autosave-accounts-interval", 60) * 20L;
    databaseInfo = List.of(
      configuration.getString("database-information.database"),
      configuration.getString("database-information.address"),
      configuration.getString("database-information.port"),
      configuration.getString("database-information.username"),
      configuration.getString("database-information.password")
    );
    advancedSettings.clear();
    advancedSettings.put("maximum-pool-size", configuration.getInt("advanced-settings.maximum-pool-size", 10));
    advancedSettings.put("minimum-idle", configuration.getInt("advanced-settings.minimum-idle", 10));
    advancedSettings.put("maximum-liftime", configuration.getInt("advanced-settings.maximum-lifetime", 1800000));
    advancedSettings.put("keepalive-time", configuration.getInt("advanced-settings.keepalive-time", 0));
    advancedSettings.put("connection-timeout", configuration.getInt("advanced-settings.connection-timeout", 5000));
  }
  
  public static void update() {
    boolean save = false;
    YamlConfiguration internalConfig;
    YamlConfiguration config;
    try (InputStream inputStream = QualityEconomy.getInstance().getResource(file.getName());
         InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
      internalConfig = YamlConfiguration.loadConfiguration(inputStreamReader);
      config = YamlConfiguration.loadConfiguration(file);
    } catch (IOException exception) {
      new Debug.QualityError("Failed to load internal config.yml", exception).log();
      return;
    }
    
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
      
      if (save)
        try {
          config.save(file);
        } catch (IOException exception) {
          new Debug.QualityError("Failed to update config.yml", exception).log();
        }
    }
  }
  
  public static boolean areBanknotesEnabled() {
    return banknotes;
  }
  
  public static boolean isCommandEnabled(String command) {
    return enabledCommands.contains(command);
  }
  
  public static boolean areCustomCurrenciesEnabled() {
    return customCurrencies;
  }
  
  public static @NotNull String getDatabaseInfo(int index, String def) {
    String info = databaseInfo.get(index);
    return info != null ? info : def;
  }
  
}
