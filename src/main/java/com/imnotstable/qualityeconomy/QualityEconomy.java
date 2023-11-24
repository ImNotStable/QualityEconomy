package com.imnotstable.qualityeconomy;

import com.imnotstable.qualityeconomy.commands.CommandManager;
import com.imnotstable.qualityeconomy.commands.MainCommand;
import com.imnotstable.qualityeconomy.commands.WithdrawCommand;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.hooks.HookManager;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.Logger;
import com.imnotstable.qualityeconomy.util.QualityLogger;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import fr.mrmicky.fastinv.FastInvManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

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
    if (new File(getDataFolder(), "debug_mode").exists()) {
      TestToolkit.DEBUG_MODE = true;
      Logger.log(Component.text("Enabled DEBUG_MODE", NamedTextColor.GRAY));
    }
    TestToolkit.Timer timer = new TestToolkit.Timer("Enabling QualityEconomy...");
    new QualityLogger("This is a warning\nPlease export your database before updating to any version.\nOnce this message no longer exists, then that will no longer be required.").log();
    instance = this;
    CommandAPI.onEnable();
    FastInvManager.register(this);
    new Metrics(this, 20121);
    
    Configuration.load();
    Messages.load();
    CustomCurrencies.loadCustomCurrencies();
    MainCommand.register();
    CommandManager.registerCommands();
    
    HookManager.loadHooks(this);
    
    Bukkit.getPluginManager().registerEvents(new StorageManager(), this);
    Bukkit.getPluginManager().registerEvents(new WithdrawCommand(), this);
    
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
