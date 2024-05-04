package com.imnotstable.qualitygambling;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class QualityGambling extends JavaPlugin {
  
  @Override
  public void onLoad() {
    CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
  }
  
  @Override
  public void onEnable() {
    CommandAPI.onEnable();
    CoinFlipCommand.getCommand().register();
  }
  
  @Override
  public void onDisable() {
    CommandAPI.onDisable();
  }
  
}
