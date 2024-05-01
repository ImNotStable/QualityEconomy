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
    logError(message, null, null, false);
  }
  
  public static void logError(String message, String otherMessage) {
    logError(message, otherMessage, null, false);
  }
  
  public static void logError(String message, Throwable throwable) {
    logError(message, null, throwable, true);
  }
  
  public static void logError(String message, Throwable throwable, boolean printStackTrace) {
    logError(message, null, throwable, printStackTrace);
  }
  
  public static void logError(String message, String otherMessage, Throwable throwable, boolean printStackTrace) {
    nl();
    log(Component.text("QualityEconomy Error", NamedTextColor.DARK_RED));
    log(Component.text(message));
    if (otherMessage != null && !otherMessage.isBlank()) {
      log(otherMessage);
    }
    nl();
    if (throwable != null) {
      log(Component.text("Exception: " + throwable.getMessage(), NamedTextColor.RED));
      if (printStackTrace)
        throwable.printStackTrace();
      nl();
    }
  }
  
  
}
