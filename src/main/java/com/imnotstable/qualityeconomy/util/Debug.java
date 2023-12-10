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
  
}
