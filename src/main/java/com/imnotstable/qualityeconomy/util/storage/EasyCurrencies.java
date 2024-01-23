package com.imnotstable.qualityeconomy.util.storage;

import com.imnotstable.qualityeconomy.commands.CommandManager;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.util.Debug;

import java.util.ArrayList;
import java.util.List;

public class EasyCurrencies {
  
  protected final List<String> currencies = new ArrayList<>();
  
  protected List<String> getCurrencies() {
    if (!Configuration.areCustomCurrenciesEnabled()) {
      new Debug.QualityError("This feature is disabled within QualityEconomy's configuration").log();
      return new ArrayList<>();
    }
    return new ArrayList<>(currencies);
  }
  
  protected String addCurrencyAttempt(String currency) {
    currency = currency.toUpperCase();
    if (!Configuration.areCustomCurrenciesEnabled()) {
      new Debug.QualityError("This feature is disabled within QualityEconomy's configuration").log();
      return null;
    }
    if (List.of("UUID", "NAME", "BALANCE", "PAYABLE", "REQUESTABLE").contains(currency)) {
      new Debug.QualityError("Failed to create currency \"" + currency + "\"", "Name cannot be \"UUID\", \"NAME\", \"BALANCE\", \"PAYABLE\", \"REQUESTABLE\"").log();
      return null;
    }
    if (currencies.contains(currency)) {
      new Debug.QualityError("Failed to create currency \"" + currency + "\"", "Currency already exists").log();
      return null;
    }
    return currency;
  }
  
  protected void addCurrencySuccess(String currency) {
    currencies.add(currency);
    CommandManager.getCommand("custombalance").register();
    CommandManager.getCommand("customeconomy").register();
  }
  
  protected String removeCurrencyAttempt(String currency) {
    currency = currency.toUpperCase();
    if (!Configuration.areCustomCurrenciesEnabled()) {
      new Debug.QualityError("This feature is disabled within QualityEconomy's configuration").log();
      return null;
    }
    if (!currencies.contains(currency)) {
      new Debug.QualityError("Failed to delete currency \"" + currency + "\"", "Currency doesn't exist").log();
      return null;
    }
    return currency;
  }
  
  protected void removeCurrencySuccess(String currency) {
    currencies.remove(currency);
    if (currencies.isEmpty()) {
      CommandManager.getCommand("custombalance").unregister();
      CommandManager.getCommand("customeconomy").unregister();
    }
  }
  
}
