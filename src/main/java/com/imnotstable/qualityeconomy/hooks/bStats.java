package com.imnotstable.qualityeconomy.hooks;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class bStats {

  public static void load() {
    Metrics metrics = new Metrics(QualityEconomy.getInstance(), 20121);
    metrics.addCustomChart(new SimplePie("database_type_used", Configuration::getStorageType));
    metrics.addCustomChart(new SimplePie("custom_currency_usage", () -> String.valueOf(Configuration.areCustomCurrenciesEnabled())));
    metrics.addCustomChart(new SimplePie("vault_usage", () -> String.valueOf(HookManager.isVaultEnabled())));
    metrics.addCustomChart(new SimplePie("placeholderapi_usage", () -> String.valueOf(HookManager.isPlaceholderapiEnabled())));
    metrics.addCustomChart(new AdvancedPie("other_plugins_used", () -> {
      Map<String, Integer> plugins = new HashMap<>();
      for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
        plugins.put(plugin.getName(), 1);
      }
      return plugins;
    }));
  }
  
}
