package com.imnotstable.qualityeconomy.util;

import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CommandUtils {
  
  public static boolean requirement(boolean requirement, MessageType errorMessage, CommandSender sender) {
    if (!requirement) {
      Messages.sendParsedMessage(sender, errorMessage);
      return true;
    }
    return false;
  }
  
  public static ArgumentSuggestions<CommandSender> getOnlinePlayerSuggestion() {
    return ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList().toArray(new String[0]));
  }
  
  public static ArgumentSuggestions<CommandSender> getOfflinePlayerSuggestion() {
    return ArgumentSuggestions.strings(info -> Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList().toArray(new String[0]));
  }
  
}
