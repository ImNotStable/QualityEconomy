package com.imnotstable.qualityeconomy.storage;

import java.util.UUID;

public class Account {
  
  private final UUID uuid;
  private String name;
  private double balance = 0D;
  private double secondaryBalance = 0D;
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
    return balance;
  }
  
  public Account setBalance(double balance) {
    this.balance = balance;
    return this;
  }
  
  public double getSecondaryBalance() {
    return secondaryBalance;
  }
  
  public Account setSecondaryBalance(double secondaryBalance) {
    this.secondaryBalance = secondaryBalance;
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
