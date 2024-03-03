package com.imnotstable.qualityeconomy.util;

import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
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
  
  public static ArgumentSuggestions<CommandSender> getOnlinePlayerSuggestion() {
    return ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new));
  }
  
  public static Argument<Double> CurrencyAmountArgument() {
    
    return new CustomArgument<>(new StringArgument("amount"), info -> {
      String rawAmount = info.input();
      double amount;
      
      try {
        amount = Number.unformat(rawAmount);
      } catch (NumberFormatException exception) {
        throw CustomArgument.CustomArgumentException.fromAdventureComponent(Messages.getParsedMessage(MessageType.INVALID_NUMBER, rawAmount));
      }
      
      return Number.round(amount);
      
    }).replaceSuggestions(ArgumentSuggestions.strings(info -> new String[]{"<amount>"})
    );
  }
  
}
