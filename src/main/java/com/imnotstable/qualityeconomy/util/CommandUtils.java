package com.imnotstable.qualityeconomy.util;

import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import dev.jorel.commandapi.SuggestionInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUtils {
  
  public static boolean requirement(boolean requirement, MessageType errorMessage, CommandSender sender) {
    if (!requirement) {
      Messages.sendParsedMessage(errorMessage, sender);
      return true;
    }
    return false;
  }
  
  public static String[] getOfflinePlayerSuggestion(SuggestionInfo<CommandSender> ignored) {
    return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList().toArray(new String[0]);
  }

}
