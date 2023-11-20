package com.imnotstable.qualityeconomy.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TestToolkit {
  
  public static boolean DEBUG_MODE = false;
  
  public static class Timer {
    
    private static long incrementer = 0;
    private long id;
    private long start;
    private String message;
    
    public Timer(String message) {
      if (!DEBUG_MODE)
        return;
      incrementer++;
      id = incrementer;
      start = System.currentTimeMillis();
      this.message = message;
      Logger.log(Component.text()
        .append(Component.text(String.format("[#%d] ", id), NamedTextColor.DARK_GRAY))
        .append(Component.text(message, NamedTextColor.GRAY))
        .build()
      );
    }
    
    public void interrupt(String message) {
      if (!DEBUG_MODE)
        return;
      long now = System.currentTimeMillis();
      Logger.log(Component.text()
        .append(Component.text(String.format("[#%d] ", id), NamedTextColor.DARK_RED))
        .append(Component.text(String.format("%s (%dms)", message, (now - start)), NamedTextColor.RED))
        .build()
      );
    }
    
    public void progress() {
      if (!DEBUG_MODE)
        return;
      long now = System.currentTimeMillis();
      Logger.log(Component.text()
        .append(Component.text(String.format("[#%d] ", id), NamedTextColor.GOLD))
        .append(Component.text(String.format("%s (%dms)", message, (now - start)), NamedTextColor.YELLOW))
        .build()
      );
    }
    
    public void end(String message) {
      if (!DEBUG_MODE)
        return;
      long now = System.currentTimeMillis();
      Logger.log(Component.text()
        .append(Component.text(String.format("[#%d] ", id), NamedTextColor.DARK_GREEN))
        .append(Component.text(String.format("%s (%dms)", message, (now - start)), NamedTextColor.GREEN))
        .build()
      );
    }
    
  }
  
}
