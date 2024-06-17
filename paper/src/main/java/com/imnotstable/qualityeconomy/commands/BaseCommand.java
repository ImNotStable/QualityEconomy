package com.imnotstable.qualityeconomy.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;

public abstract class BaseCommand {
  
  private boolean isRegistered = false;
  
  public abstract void register();
  
  public abstract void unregister();
  
  protected boolean register(CommandTree command) {
    return register(command, command != null);
  }
  
  protected boolean register(CommandTree command, boolean conditions) {
    if (isRegistered || !conditions)
      return false;
    command.register("qualityeconomy");
    isRegistered = true;
    return true;
  }
  
  protected boolean unregister(CommandTree command) {
    if (!isRegistered || command == null)
      return false;
    CommandAPI.unregister(command.getName(), true);
    for (String alias : command.getAliases())
      CommandAPI.unregister(alias, true);
    isRegistered = false;
    return true;
  }
  
}
