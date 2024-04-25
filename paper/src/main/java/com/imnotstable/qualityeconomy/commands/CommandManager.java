package com.imnotstable.qualityeconomy.commands;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class CommandManager {
  
  private static final Map<String, BaseCommand> commands = Map.of(
    "balancetop", new BalanceTopCommand(),
    "qualityeconomy", new MainCommand()
  );
  
  public static void registerCommands() {
    commands.values().forEach(BaseCommand::register);
  }
  
  public static void unregisterCommands() {
    commands.values().forEach(BaseCommand::unregister);
  }
  
  public static Set<String> getCommandNames() {
    return commands.keySet();
  }
  
  public static @NotNull BaseCommand getCommand(@NotNull String command) {
    return commands.getOrDefault(command, new BaseCommand() {
      @Override
      public void register() {
        throw new UnsupportedOperationException("This command does not exist");
      }
      
      @Override
      public void unregister() {
        throw new UnsupportedOperationException("This command does not exist");
      }
    });
  }
  
}
