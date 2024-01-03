package com.imnotstable.qualityeconomy.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public class Logger {
  
  public static void log(Component[] messages) {
    for (Component message : messages)
      log(message);
  }
  
  public static void log(Component message) {
    Bukkit.getConsoleSender().sendMessage(Component.text().content("[QualityEconomy] ").append(message).build());
  }
  
  public static void log(String message) {
    log(Component.text(message));
  }
  
  public static void nl() {
    log(Component.empty());
  }
}
