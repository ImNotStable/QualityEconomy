package com.imnotstable.qualityeconomy.hooks;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.commands.BalanceTopCommand;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.Logger;
import com.imnotstable.qualityeconomy.util.Misc;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class PlaceholderHook extends PlaceholderExpansion {
  
  public static boolean load() {
    if (new PlaceholderHook().register()) {
      Logger.log(Component.text("Successfully registered expansion with PlaceholderAPI", NamedTextColor.GREEN));
      return true;
    } else
      Logger.log(Component.text("Failed to register expansion with PlaceholderAPI", NamedTextColor.RED));
    return false;
  }
  
  @Override
  public boolean canRegister() {
    return true;
  }
  
  @Override
  public @NotNull String getAuthor() {
    return String.join(", ", QualityEconomy.getInstance().getDescription().getAuthors());
  }
  
  @Override
  public @NotNull String getIdentifier() {
    return QualityEconomy.getInstance().getName();
  }
  
  @Override
  public @NotNull String getVersion() {
    return QualityEconomy.getInstance().getDescription().getVersion();
  }
  
  @Override
  public boolean persist() {
    return true;
  }
  
  @Override
  public @NotNull List<String> getPlaceholders() {
    return List.of(
      "balancetop_#<integer>",
      "balance", "balance_<uuid>", "balance_<player>",
      "cbalance_<currency>", "cbalance_<currency>_<uuid>", "cbalance_<currency>_<player>",
      "isPayable", "isPayable_<uuid>", "isPayable_<player>",
      "isRequestable", "isRequestable_<uuid>", "isRequestable_<player>"
    );
  }
  
  @Override
  public String onPlaceholderRequest(Player player, @NotNull String input) {
    String[] elements = input.split("_");
    
    switch (elements[0]) {
      case "balancetop" -> {
        if (elements.length == 2 && elements[1].startsWith("#")) {
          try {
            int place = Integer.parseInt(elements[1].substring(1)) - 1;
            return BalanceTopCommand.orderedPlayerList.get(place).getUsername();
          } catch (NumberFormatException exception) {
            new Debug.QualityError("Invalid input for \"balancetop_#<integer>\": " + input, exception).log();
          }
        }
      }
      case "balance" -> {
        UUID uuid = null;
        if (elements.length == 2) {
          if (Misc.isValidUUID(elements[1])) {
            uuid = UUID.fromString(elements[1]);
          } else {
            uuid = Bukkit.getOfflinePlayer(elements[1]).getUniqueId();
          }
        } else if (elements.length == 1) {
          uuid = player.getUniqueId();
        }
        if (uuid == null)
          return null;
        return String.valueOf(QualityEconomyAPI.getBalance(uuid));
      }
      case "cbalance" -> {
        if (Configuration.areCustomCurrenciesEnabled())
          return "Feature is disabled";
        UUID uuid = null;
        if (elements.length == 3) {
          if (Misc.isValidUUID(elements[2])) {
            uuid = UUID.fromString(elements[2]);
          } else {
            uuid = Bukkit.getOfflinePlayer(elements[2]).getUniqueId();
          }
        } else if (elements.length == 2) {
          uuid = player.getUniqueId();
        }
        if (uuid == null)
          return null;
        if (!StorageManager.getActiveStorageFormat().getCurrencies().contains(elements[1]))
          return null;
        return String.valueOf(QualityEconomyAPI.getCustomBalance(uuid, elements[1]));
      }
      case "isPayable" -> {
        UUID uuid = null;
        if (elements.length == 2) {
          if (Misc.isValidUUID(elements[1])) {
            uuid = UUID.fromString(elements[1]);
          } else {
            uuid = Bukkit.getOfflinePlayer(elements[2]).getUniqueId();
          }
        } else if (elements.length == 1) {
          uuid = player.getUniqueId();
        }
        if (uuid == null)
          return null;
        return String.valueOf(QualityEconomyAPI.isPayable(uuid));
      }
      case "isRequestable" -> {
        UUID uuid = null;
        if (elements.length == 2) {
          if (Misc.isValidUUID(elements[1])) {
            uuid = UUID.fromString(elements[1]);
          } else {
            uuid = Bukkit.getOfflinePlayer(elements[2]).getUniqueId();
          }
        } else if (elements.length == 1) {
          uuid = player.getUniqueId();
        }
        if (uuid == null)
          return null;
        return String.valueOf(QualityEconomyAPI.isRequestable(uuid));
      }
    }
    
    return null;
  }
  
}
