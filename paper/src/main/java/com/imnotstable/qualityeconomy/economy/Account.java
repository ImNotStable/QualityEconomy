package com.imnotstable.qualityeconomy.economy;

import com.google.common.base.Preconditions;
import com.imnotstable.qualityeconomy.QualityEconomy;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApiStatus.Internal
public class Account {
  @Getter
  private final UUID uniqueId;
  private final Map<String, BalanceEntry> balances;
  @Getter
  private String username = "";
  
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
  
  public double getBalance(@NotNull String currency) {
    return getBalanceEntry(currency).getBalance();
  }
  
  public BalanceEntry getBalanceEntry(@NotNull String currency) {
    Preconditions.checkArgument(QualityEconomy.getCurrencyConfig().getCurrency(currency).isPresent(), "Currency " + currency + " does not exist");
    if (!balances.containsKey(currency)) {
      BalanceEntry balanceEntry = new BalanceEntry(currency, QualityEconomy.getCurrencyConfig().getStartingBalance(currency), true);
      balances.put(currency, balanceEntry);
      return balanceEntry;
    }
    return balances.get(currency);
  }
  
  public Account updateBalanceEntry(BalanceEntry balanceEntry) {
    balances.put(balanceEntry.getCurrency(), balanceEntry);
    return this;
  }
  
  public Collection<BalanceEntry> getBalanceEntries() {
    return balances.values();
  }
  
  public Account updateBalanceEntries(Collection<BalanceEntry> balanceEntries) {
    balanceEntries.forEach(this::updateBalanceEntry);
    return this;
  }
  
}
