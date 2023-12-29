package com.imnotstable.qualityeconomy.commands;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CommandManager {
  
  private static final Map<String, Command> commands = Map.of(
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
    commands.values().forEach(Command::register);
  }
  
  public static void unregisterCommands() {
    commands.values().forEach(Command::unregister);
  }
  
  public static @Nullable Command getCommand(String command) {
    return commands.get(command);
  }
  
}
