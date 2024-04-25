package com.imnotstable.qualityeconomy.storage.accounts;

import com.imnotstable.qualityeconomy.QualityEconomy;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Setter
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
      this.balance = QualityEconomy.getCurrencyConfig().getDefaultBalance(currency);
    if (payable != null)
      this.payable = payable;
    else
      this.payable = true;
  }
  
  public void increaseBalance(double amount) {
    balance += amount;
  }
  
  public void decreaseBalance(double amount) {
    balance -= amount;
  }
  
}
