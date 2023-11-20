package com.imnotstable.qualityeconomy.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class QualityError {
  
  private static final Component title = Component.text("QualityEconomy Error", NamedTextColor.DARK_RED);
  private final Component informativeMessage;
  private Exception exception;
  private String extraInformation;
  
  public QualityError(String informativeMessage, Exception exception) {
    this.informativeMessage = Component.text(informativeMessage);
    this.exception = exception;
  }
  
  public QualityError(String informativeMessage, String extraInformation) {
    this.informativeMessage = Component.text(informativeMessage);
    this.extraInformation = extraInformation;
  }
  
  public QualityError(String informativeMessage) {
    this.informativeMessage = Component.text(informativeMessage);
  }
  
  public void log() {
    Logger.nl();
    Logger.log(title);
    Logger.log(informativeMessage);
    Logger.nl();
    if (exception != null) {
      Logger.log(Component.text("Exception: " + exception.getMessage(), NamedTextColor.RED));
      Logger.nl();
    } else if (extraInformation != null) {
      Logger.log(Component.text("Extra Information: " + extraInformation, NamedTextColor.RED));
      Logger.nl();
    }
  }
  
}
