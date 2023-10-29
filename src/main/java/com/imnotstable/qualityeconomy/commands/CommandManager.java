package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.configuration.Configuration;

public class CommandManager {
  
  public static void loadCommands() {
    MainCommand.loadCommand();
    if (Configuration.COMMAND_BALANCE) BalanceCommand.loadCommand();
    if (Configuration.COMMAND_BALANCETOP) BalanceTopCommand.loadCommand();
    if (Configuration.COMMAND_ECONOMY) EconomyCommand.loadCommand();
    if (Configuration.COMMAND_PAY) PayCommand.loadCommand();
  }
  
  public static void unloadCommands() {
    MainCommand.unloadCommand();
    BalanceCommand.unloadCommand();
    BalanceTopCommand.unloadCommand();
    EconomyCommand.unloadCommand();
    PayCommand.unloadCommand();
  }
  
}
