package com.imnotstable.qualityeconomy.commands;

public abstract class AbstractCommand {
  
  public abstract String getName();
  
  public abstract void register();
  
  public abstract void unregister();
  
}
