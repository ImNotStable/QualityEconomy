package com.imnotstable.qualityeconomy.util;

import com.imnotstable.qualityeconomy.QualityEconomy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class Misc {
  
  private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
  
  public static boolean isUUID(String uuid) {
    return UUID_PATTERN.matcher(uuid).matches();
  }
  
  public static boolean equals(Object object, Object... comparable) {
    for (Object o : comparable)
      if (object.equals(o)) return true;
    return false;
  }
  
  public static void runAsync(Runnable runnable) {
    Bukkit.getScheduler().runTaskAsynchronously(QualityEconomy.getInstance(), runnable);
  }
  
  public static void sendColoredMessage(@NotNull CommandSender receiver, @NotNull String message) {
    receiver.sendMessage(colored(message));
  }
  
  public static String colored(@NotNull String message) {
    return ChatColor.translateAlternateColorCodes('&', message);
  }
  
}
