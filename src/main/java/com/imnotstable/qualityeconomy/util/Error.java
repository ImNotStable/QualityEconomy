package com.imnotstable.qualityeconomy.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class Error {
  
  private final Component informativeMessage;
  private Component title = Component.text("QualityEconomy Error", NamedTextColor.DARK_RED);
  private Exception exception;
  private String extraInformation;
  
  public Error(String informativeMessage, Exception exception) {
    this.informativeMessage = Component.text(informativeMessage);
    this.exception = exception;
  }
  
  public Error(String informativeMessage, String extraInformation) {
    this.informativeMessage = Component.text(informativeMessage);
    this.extraInformation = extraInformation;
  }
  
  public Error(String informativeMessage) {
    this.informativeMessage = Component.text(informativeMessage);
  }
  
  public Error title(Component title) {
    this.title = title;
    return this;
  }
  
  public Error title(String title, TextColor color) {
    this.title = Component.text(title, color);
    return this;
  }
  
  public Error title(String title) {
    return title(title, NamedTextColor.DARK_RED);
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
      Logger.log(Component.text("Extra Information: " + exception.getMessage(), NamedTextColor.RED));
      Logger.nl();
    }
  }
  
}
