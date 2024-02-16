package com.imnotstable.qualityeconomy.hooks;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.commands.CommandManager;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

public class bStats {
  
  public static void load() {
    Metrics metrics = new Metrics(QualityEconomy.getInstance(), 20121);
    metrics.addCustomChart(new SimplePie("database_type_used", Configuration::getStorageType));
    metrics.addCustomChart(new SimplePie("custom_currency_usage", () -> String.valueOf(Configuration.areCustomCurrenciesEnabled())));
    metrics.addCustomChart(new SimplePie("vault_usage", () -> String.valueOf(HookManager.isVaultEnabled())));
    metrics.addCustomChart(new SimplePie("placeholderapi_usage", () -> String.valueOf(HookManager.isPlaceholderapiEnabled())));
    CommandManager.getCommandNames().forEach(command -> {
      if (command.equals("qualityeconomy")) return;
      metrics.addCustomChart(new SimplePie("command_usage_" + command, () -> String.valueOf(Configuration.isCommandEnabled(command))));
    });
  }
  
}
