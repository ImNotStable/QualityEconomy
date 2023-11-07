package com.imnotstable.qualityeconomy.storage;

import com.imnotstable.qualityeconomy.util.Number;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Account {
  
  private final UUID uuid;
  private String name;
  private double balance = 0D;
  private final Map<String, Double> otherBalances = new HashMap<>();
  private boolean payable = true;
  
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
  
  public Account setCustomBalance(String currency, double balance) {
    otherBalances.put(currency, balance);
    return this;
  }
  
  public Account setCustomBalances(Map<String, Double> balanceMap) {
    otherBalances.putAll(balanceMap);
    return this;
  }
  
  public boolean getPayable() {
    return payable;
  }
  
  public Account setPayable(boolean payable) {
    this.payable = payable;
    return this;
  }
  
}
