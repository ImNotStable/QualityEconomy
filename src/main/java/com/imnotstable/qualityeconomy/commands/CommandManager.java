package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.banknotes.BankNotes;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;

public class CommandManager {
  
  public static void loadCommands() {
    if (Configuration.isBalanceCommandEnabled()) BalanceCommand.loadCommand();
    if (Configuration.isBalancetopCommandEnabled()) BalanceTopCommand.loadCommand();
    if (Configuration.isEconomyCommandEnabled()) EconomyCommand.loadCommand();
    if (Configuration.isPayCommandEnabled()) PayCommand.loadCommand();
    if (Configuration.areBanknotesEnabled()) BankNotes.loadCommand();
    if (!CustomCurrencies.getCustomCurrencies().isEmpty()) CustomCurrencies.loadCommands();
  }
  
  public static void unloadCommands() {
    BalanceCommand.unloadCommand();
    BalanceTopCommand.unloadCommand();
    EconomyCommand.unloadCommand();
    PayCommand.unloadCommand();
    BankNotes.unloadCommand();
    CustomCurrencies.unloadCommands();
  }
  
}
