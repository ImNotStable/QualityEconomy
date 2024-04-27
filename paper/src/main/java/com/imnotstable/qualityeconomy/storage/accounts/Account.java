package com.imnotstable.qualityeconomy.storage.accounts;

import com.imnotstable.qualityeconomy.QualityEconomy;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Account {
  @Getter
  private final UUID uniqueId;
  @Getter
  private String username = "";
  private final Map<String, BalanceEntry> balances;
  
  public Account(UUID uniqueId) {
    this.uniqueId = uniqueId;
    this.balances = new HashMap<>();
  }
  
  protected Account(Account account) {
    this.uniqueId = account.uniqueId;
    this.username = account.username;
    this.balances = account.balances;
  }
  
  public Account setUsername(@NotNull String username) {
    this.username = username;
    return this;
  }
  
  public double getDefaultBalance() {
    return getBalance("default");
  }
  
  public Account setDefaultBalance(double balance) {
    balances.get("default").setBalance(balance);
    return this;
  }
  
  public double getBalance(@NotNull String currency) {
    return getBalanceEntry(currency).getBalance();
  }
  
  public BalanceEntry getBalanceEntry(@NotNull String currency) {
    if (!balances.containsKey(currency)) {
      if (QualityEconomy.getCurrencyConfig().getCurrency(currency).isEmpty())
        throw new IllegalArgumentException("Currency " + currency + " does not exist");
      BalanceEntry balanceEntry = new BalanceEntry(currency, QualityEconomy.getCurrencyConfig().getDefaultBalance(currency), true);
      balances.put(currency, balanceEntry);
      return balanceEntry;
    }
    return balances.get(currency);
  }
  
  public Collection<BalanceEntry> getBalanceEntries() {
    return balances.values();
  }
  
  public Account updateBalanceEntry(BalanceEntry balance) {
    balances.put(balance.getCurrency(), balance);
    return this;
  }
  
  public Account updateBalanceEntries(Collection<BalanceEntry> balanceEntries) {
    balanceEntries.forEach(this::updateBalanceEntry);
    return this;
  }
  
}
