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
  private final Map<String, BalanceEntry> balances;
  @Getter
  private String username = "";
  private Boolean requiresUpdate = null;
  
  public Account(UUID uniqueId) {
    this.uniqueId = uniqueId;
    this.balances = new HashMap<>();
  }
  
  protected Account(Account account) {
    this.uniqueId = account.uniqueId;
    this.username = account.username;
    this.balances = account.balances;
    this.requiresUpdate = account.requiresUpdate;
  }
  
  public Account setUsername(@NotNull String username) {
    this.username = username;
    this.requiresUpdate = true;
    return this;
  }
  
  public Account setDefaultBalance(double balance) {
    balances.get("default").setBalance(balance);
    this.requiresUpdate = true;
    return this;
  }
  
  public double getDefaultBalance() {
    return getBalance("default");
  }
  
  public double getBalance(@NotNull String currency) {
    return getBalanceEntry(currency).getBalance();
  }
  
  public BalanceEntry getBalanceEntry(@NotNull String currency) {
    if (!balances.containsKey(currency)) {
      if (QualityEconomy.getCurrencyConfig().getCurrency(currency) == null)
        throw new IllegalArgumentException("Currency " + currency + " does not exist");
      return new BalanceEntry(currency, QualityEconomy.getCurrencyConfig().getDefaultBalance(currency), true);
    }
    return balances.get(currency);
  }
  
  public Map<String, BalanceEntry> getBalances() {
    return new HashMap<>(balances);
  }
  
  public Account setBalances(Collection<BalanceEntry> balanceEntries) {
    balanceEntries.forEach(this::setBalance);
    this.requiresUpdate = true;
    return this;
  }
  
  public Account setBalances(@NotNull Map<String, BalanceEntry> balanceMap) {
    balances.putAll(balanceMap);
    this.requiresUpdate = true;
    return this;
  }
  
  public Account setBalance(BalanceEntry balance) {
    balances.put(balance.getCurrency(), balance);
    this.requiresUpdate = true;
    return this;
  }
  
  public boolean requiresUpdate() {
    return requiresUpdate != null;
  }
  
  public Account update() {
    requiresUpdate = null;
    return this;
  }
  
}
