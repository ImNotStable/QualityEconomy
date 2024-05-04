package com.imnotstable.qualityeconomy.hooks;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.economy.Account;
import com.imnotstable.qualityeconomy.economy.Currency;
import com.imnotstable.qualityeconomy.util.Misc;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
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
  
  private static Currency validateCurrency(String currency) throws Exception {
    Optional<Currency> optionalCurrency = QualityEconomy.getCurrencyConfig().getCurrency(currency);
    if (optionalCurrency.isEmpty())
      throw new Exception("Invalid Currency");
    return optionalCurrency.get();
  }
  
  private static UUID grabUUID(String[] elements, OfflinePlayer player) throws Exception {
    if (elements.length == 3) {
      Optional<UUID> optionalUUID = Misc.isUUID(elements[2]);
      if (optionalUUID.isPresent())
        return optionalUUID.get();
      OfflinePlayer target = Bukkit.getOfflinePlayer(elements[2]);
      if (!target.hasPlayedBefore())
        throw new Exception("Invalid UUID/Player Name input");
      return target.getUniqueId();
    } else {
      if (player == null)
        throw new Exception("Player was found to be null");
      return player.getUniqueId();
    }
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
        "leaderboard_<currency>_#<integer>_username", "leaderboard_<currency>_#<integer>_balance"
      );
    }
    
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String input) {
      String[] elements = input.split("_");
      try {
        return switch (elements[0]) {
          case "balance" -> {
            UUID uuid = grabUUID(elements, player);
            Currency currency = validateCurrency(elements[1]);
            yield currency.getFormattedAmount(QualityEconomyAPI.getBalance(uuid, elements[1]));
          }
          case "isPayable" -> {
            UUID uuid = grabUUID(elements, player);
            validateCurrency(elements[1]);
            yield QualityEconomyAPI.isPayable(uuid, elements[1]) ? "true" : "false";
          }
          case "leaderboard" -> {
            validateCurrency(elements[1]);
            Currency currency = QualityEconomy.getCurrencyConfig().getCurrency(elements[1]).get();
            try {
              Account account = QualityEconomy.getCurrencyConfig().getLeaderboardAccount(currency, Integer.parseInt(elements[2].substring(1)) - 1);
              if (account == null)
                yield "N/A";
              if (elements.length > 3 && elements[3].equals("username")) {
                yield account.getUsername();
              } else {
                yield currency.getFormattedAmount(account.getBalance(elements[1]));
              }
            } catch (NumberFormatException exception) {
              throw new Exception("Invalid Position");
            }
          }
          default -> throw new Exception("Unknown Placeholder");
        };
      } catch (Exception exception) {
        Logger.logError("Error while processing PlaceholderAPI request (" + input + ")", exception);
        return "Error";
      }
    }
  }
  
}
