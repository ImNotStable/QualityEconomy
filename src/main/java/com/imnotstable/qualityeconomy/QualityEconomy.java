package com.imnotstable.qualityeconomy;

import com.imnotstable.qualityeconomy.commands.CommandManager;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.hooks.HookManager;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.Logger;
import com.imnotstable.qualityeconomy.util.UpdateChecker;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class QualityEconomy extends JavaPlugin {
  
  @Getter
  private static QualityEconomy instance;
  
  @Override
  public void onLoad() {
    if (new File(getDataFolder(), "debug_mode").exists()) {
      Debug.DEBUG_MODE = true;
      Logger.log(Component.text("Enabled DEBUG_MODE", NamedTextColor.GRAY));
    }
    CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
      .verboseOutput(Debug.DEBUG_MODE)
      .silentLogs(!Debug.DEBUG_MODE)
      //.dispatcherFile(new File(getDataFolder(), "command_registration.json"))
    );
  }
  
  @Override
  public void onEnable() {
    Debug.Timer timer = new Debug.Timer("onEnable()");
    new Debug.QualityLogger("This is a warning", "Please export your database before updating to any version", "Once this message no longer exists, doing so will no longer be required.").log();
    instance = this;
    CommandAPI.onEnable();
    
    Configuration.load();
    Messages.load();
    
    StorageManager.initStorageProcesses();
    CommandManager.registerCommands();
    
    HookManager.loadHooks();
    
    Bukkit.getPluginManager().registerEvents(new StorageManager(), this);
    
    UpdateChecker.load();
    
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
