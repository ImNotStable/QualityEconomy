package com.imnotstable.qualityeconomy.storage.accounts;

import com.imnotstable.qualityeconomy.util.Number;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Account {
  
  private final UUID uuid;
  private String name;
  private double balance = 0.0;
  private final Map<String, Double> otherBalances = new HashMap<>();
  private boolean isPayable = true;
  
  public Account(UUID uuid) {
    this.uuid = uuid;
  }
  
  public UUID getUUID() {
    return uuid;
  }
  
  public String getName() {
    return name;
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
  
  public Account setCustomBalance(String currency, double balance) {
    otherBalances.put(currency, Number.round(balance));
    return this;
  }
  
  public Account setCustomBalances(Map<String, Double> balanceMap) {
    balanceMap.replaceAll((currency, balance) -> Number.round(balance));
    otherBalances.putAll(balanceMap);
    return this;
  }
  
  public boolean getPayable() {
    return isPayable;
  }
  
  public Account setPayable(boolean payable) {
    this.isPayable = payable;
    return this;
  }
  
}
