package com.imnotstable.qualityeconomy;

import com.imnotstable.qualityeconomy.commands.BalanceCommand;
import com.imnotstable.qualityeconomy.commands.BalanceTopCommand;
import com.imnotstable.qualityeconomy.commands.EconomyCommand;
import com.imnotstable.qualityeconomy.commands.MainCommand;
import com.imnotstable.qualityeconomy.commands.PayCommand;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.hooks.HookManager;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class QualityEconomy extends JavaPlugin {
  private static QualityEconomy instance;
  
  
  public static QualityEconomy getInstance() {
    return instance;
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
    new HookManager(this);
    
    Bukkit.getPluginManager().registerEvents(new StorageManager(), this);
    
    Configuration.loadConfiguration();
    Messages.loadMessages();
    StorageManager.initStorageProcesses();
    
    MainCommand.loadMainCommand();
    EconomyCommand.loadEconomyCommand();
    BalanceCommand.loadBalanceCommand();
    PayCommand.loadPayCommand();
    BalanceTopCommand.loadBalanceTopCommand();
    
    BalanceTopCommand.initScheduler();
    
    timer.end("Enabled QualityEconomy");
  }
  
  @Override
  public void onDisable() {
    TestToolkit.Timer timer = new TestToolkit.Timer("Disabling QualityEconomy...");
    StorageManager.endStorageProcesses();
    timer.end("Disabled QualityEconomy");
  }
  
}
