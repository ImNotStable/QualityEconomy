package com.imnotstable.qualityeconomy;

import com.imnotstable.qualityeconomy.commands.CommandManager;
import com.imnotstable.qualityeconomy.config.Config;
import com.imnotstable.qualityeconomy.config.Messages;
import com.imnotstable.qualityeconomy.hooks.HookManager;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.UpdateChecker;
import com.imnotstable.qualityeconomy.util.debug.Debug;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import com.imnotstable.qualityeconomy.util.debug.Timer;
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
  @Getter
  private static Config qualityConfig;
  @Getter
  private static Messages qualityMessages;
  
  @Override
  public void onLoad() {
    if (new File(getDataFolder(), "debug_mode").exists()) {
      Debug.DEBUG_MODE = true;
      Logger.log(Component.text("Enabled DEBUG_MODE", NamedTextColor.GRAY));
    }
    CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
      .verboseOutput(Debug.DEBUG_MODE)
      .silentLogs(!Debug.DEBUG_MODE)
    );
  }
  
  @Override
  public void onEnable() {
    Timer timer = new Timer("onEnable()");
    instance = this;
    CommandAPI.onEnable();
    
    qualityConfig = new Config(this);
    qualityMessages = new Messages(this);
    
    StorageManager.initStorageProcesses(this);
    CommandManager.registerCommands();
    
    HookManager.loadHooks();
    
    Bukkit.getPluginManager().registerEvents(new StorageManager(), this);
    
    UpdateChecker.load(QualityEconomy.getInstance().getDescription().getVersion());
    
    timer.end();
  }
  
  @Override
  public void onDisable() {
    Timer timer = new Timer("onDisable()");
    StorageManager.endStorageProcesses();
    CommandAPI.onDisable();
    timer.end();
  }
  
}
