package com.imnotstable.qualityeconomy.hooks;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.commands.BalanceTopCommand;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.Logger;
import com.imnotstable.qualityeconomy.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaceholderHook extends PlaceholderExpansion {
  
  public static void initPlaceholderHook() {
    new PlaceholderHook().register();
    Logger.log(Component.text("Successfully loaded PlaceholderAPI hook.", NamedTextColor.GREEN));
  }
  
  @Override
  public boolean canRegister() {
    return true;
  }
  
  @Override
  public @NotNull String getAuthor() {
    return "ImNotStable";
  }
  
  @Override
  public @NotNull String getIdentifier() {
    return QualityEconomy.getInstance().getName();
  }
  
  @Override
  public @NotNull String getVersion() {
    return Configuration.VERSION;
  }
  
  @Override
  public boolean persist() {
    return true;
  }
  
  @Override
  public @NotNull List<String> getPlaceholders() {
    return List.of("balancetop_#<number>", "balance", "balance_<uuid>", "balance_<player>", "isPayable", "isPayable_<uuid>", "isPayable_<player>");
  }
  
  @Override
  public String onPlaceholderRequest(Player player, String identifier) {
    String[] splitIdentifier = identifier.split("_");
    
    switch (splitIdentifier[0]) {
      
      case "balancetop" -> {
        int place = Integer.parseInt(splitIdentifier[1].substring(1)) - 1;
        return BalanceTopCommand.orderedPlayerList.get(place).getName();
      }
      
      case "balance" -> {
        java.util.UUID uuid;
        if (splitIdentifier.length > 1) {
          if (UUID.isValidUUID(splitIdentifier[1])) {
            uuid = java.util.UUID.fromString(splitIdentifier[1]);
          } else {
            uuid = StorageManager.getUUID(splitIdentifier[1]);
          }
        } else {
          uuid = StorageManager.getUUID(player);
        }
        return String.valueOf(AccountManager.getAccount(uuid).getBalance());
      }
      
      case "isPayable" -> {
        java.util.UUID uuid;
        if (splitIdentifier.length > 1) {
          if (UUID.isValidUUID(splitIdentifier[1])) {
            uuid = java.util.UUID.fromString(splitIdentifier[1]);
          } else {
            uuid = StorageManager.getUUID(splitIdentifier[1]);
          }
        } else {
          uuid = StorageManager.getUUID(player);
        }
        return String.valueOf(AccountManager.getAccount(uuid).getPayable());
      }
      
    }
    return null;
  }
  
}
