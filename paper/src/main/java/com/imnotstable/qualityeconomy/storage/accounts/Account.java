package com.imnotstable.qualityeconomy.storage.accounts;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Account {
  @Getter
  private final UUID uniqueId;
  private final Map<String, Double> otherBalances;
  @Getter
  private String username = "";
  @Getter
  private double balance = 0.0;
  @Getter
  private boolean isPayable = true;
  @Getter
  private boolean isRequestable = false;
  private Boolean requiresUpdate = null;
  
  public Account(UUID uniqueId) {
    this.uniqueId = uniqueId;
    this.otherBalances = new HashMap<>();
  }
  
  protected Account(Account account) {
    this.uniqueId = account.uniqueId;
    this.username = account.username;
    this.balance = account.balance;
    this.otherBalances = account.otherBalances;
    this.isPayable = account.isPayable;
    this.isRequestable = account.isRequestable;
    this.requiresUpdate = account.requiresUpdate;
  }
  
  public Account setUsername(@NotNull String username) {
    this.username = username;
    this.requiresUpdate = true;
    return this;
  }
  
  public Account setBalance(double balance) {
    this.balance = balance;
    this.requiresUpdate = true;
    return this;
  }
  
  public double getCustomBalance(@NotNull String currency) {
    return otherBalances.getOrDefault(currency, 0.0);
  }
  
  public Map<String, Double> getCustomBalances() {
    return new HashMap<>(otherBalances);
  }
  
  public Account setCustomBalances(@NotNull Map<String, Double> balanceMap) {
    otherBalances.putAll(balanceMap);
    this.requiresUpdate = true;
    return this;
  }
  
  public Account setCustomBalance(@NotNull String currency, double balance) {
    otherBalances.put(currency, balance);
    this.requiresUpdate = true;
    return this;
  }
  
  public Account setPayable(boolean isPayable) {
    this.isPayable = isPayable;
    this.requiresUpdate = true;
    return this;
  }
  
  public Account setRequestable(boolean isRequestable) {
    this.isRequestable = isRequestable;
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
