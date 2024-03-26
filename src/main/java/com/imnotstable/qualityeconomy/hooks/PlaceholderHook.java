package com.imnotstable.qualityeconomy.hooks;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.commands.BalanceTopCommand;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.Logger;
import com.imnotstable.qualityeconomy.util.Misc;
import com.imnotstable.qualityeconomy.util.Number;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class PlaceholderHook {
  
  public static boolean load() {
    if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
      return false;
    
    if (new HookProvider().register()) {
      Logger.log(Component.text("Successfully registered expansion with PlaceholderAPI", NamedTextColor.GREEN));
      return true;
    }
    return false;
  }
  
  private static class HookProvider extends PlaceholderExpansion {
    
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
        "balancetop_#<integer>", "balancetop_balance_#<integer>",
        "balance", "balance_<uuid>", "balance_<player>",
        "cbalance_<currency>", "cbalance_<currency>_<uuid>", "cbalance_<currency>_<player>",
        "isPayable", "isPayable_<uuid>", "isPayable_<player>",
        "isRequestable", "isRequestable_<uuid>", "isRequestable_<player>"
      );
    }
    
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String input) {
      String[] elements = input.split("_");
      
      try {
        switch (elements[0]) {
          case "balancetop" -> {
            int place;
            try {
              int index;
              if (elements.length == 2 && elements[1].startsWith("#"))
                index = 1;
              else if (elements.length == 3 && elements[2].startsWith("#"))
                index = 2;
              else
                return null;
              place = Integer.parseInt(elements[index].substring(1)) - 1;
            } catch (NumberFormatException exception) {
              new Debug.QualityError("Invalid input for \"balancetop_#<integer>\": " + input, exception).log();
              return null;
            }
            if (place == -1 || BalanceTopCommand.orderedPlayerList.length < place + 1)
              return "N/A";
            if (elements[1].equals("balance"))
              return Number.format(BalanceTopCommand.orderedPlayerList[place].getBalance(), Number.FormatType.NORMAL);
            else
              return BalanceTopCommand.orderedPlayerList[place].getUsername();
          }
          case "balance" -> {
            UUID uuid = grabUUID(elements, player, 1);
            return Number.format(QualityEconomyAPI.getBalance(uuid), Number.FormatType.NORMAL);
          }
          case "cbalance" -> {
            if (!QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES)
              return "Feature is disabled";
            if (!QualityEconomyAPI.doesCustomCurrencyExist(elements[1]))
              return "Currency does not exist";
            UUID uuid = grabUUID(elements, player, 2);
            return Number.format(QualityEconomyAPI.getCustomBalance(uuid, elements[1]), Number.FormatType.NORMAL);
          }
          case "isPayable" -> {
            UUID uuid = grabUUID(elements, player, 1);
            return String.valueOf(QualityEconomyAPI.isPayable(uuid));
          }
          case "isRequestable" -> {
            UUID uuid = grabUUID(elements, player, 1);
            return String.valueOf(QualityEconomyAPI.isRequestable(uuid));
          }
        }
      } catch (Exception ignored) {
      }
      
      return null;
    }
    
    private @NotNull UUID grabUUID(String[] elements, Player player, int index) throws Exception {
      UUID uuid = null;
      if (elements.length == index + 1) {
        if (Misc.isUUID(elements[index])) {
          uuid = UUID.fromString(elements[index]);
        } else {
          uuid = Bukkit.getOfflinePlayer(elements[index]).getUniqueId();
        }
      } else if (elements.length == index) {
        uuid = player.getUniqueId();
      }
      if (uuid == null)
        throw new Exception();
      return uuid;
    }
    
  }
  
}
