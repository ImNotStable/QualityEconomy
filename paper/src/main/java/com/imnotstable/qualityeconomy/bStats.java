package com.imnotstable.qualityeconomy;

import com.imnotstable.qualityeconomy.hooks.HookManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

public class bStats {
  
  public static void load() {
    Metrics metrics = new Metrics(QualityEconomy.getInstance(), 20121);
    metrics.addCustomChart(new SimplePie("database_type_used", () -> QualityEconomy.getQualityConfig().STORAGE_TYPE));
    metrics.addCustomChart(new SimplePie("vault_usage", () -> String.valueOf(HookManager.isVaultEnabled())));
    metrics.addCustomChart(new SimplePie("placeholderapi_usage", () -> String.valueOf(HookManager.isPlaceholderapiEnabled())));
    metrics.addCustomChart(new SimplePie("update_notifications_usage", () -> String.valueOf(QualityEconomy.getQualityConfig().UPDATE_NOTIFICATIONS)));
  }
  
}
