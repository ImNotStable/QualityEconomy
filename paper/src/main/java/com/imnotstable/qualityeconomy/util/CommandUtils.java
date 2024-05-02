package com.imnotstable.qualityeconomy.util;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.config.MessageType;
import com.imnotstable.qualityeconomy.config.Messages;
import com.imnotstable.qualityeconomy.economy.Currency;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUtils {
  
  public static boolean requirement(boolean requirement, Currency currency, MessageType errorMessage, CommandSender sender) {
    if (!requirement) {
      Messages.sendParsedMessage(sender, currency.getMessage(errorMessage));
      return true;
    }
    return false;
  }
  
  public static Argument<OfflinePlayer> TargetArgument(Currency currency, MessageType errorMessage) {
    return new CustomArgument<>(new OfflinePlayerArgument("target"), info -> {
      OfflinePlayer target = info.currentInput();
      if (!QualityEconomyAPI.hasAccount(target.getUniqueId()))
        throw CustomArgument.CustomArgumentException.fromAdventureComponent(Messages.getParsedMessage(currency.getMessage(errorMessage),
          "player", target.getName()));
      return target;
    }).replaceSuggestions(ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)));
  }
  
  public static Argument<Double> AmountArgument(Currency currency, MessageType errorMessage) {
    return new CustomArgument<>(new StringArgument("amount"), info -> {
      String rawAmount = info.input();
      double amount;
      try {
        amount = CurrencyFormatter.unformat(rawAmount);
      } catch (NumberFormatException exception) {
        throw CustomArgument.CustomArgumentException.fromAdventureComponent(Messages.getParsedMessage(currency.getMessage(errorMessage),
          "amount", rawAmount));
      }
      return currency.round(amount);
    }).replaceSuggestions(ArgumentSuggestions.strings("<amount>")
    );
  }
  
}
