package com.imnotstable.qualityeconomy.util;

import dev.jorel.commandapi.SuggestionInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Misc {
  
  public static String[] getOfflinePlayerSuggestion(SuggestionInfo<CommandSender> ignored) {
    return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList().toArray(new String[0]);
  }
  
}
