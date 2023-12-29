package com.imnotstable.qualityeconomy.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Debug {
  
  public static boolean DEBUG_MODE = false;
  
  public static class Timer {
    
    private static int incrementer = 0;
    private int id;
    private long start;
    private String message;
    
    public Timer(String message) {
      if (!DEBUG_MODE)
        return;
      incrementer++;
      id = incrementer;
      start = System.nanoTime();
      this.message = message;
      Logger.log(Component.text().append(
          Component.text(String.format("[#%d] ", id), NamedTextColor.DARK_GRAY),
          Component.text(message, NamedTextColor.GRAY))
        .build()
      );
    }
    
    public void interrupt() {
      if (!DEBUG_MODE)
        return;
      long now = System.nanoTime();
      Logger.log(Component.text().append(
        Component.text(String.format("[#%d] ", id), NamedTextColor.DARK_RED),
        Component.text(String.format("%s {%fms}", message, (now - start) / 1000000.0), NamedTextColor.RED)
      ).build());
    }
    
    public void progress() {
      if (!DEBUG_MODE)
        return;
      long now = System.nanoTime();
      Logger.log(Component.text().append(
          Component.text(String.format("[#%d] ", id), NamedTextColor.GOLD),
          Component.text(String.format("%s {%fms}", message, (now - start) / 1000000.0), NamedTextColor.YELLOW))
        .build()
      );
    }
    
    public void end() {
      if (!DEBUG_MODE)
        return;
      long now = System.nanoTime();
      Logger.log(Component.text().append(
          Component.text(String.format("[#%d] ", id), NamedTextColor.DARK_GREEN),
          Component.text(String.format("%s {%fms}", message, (now - start) / 1000000.0), NamedTextColor.GREEN))
        .build()
      );
    }
    
  }
  
  public static class QualityLogger {
    
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
  
  public static class QualityError {
    
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
        exception.printStackTrace();
        Logger.nl();
      } else if (extraInformation != null) {
        Logger.log(Component.text("Extra Information: " + extraInformation, NamedTextColor.RED));
        Logger.nl();
      }
    }
    
  }
}
