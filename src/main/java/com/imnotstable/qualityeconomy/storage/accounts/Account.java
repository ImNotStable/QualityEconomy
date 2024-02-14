package com.imnotstable.qualityeconomy.storage.accounts;

import com.imnotstable.qualityeconomy.util.Number;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Account {
  @Getter
  private final UUID uniqueId;
  private final Map<String, Double> otherBalances = new HashMap<>();
  @Getter
  private String username = "";
  private double balance = 0.0;
  @Getter
  private boolean isPayable = true;
  @Getter
  private boolean isRequestable = false;
  
  public Account(UUID uniqueId) {
    this.uniqueId = uniqueId;
  }
  
  public Account setUsername(@NotNull String username) {
    this.username = username;
    return this;
  }
  
  public double getBalance() {
    return Number.round(balance);
  }
  
  public Account setBalance(double balance) {
    this.balance = Number.round(balance);
    return this;
  }
  
  public double getCustomBalance(@NotNull String currency) {
    return otherBalances.getOrDefault(currency, 0.0);
  }
  
  public Map<String, Double> getCustomBalances() {
    return otherBalances;
  }
  
  public Account setCustomBalances(@NotNull Map<String, Double> balanceMap) {
    balanceMap.forEach(this::setCustomBalance);
    return this;
  }
  
  public Account setCustomBalance(@NotNull String currency, double balance) {
    otherBalances.put(currency, Number.round(balance));
    return this;
  }
  
  public Account setPayable(boolean isPayable) {
    this.isPayable = isPayable;
    return this;
  }
  
  public Account setRequestable(boolean isRequestable) {
    this.isRequestable = isRequestable;
    return this;
  }
  
}
