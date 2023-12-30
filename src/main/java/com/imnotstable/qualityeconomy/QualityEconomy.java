package com.imnotstable.qualityeconomy;

import com.imnotstable.qualityeconomy.commands.CommandManager;
import com.imnotstable.qualityeconomy.commands.WithdrawCommand;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.hooks.HookManager;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.Logger;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class QualityEconomy extends JavaPlugin {
  
  @Getter
  private static QualityEconomy instance;
  
  @Override
  public void onLoad() {
    CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(false).silentLogs(true));
  }
  
  @Override
  public void onEnable() {
    if (new File(getDataFolder(), "debug_mode").exists()) {
      Debug.DEBUG_MODE = true;
      Logger.log(Component.text("Enabled DEBUG_MODE", NamedTextColor.GRAY));
    }
    Debug.Timer timer = new Debug.Timer("onEnable()");
    new Debug.QualityLogger("This is a warning", "Please export your database before updating to any version", "Once this message no longer exists, then that will no longer be required.").log();
    instance = this;
    CommandAPI.onEnable();
    
    Configuration.load();
    Messages.load();
    
    StorageManager.initStorageProcesses();
    
    CommandManager.registerCommands();
    
    HookManager.loadHooks(this);
    
    Metrics metrics = new Metrics(this, 20121);
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
    
    Bukkit.getPluginManager().registerEvents(new StorageManager(), this);
    Bukkit.getPluginManager().registerEvents(new WithdrawCommand(), this);
    
    timer.end();
  }
  
  @Override
  public void onDisable() {
    Debug.Timer timer = new Debug.Timer("onDisable()");
    StorageManager.endStorageProcesses();
    CommandAPI.onDisable();
    timer.end();
  }
}
