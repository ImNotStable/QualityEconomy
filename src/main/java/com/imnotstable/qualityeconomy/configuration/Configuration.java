package com.imnotstable.qualityeconomy.configuration;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.QualityError;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {
  
  private static final File file = new File(QualityEconomy.getInstance().getDataFolder(), "config.yml");
  private static final List<String> enabledCommands = new ArrayList<>();
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
    List.of("balance", "balancetop", "economy", "pay", "request", "custombalance", "customeconomy").forEach(command -> {
      if (configuration.getBoolean("commands." + command, TestToolkit.DEBUG_MODE))
        enabledCommands.add(command);
    });
    customCurrencies = configuration.getBoolean("custom-currencies", false);
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
      new QualityError("Failed to update config.yml", exception).log();
    }
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
  
  public static boolean isRequestCommandEnabled() {
    return enabledCommands.contains("request");
  }
  
  public static boolean isCustomBalanceCommandEnabled() {
    return enabledCommands.contains("custombalance");
  }
  
  public static boolean isCustomEconomyCommandEnabled() {
    return enabledCommands.contains("customeconomy");
  }
  
  public static boolean areCustomCurrenciesEnabled() {
    return customCurrencies;
  }
  
}
