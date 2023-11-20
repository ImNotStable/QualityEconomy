package com.imnotstable.qualityeconomy.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class QualityLogger {
  
  private static final Component title = Component.text("QualityEconomy", NamedTextColor.DARK_GREEN);
  private final Component message;
  private String extraMessage;
  
  public QualityLogger(String message, String extraMessage) {
    this.message = Component.text(message);
    this.extraMessage = extraMessage;
  }
  
  public QualityLogger(String message) {
    this.message = Component.text(message);
  }
  
  public void log() {
    Logger.nl();
    Logger.log(title);
    Logger.log(message);
    Logger.nl();
    if (extraMessage != null) {
      Logger.log(Component.text("Extra Information: " + extraMessage, NamedTextColor.RED));
      Logger.nl();
    }
  }
  
}