package com.imnotstable.qualityeconomy.commands;

public interface Command {
  
  String getName();
  
  void register();
  
  void unregister();
  
}
