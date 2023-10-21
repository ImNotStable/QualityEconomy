package com.imnotstable.qualityeconomy.hooks;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.util.Version;
import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Error;
import com.imnotstable.qualityeconomy.util.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;

public class SkriptHook {
  
  public static void initSkriptHook() {
    if (Skript.getVersion().isSmallerThan(new Version(2, 7))) {
      Logger.log(Component.text("Invalid Skript version found. Skript functionality will be disabled.", NamedTextColor.RED));
      return;
    }
    SkriptAddon addon = Skript.registerAddon(QualityEconomy.getInstance());
    try {
      addon.loadClasses("com.imnotstable.qualityeconomy.hooks.skriptelements");
    } catch (IOException exception) {
      new Error("Failed to load SkriptAddon elements", exception).log();
    }
    
    Skript.disableHookRegistration(VaultHook.class);
    Logger.log(Component.text("Successfully loaded Skript hook.", NamedTextColor.GREEN));
  }
  
}
