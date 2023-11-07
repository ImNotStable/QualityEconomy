package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.banknotes.BankNotes;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;

public class CommandManager {
  
  public static void loadCommands() {
    if (Configuration.COMMAND_BALANCE) BalanceCommand.loadCommand();
    if (Configuration.COMMAND_BALANCETOP) BalanceTopCommand.loadCommand();
    if (Configuration.COMMAND_ECONOMY) EconomyCommand.loadCommand();
    if (Configuration.COMMAND_PAY) PayCommand.loadCommand();
    if (Configuration.BANKNOTES) BankNotes.loadCommand();
    if (!CustomCurrencies.getCustomCurrencies().isEmpty()) CustomCurrencies.loadCommand();
  }
  
  public static void unloadCommands() {
    BalanceCommand.unloadCommand();
    BalanceTopCommand.unloadCommand();
    EconomyCommand.unloadCommand();
    PayCommand.unloadCommand();
    BankNotes.unloadCommand();
    CustomCurrencies.unloadCommand();
  }
  
}
