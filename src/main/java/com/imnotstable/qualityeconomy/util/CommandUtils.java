package com.imnotstable.qualityeconomy.util;

import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.regex.Pattern;

public class CommandUtils {
  
  private static final Pattern FORMATTED_NUMBER_PATTERN = Pattern.compile("\\d{1,3}+[a-zA-Z]");
  
  public static boolean requirement(boolean requirement, MessageType errorMessage, CommandSender sender) {
    if (!requirement) {
      Messages.sendParsedMessage(sender, errorMessage);
      return true;
    }
    return false;
  }
  
  public static double parseNumber(String rawNumber) throws NumberFormatException {
    if (FORMATTED_NUMBER_PATTERN.matcher(rawNumber).matches())
      return Number.round(Number.unformatSuffix(rawNumber));
    return Double.parseDouble(rawNumber);
  }
  
  public static ArgumentSuggestions<CommandSender> getOnlinePlayerSuggestion() {
    return ArgumentSuggestions.stringCollection(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
  }
  
  public static ArgumentSuggestions<CommandSender> getOfflinePlayerSuggestion() {
    return ArgumentSuggestions.stringCollection(info -> Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList());
  }
  
}
