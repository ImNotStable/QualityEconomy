package com.imnotstable.qualityeconomy.util.debug;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

public class Logger {
  
  public static void log(Component... messages) {
    for (Component message : messages)
      log(message);
  }
  
  public static void log(Component message) {
    Bukkit.getConsoleSender().sendMessage(Component.text().content("[QualityEconomy] ").append(message).build());
  }
  
  public static void log(String... messages) {
    for (String message : messages)
      log(message);
  }
  
  public static void log(String message) {
    log(Component.text(message));
  }
  
  public static void nl() {
    log(Component.empty());
  }
  
  public static void logError(String message) {
    logError(message, null, null);
  }
  
  public static void logError(String message, String otherMessage) {
    logError(message, otherMessage, null);
  }
  
  public static void logError(String message, Throwable throwable) {
    logError(message, null, throwable);
  }
  
  public static void logError(String message, String otherMessage, Throwable throwable) {
    nl();
    log(Component.text("QualityEconomy Error", NamedTextColor.DARK_RED));
    log(Component.text(message));
    if (otherMessage != null && !otherMessage.isBlank()) {
      log(otherMessage);
    }
    nl();
    if (throwable != null) {
      log(Component.text("Exception: " + throwable.getMessage(), NamedTextColor.RED));
      throwable.printStackTrace();
      nl();
    }
  }
  
  
}
