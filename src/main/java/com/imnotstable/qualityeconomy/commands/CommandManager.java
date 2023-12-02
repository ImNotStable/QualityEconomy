package com.imnotstable.qualityeconomy.commands;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CommandManager {
  
  private static Map<String, AbstractCommand> commands = Map.of(
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
    commands.values().forEach(AbstractCommand::register);
  }
  
  public static void unregisterCommands() {
    commands.values().forEach(AbstractCommand::unregister);
  }
  
  public static @Nullable AbstractCommand getCommand(String commandName) {
    return commands.get(commandName);
  }
  
}
