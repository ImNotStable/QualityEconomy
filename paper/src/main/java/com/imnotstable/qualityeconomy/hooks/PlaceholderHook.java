package com.imnotstable.qualityeconomy.hooks;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.util.Misc;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class PlaceholderHook {
  
  public static boolean load() {
    if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
      return false;
    if (!new HookProvider().register()) {
      Logger.logError("Failed to register PlaceholderAPI hook");
      return false;
    }
    return true;
  }
  
  private static class HookProvider extends PlaceholderExpansion {
    
    @Override
    public boolean canRegister() {
      return true;
    }
    
    @Override
    public @NotNull String getAuthor() {
      return String.join(", ", QualityEconomy.getInstance().getPluginMeta().getAuthors());
    }
    
    @Override
    public @NotNull String getIdentifier() {
      return QualityEconomy.getInstance().getName();
    }
    
    @Override
    public @NotNull String getVersion() {
      return QualityEconomy.getInstance().getPluginMeta().getVersion();
    }
    
    @Override
    public boolean persist() {
      return true;
    }
    
    @Override
    public @NotNull List<String> getPlaceholders() {
      return List.of(
        "balance_<currency>", "balance_<currency>_<uuid>", "balance_<currency>_<player>",
        "isPayable_<currency>", "isPayable_<currency>_<uuid>", "isPayable_<currency>_<player>",
        "leaderboard_<currency>_#<integer>_username", "balancetop_<currency>_#<integer>_balance"
      );
    }
    
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String input) {
      String[] elements = input.split("_");
      try {
        return switch (elements[0]) {
          case "balance" -> {
            UUID uuid = grabUUID(elements, player);
            yield String.valueOf(QualityEconomyAPI.getBalance(uuid, elements[1]));
          }
          case "isPayable" -> {
            UUID uuid = grabUUID(elements, player);
            yield QualityEconomyAPI.isPayable(uuid, elements[1]) ? "true" : "false";
          }
          case "leaderboard" -> {
            try {
              Account account = QualityEconomy.getCurrencyConfig().getLeaderboardAccount(QualityEconomy.getCurrencyConfig().getCurrency(elements[1]).orElseThrow(() -> new Exception("Invalid Currency")), Integer.parseInt(elements[2]));
              if (elements.length > 3 && elements[3].equals("username")) {
                yield account.getUsername();
              } else {
                yield String.valueOf(account.getBalance(elements[1]));
              }
            } catch (NumberFormatException exception) {
              throw new Exception("Invalid Position");
            }
          }
          default -> throw new Exception();
        };
      } catch (Exception exception) {
        throw new IllegalArgumentException("Unexpected value: " + input);
      }
    }
    
    private @NotNull UUID grabUUID(String[] elements, Player player) throws Exception {
      UUID uuid = null;
      if (elements.length > 2) {
        if (Misc.isUUID(elements[3])) {
          uuid = UUID.fromString(elements[3]);
        } else {
          uuid = Bukkit.getOfflinePlayer(elements[3]).getUniqueId();
        }
      } else if (elements.length == 2) {
        uuid = player.getUniqueId();
      }
      if (uuid == null)
        throw new Exception();
      return uuid;
    }
    
  }
  
}
