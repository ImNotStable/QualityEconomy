package com.imnotstable.qualityeconomy.hooks;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

public class HookManager {
  
  public static void loadHooks(QualityEconomy plugin) {
    if (Bukkit.getPluginManager().isPluginEnabled("Vault"))
      VaultHook.initVaultHook(plugin);
    else
      Logger.log(Component.text("Couldn't find Vault. Vault functionality will be disabled.", NamedTextColor.RED));
    
    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
      PlaceholderHook.initPlaceholderHook();
    else
      Logger.log(Component.text("Couldn't find PlaceholderAPI. PlaceholderAPI functionality will be disabled.", NamedTextColor.RED));
  }
  
}
