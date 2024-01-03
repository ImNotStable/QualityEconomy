package com.imnotstable.qualityeconomy.hooks;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Logger;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    if (Bukkit.getPluginManager().isPluginEnabled("Vault"))
      vaultEnabled = VaultHook.load();
    else
      Logger.log(Component.text("Couldn't find Vault. Vault functionality will be disabled.", NamedTextColor.RED));
    
    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
      placeholderapiEnabled = PlaceholderHook.load();
    else
      Logger.log(Component.text("Couldn't find PlaceholderAPI. PlaceholderAPI functionality will be disabled.", NamedTextColor.RED));
    
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
