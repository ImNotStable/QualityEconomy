package com.imnotstable.qualityeconomy.configuration;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Debug;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
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
  @Getter
  private static List<String> MySQL;
  
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
    Set.of("balance", "balancetop", "economy", "pay", "request", "custombalance", "customeconomy").forEach(command -> {
      if (configuration.getBoolean("commands." + command, Debug.DEBUG_MODE))
        enabledCommands.add(command);
    });
    customCurrencies = configuration.getBoolean("custom-currencies", false);
    if (!customCurrencies) {
      enabledCommands.remove("custombalance");
      enabledCommands.remove("customeconomy");
    }
    backupInterval = (long) (configuration.getDouble("backup-interval", 1) * 72000);
    balancetopInterval = configuration.getInt("balancetop-inverval", 5) * 20L;
    autoSaveAccountsInterval = configuration.getInt("autosave-accounts-interval", 60) * 20L;
    MySQL = List.of(
      configuration.getString("MySQL.address", "localhost:3306"),
      configuration.getString("MySQL.name", "qualityeconomy"),
      configuration.getString("MySQL.user", "root"),
      configuration.getString("MySQL.password", "root")
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
      new Debug.QualityError("Failed to update config.yml", exception).log();
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
  
}
