package com.imnotstable.qualityeconomy.util;

import dev.jorel.commandapi.SuggestionInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class Misc {
  
  private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
  
  public static String[] getOfflinePlayerSuggestion(SuggestionInfo<CommandSender> ignored) {
    return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList().toArray(new String[0]);
  }
  
  public static boolean isValidUUID(String uuid) {
    return UUID_PATTERN.matcher(uuid).matches();
  }
  
}
