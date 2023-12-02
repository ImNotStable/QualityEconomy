package com.imnotstable.qualityeconomy.storage.accounts;

import com.imnotstable.qualityeconomy.util.Number;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Account {
  
  private final UUID uuid;
  private final Map<String, Double> otherBalances = new HashMap<>();
  private @Getter String name = "";
  private double balance = 0.0;
  private @Getter boolean isPayable = true;
  private @Getter boolean isRequestable = false;
  
  public Account(UUID uuid) {
    this.uuid = uuid;
  }
  
  public UUID getUUID() {
    return uuid;
  }
  
  public Account setName(String name) {
    this.name = name;
    return this;
  }
  
  public double getBalance() {
    return Number.round(balance);
  }
  
  public Account setBalance(double balance) {
    this.balance = Number.round(balance);
    return this;
  }
  
  public double getCustomBalance(String currency) {
    return otherBalances.getOrDefault(currency, 0.0);
  }
  
  public Map<String, Double> getCustomBalances() {
    return otherBalances;
  }
  
  public Account setCustomBalances(Map<String, Double> balanceMap) {
    balanceMap.replaceAll((currency, balance) -> Number.round(balance));
    otherBalances.putAll(balanceMap);
    return this;
  }
  
  public Account setCustomBalance(String currency, double balance) {
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
