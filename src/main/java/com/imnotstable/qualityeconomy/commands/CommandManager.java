package com.imnotstable.qualityeconomy.commands;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class CommandManager {
  
  private static final Map<String, BaseCommand> commands = Map.of(
    "balance", new BalanceCommand(),
    "balancetop", new BalanceTopCommand(),
    "custombalance", new CustomBalanceCommand(),
    "customeconomy", new CustomEconomyCommand(),
    "economy", new EconomyCommand(),
    "qualityeconomy", new MainCommand(),
    "pay", new PayCommand(),
    "request", new RequestCommand(),
    "withdraw", new WithdrawCommand()
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
  
  public static @Nullable BaseCommand getCommand(String command) {
    return commands.get(command);
  }
  
}
