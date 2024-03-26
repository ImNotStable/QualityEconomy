package com.imnotstable.qualityeconomy.util;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.config.MessageType;
import com.imnotstable.qualityeconomy.config.Messages;
import com.imnotstable.qualityeconomy.storage.StorageManager;
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
  
  public static boolean requirement(boolean requirement, MessageType errorMessage, CommandSender sender) {
    if (!requirement) {
      Messages.sendParsedMessage(sender, errorMessage);
      return true;
    }
    return false;
  }
  
  public static Argument<String> CurrencyArgument() {
    return new CustomArgument<>(new StringArgument("currency"), info -> {
      String currency = info.input();
      if (!StorageManager.getActiveStorageType().getCurrencies().contains(currency))
        throw CustomArgument.CustomArgumentException.fromAdventureComponent(Messages.getParsedMessage(MessageType.CURRENCY_NOT_FOUND,
          "currency", currency));
      return currency;
    }).replaceSuggestions(ArgumentSuggestions.strings(info -> StorageManager.getActiveStorageType().getCurrencies().toArray(new String[0])));
  }
  
  public static Argument<OfflinePlayer> TargetArgument(boolean mustBeOnline) {
    return new CustomArgument<>(new OfflinePlayerArgument("target"), info -> {
      OfflinePlayer target = info.currentInput();
      if (!QualityEconomyAPI.hasAccount(target.getUniqueId()))
        throw CustomArgument.CustomArgumentException.fromAdventureComponent(Messages.getParsedMessage(MessageType.PLAYER_NOT_FOUND,
          "player", target.getName()));
      if (mustBeOnline && !target.isOnline())
        throw CustomArgument.CustomArgumentException.fromAdventureComponent(Messages.getParsedMessage(MessageType.PLAYER_NOT_ONLINE,
          "player", target.getName()));
      return target;
    }).replaceSuggestions(ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)));
  }
  
  public static Argument<Double> AmountArgument() {
    return new CustomArgument<>(new StringArgument("amount"), info -> {
      String rawAmount = info.input();
      double amount;
      try {
        amount = Number.unformat(rawAmount);
      } catch (NumberFormatException exception) {
        throw CustomArgument.CustomArgumentException.fromAdventureComponent(Messages.getParsedMessage(MessageType.INVALID_NUMBER,
          "amount", rawAmount));
      }
      return Number.round(amount);
    }).replaceSuggestions(ArgumentSuggestions.strings("<amount>")
    );
  }
  
}
