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
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class QualityEconomy extends JavaPlugin {
  private static QualityEconomy instance;


  public static QualityEconomy getInstance() {
    return instance;
  }

  @Override
  public void onEnable() {
    TestToolkit.Timer timer = new TestToolkit.Timer("Enabling QualityEconomy...");
    instance = this;
    
    new HookManager(this);

    Bukkit.getPluginManager().registerEvents(new StorageManager(), this);

    Configuration.loadConfiguration();
    Messages.loadMessages();
    StorageManager.initStorageProcesses();

    getCommand("qualityeconomy").setExecutor(new MainCommand());
    getCommand("qualityeconomy").setTabCompleter(new MainCommand());

    getCommand("economy").setExecutor(new EconomyCommand());
    getCommand("economy").setTabCompleter(new EconomyCommand());

    getCommand("balancetop").setExecutor(new BalanceTopCommand());
    getCommand("balancetop").setTabCompleter(new BalanceTopCommand());
    BalanceTopCommand.initScheduler();

    getCommand("balance").setExecutor(new BalanceCommand());

    getCommand("pay").setExecutor(new PayCommand());
    getCommand("pay").setTabCompleter(new PayCommand());

    timer.end("Enabled QualityEconomy");
  }

  @Override
  public void onDisable() {
    TestToolkit.Timer timer = new TestToolkit.Timer("Disabling QualityEconomy...");
    StorageManager.endStorageProcesses();
    timer.end("Disabled QualityEconomy");
  }

}
