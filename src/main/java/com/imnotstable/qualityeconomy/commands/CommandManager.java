package com.imnotstable.qualityeconomy.commands;

public class CommandManager {
  
  public static void registerCommands() {
    BalanceCommand.register();
    BalanceTopCommand.register();
    CustomBalanceCommand.register();
    CustomEconomyCommand.register();
    EconomyCommand.register();
    MainCommand.register();
    PayCommand.register();
    WithdrawCommand.register();
  }
  
  public static void unregisterCommands() {
    BalanceCommand.unregister();
    BalanceTopCommand.unregister();
    CustomEconomyCommand.unregister();
    CustomBalanceCommand.unregister();
    EconomyCommand.unregister();
    MainCommand.unregister();
    PayCommand.unregister();
    WithdrawCommand.unregister();
  }
  
}
