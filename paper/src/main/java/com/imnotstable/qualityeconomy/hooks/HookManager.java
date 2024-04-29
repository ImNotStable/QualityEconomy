package com.imnotstable.qualityeconomy.hooks;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.bStats;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

public class HookManager implements Listener {
  
  @Getter
  private static boolean vaultEnabled;
  
  @Getter
  private static boolean placeholderapiEnabled;
  
  public static void loadHooks() {
    vaultEnabled = VaultHook.load();
    placeholderapiEnabled = PlaceholderHook.load();
    bStats.load();
    
    Bukkit.getPluginManager().registerEvents(new Listener() {
      @EventHandler
      public void on(PluginEnableEvent event) {
        switch (event.getPlugin().getName()) {
          case "Vault" -> vaultEnabled = VaultHook.load();
          case "PlaceholderAPI" -> placeholderapiEnabled = PlaceholderHook.load();
        }
      }
    }, QualityEconomy.getInstance());
  }
  
}
