package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.configuration.Configuration;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;

public abstract class BaseCommand {
  
  private boolean isRegistered = false;
  
  public abstract void register();
  
  public abstract void unregister();
  
  protected boolean register(CommandTree command) {
    return register(command, true);
  }
  
  protected boolean register(CommandTree command, boolean conditions) {
    if (isRegistered || !Configuration.isCommandEnabled(command.getName()) || !conditions)
      return false;
    command.register();
    isRegistered = true;
    return true;
  }
  
  protected boolean unregister(CommandTree command) {
    if (!isRegistered)
      return false;
    CommandAPI.unregister(command.getName(), true);
    for (String alias : command.getAliases())
      CommandAPI.unregister(alias, true);
    isRegistered = false;
    return true;
  }
  
}
