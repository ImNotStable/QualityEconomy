package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.banknotes.BankNotes;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;

public class CommandManager {
  
  public static void registerCommands() {
    if (Configuration.isBalanceCommandEnabled()) BalanceCommand.register();
    if (Configuration.isBalancetopCommandEnabled()) BalanceTopCommand.register();
    if (Configuration.isEconomyCommandEnabled()) EconomyCommand.register();
    if (Configuration.isPayCommandEnabled()) PayCommand.register();
    if (Configuration.areBanknotesEnabled()) BankNotes.register();
    if (!CustomCurrencies.getCustomCurrencies().isEmpty()) {
      CustomEconomyCommand.register();
      CustomBalanceCommand.register();
    }
  }
  
  public static void unregisterCommands() {
    BalanceCommand.unregister();
    BalanceTopCommand.unregister();
    EconomyCommand.unregister();
    PayCommand.unregister();
    BankNotes.unregister();
    CustomEconomyCommand.unregister();
    CustomBalanceCommand.unregister();
  }
  
}
