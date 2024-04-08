package com.imnotstable.qualityeconomy.util;

import org.bukkit.Bukkit;

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
      Debug.Logger.log(String.format("&8[#%d] &7%s", id, message));
    }
    
    public void interrupt() {
      if (!DEBUG_MODE)
        return;
      long now = System.nanoTime();
      Debug.Logger.log(String.format("&4[#%d] &c%s {%fms}", id, message, (now - start) / 1000000.0));
    }
    
    public void progress() {
      if (!DEBUG_MODE)
        return;
      long now = System.nanoTime();
      Debug.Logger.log(String.format("&6[#%d] &e%s {%fms}", id, message, (now - start) / 1000000.0));
    }
    
    public void end() {
      if (!DEBUG_MODE)
        return;
      long now = System.nanoTime();
      Debug.Logger.log(String.format("&2[#%d] &a%s {%fms}", id, message, (now - start) / 1000000.0));
    }
    
  }
  
  public static class QualityLogger {
    
    private static final String title = "&2QualityEconomy";
    private final String[] messages;
    
    public QualityLogger(String... messages) {
      this.messages = messages;
    }
    
    public QualityLogger(String message) {
      messages = new String[]{message};
    }
    
    public void log() {
      Logger.nl();
      Debug.Logger.log(title);
      Debug.Logger.log(messages);
      Logger.nl();
    }
    
  }
  
  public static class QualityError {
    
    private static final String title = "&4QualityEconomy Error";
    private final String informativeMessage;
    private Exception exception;
    private String extraInformation;
    
    public QualityError(String informativeMessage, Exception exception) {
      this.informativeMessage = informativeMessage;
      this.exception = exception;
    }
    
    public QualityError(String informativeMessage, String extraInformation) {
      this.informativeMessage = informativeMessage;
      this.extraInformation = extraInformation;
    }
    
    public QualityError(String informativeMessage) {
      this.informativeMessage = informativeMessage;
    }
    
    public void log() {
      Logger.nl();
      Debug.Logger.log(title);
      Debug.Logger.log(informativeMessage);
      Logger.nl();
      if (exception != null && exception.getMessage() != null) {
        Debug.Logger.log("&cException: " + exception.getMessage());
        exception.printStackTrace();
        Logger.nl();
      } else if (extraInformation != null) {
        Debug.Logger.log("&cExtra Information: " + extraInformation);
        Logger.nl();
      }
    }
    
  }
  
  public static class Logger {
    
    public static void log(String... messages) {
      for (String message : messages)
        Bukkit.getConsoleSender().sendMessage(Misc.colored("[QualityEconomy] " + message));
    }
    
    public static void log(String message) {
      Bukkit.getConsoleSender().sendMessage("[QualityEconomy] " + Misc.colored(message));
    }
    
    public static void nl() {
      log("");
    }
  }
}
