package com.imnotstable.qualityeconomy.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class QualityLogger {
  
  private static final Component title = Component.text("QualityEconomy", NamedTextColor.DARK_GREEN);
  private final Component[] messages;
  
  public QualityLogger(String... messages) {
    this.messages = new Component[messages.length];
    for (int i = 0; i < messages.length; i++)
      this.messages[i] = Component.text(messages[i]);
  }
  
  public QualityLogger(String message) {
    messages = new Component[]{Component.text(message)};
  }
  
  public void log() {
    Logger.nl();
    Logger.log(title);
    Logger.log(messages);
    Logger.nl();
  }
  
}