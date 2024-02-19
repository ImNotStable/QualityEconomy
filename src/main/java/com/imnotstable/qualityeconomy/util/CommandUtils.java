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
    return FORMATTED_NUMBER_PATTERN.matcher(rawNumber).matches() ? Number.round(Number.unformatSuffix(rawNumber)) : Double.parseDouble(rawNumber);
  }
  
  public static ArgumentSuggestions<CommandSender> getOnlinePlayerSuggestion() {
    return ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new));
  }
  
  public static ArgumentSuggestions<CommandSender> getOfflinePlayerSuggestion() {
    return ArgumentSuggestions.strings(info -> Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toArray(String[]::new));
  }
  
}
