package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.QualityEconomy;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class BalanceEntry {
  
  private final String currency;
  private double balance;
  private boolean payable;
  
  public BalanceEntry(@NotNull String currency, @Nullable Double balance, @Nullable Boolean payable) {
    this.currency = currency;
    if (balance != null)
      this.balance = balance;
    else
      this.balance = QualityEconomy.getCurrencyConfig().getStartingBalance(currency);
    if (payable != null)
      this.payable = payable;
    else
      this.payable = true;
  }
  
  public BalanceEntry setBalance(double balance) {
    this.balance = balance;
    return this;
  }
  
  public BalanceEntry increaseBalance(double amount) {
    balance += amount;
    return this;
  }
  
  public BalanceEntry decreaseBalance(double amount) {
    balance -= amount;
    return this;
  }
  
  public BalanceEntry setPayable(boolean payable) {
    this.payable = payable;
    return this;
  }
  
}
