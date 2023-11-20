package com.imnotstable.qualityeconomy.storage;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.commands.CustomBalanceCommand;
import com.imnotstable.qualityeconomy.commands.CustomEconomyCommand;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.util.QualityError;
import com.imnotstable.qualityeconomy.util.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomCurrencies {
  
  private static final File file = new File(QualityEconomy.getInstance().getDataFolder(), "customCurrencies.yml");
  private static List<String> customCurrencies = new ArrayList<>();
  
  public static void loadCustomCurrencies() {
    if (!Configuration.areCustomCurrenciesEnabled())
      return;
    if (!file.exists())
      return;
    customCurrencies = YamlConfiguration.loadConfiguration(file).getStringList("custom-currencies");
    if (customCurrencies.isEmpty())
      file.delete();
    else {
      CustomBalanceCommand.register();
      CustomEconomyCommand.register();
    }
  }
  
  public static void createCustomCurrency(String currencyName) {
    if (!Configuration.areCustomCurrenciesEnabled())
      return;
    synchronized (StorageManager.lock) {
      currencyName = currencyName.replaceAll(" ", "_").toLowerCase();
      if (List.of("uuid", "name", "balance", "payable").contains(currencyName)) {
        new QualityError("Failed to create currency \"" + currencyName + "\"", "Name cannot be \"uuid\", \"name\", \"balance\", \"payable\"").log();
        return;
      }
      if (!file.exists()) {
        try {
          if (file.createNewFile())
            Logger.log(Component.text("Successfully created customCurrencies.yml", NamedTextColor.GREEN));
        } catch (IOException exception) {
          new QualityError("Failed to created customCurrencies.yml", exception).log();
        }
      }
      customCurrencies.add(currencyName);
      YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
      configuration.set("custom-currencies", customCurrencies);
      configuration.setComments("custom-currencies", List.of("Don't touch this file."));
      try {
        configuration.save(file);
      } catch (IOException exception) {
        new QualityError("Failed to save customCurrencies.yml", exception).log();
      }
      StorageManager.getActiveStorageFormat().addCurrency(currencyName);
      CustomBalanceCommand.register();
      CustomEconomyCommand.register();
    }
  }
  
  public static void deleteCustomCurrency(String currencyName) {
    if (!Configuration.areCustomCurrenciesEnabled())
      return;
    synchronized (StorageManager.lock) {
      customCurrencies.remove(currencyName);
      if (customCurrencies.isEmpty()) {
        file.delete();
        CustomBalanceCommand.unregister();
        CustomEconomyCommand.unregister();
      } else {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        configuration.set("custom-currencies", customCurrencies);
        try {
          configuration.save(file);
        } catch (IOException exception) {
          new QualityError("Failed to save customCurrencies.yml", exception).log();
        }
      }
      StorageManager.getActiveStorageFormat().removeCurrency(currencyName);
    }
  }
  
  public static List<String> getCustomCurrencies() {
    return new ArrayList<>(customCurrencies);
  }
  
}
