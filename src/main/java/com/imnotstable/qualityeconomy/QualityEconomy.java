package com.imnotstable.qualityeconomy;

import com.imnotstable.qualityeconomy.bStats.Metrics;
import com.imnotstable.qualityeconomy.banknotes.BankNotes;
import com.imnotstable.qualityeconomy.commands.CommandManager;
import com.imnotstable.qualityeconomy.commands.MainCommand;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.hooks.HookManager;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class QualityEconomy extends JavaPlugin {
  private static QualityEconomy instance;
  
  public static QualityEconomy getInstance() {
    return instance;
  }
  
  public static File getPluginFolder() {
    return getInstance().getDataFolder();
  }
  
  @Override
  public void onLoad() {
    CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
  }
  
  @Override
  public void onEnable() {
    TestToolkit.Timer timer = new TestToolkit.Timer("Enabling QualityEconomy...");
    instance = this;
    CommandAPI.onEnable();
    new Metrics(this, 20121);
    
    HookManager.loadHooks(this);
    
    Bukkit.getPluginManager().registerEvents(new StorageManager(), this);
    Bukkit.getPluginManager().registerEvents(new BankNotes(), this);
    
    Configuration.loadConfiguration();
    Configuration.updateConfiguration();
    Messages.loadMessages();
    Messages.updateMessages();
    CustomCurrencies.loadCustomCurrencies();
    MainCommand.loadCommand();
    CommandManager.loadCommands();
    
    StorageManager.initStorageProcesses();
    
    timer.end("Enabled QualityEconomy");
  }
  
  @Override
  public void onDisable() {
    TestToolkit.Timer timer = new TestToolkit.Timer("Disabling QualityEconomy...");
    StorageManager.endStorageProcesses();
    timer.end("Disabled QualityEconomy");
  }
}
