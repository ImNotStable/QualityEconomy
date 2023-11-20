package com.imnotstable.qualityeconomy.api;

import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;

import java.util.UUID;

@SuppressWarnings("unused")
public class QualityEconomyAPI {
  
  public static boolean hasAccount(UUID uuid) {
    return AccountManager.accountExists(uuid);
  }
  
  public static double getBalance(UUID uuid) {
    return AccountManager.getAccount(uuid).getBalance();
  }
  
  public static void setBalance(UUID uuid, double amount) {
    AccountManager.updateAccount(AccountManager.getAccount(uuid).setBalance(amount));
  }
  
  public static void addBalance(UUID uuid, double amount) {
    setBalance(uuid, getBalance(uuid) + amount);
  }
  
  public static void removeBalance(UUID uuid, double amount) {
    setBalance(uuid, getBalance(uuid) - amount);
  }
  
  public static void transferBalance(UUID sender, UUID receiver, double amount) {
    removeBalance(sender, amount);
    addBalance(receiver, amount);
  }
  
  public static boolean hasBalance(UUID uuid, double amount) {
    return getBalance(uuid) >= amount;
  }
  
  public static double getCustomBalance(UUID uuid, String currency) {
    return AccountManager.getAccount(uuid).getCustomBalance(currency);
  }
  
  public static void setCustomBalance(UUID uuid, String currency, double amount) {
    AccountManager.updateAccount(AccountManager.getAccount(uuid).setCustomBalance(currency, amount));
  }
  
  public static void addCustomBalance(UUID uuid, String currency, double amount) {
    setCustomBalance(uuid, currency, getCustomBalance(uuid, currency) + amount);
  }
  
  public static void removeCustomBalance(UUID uuid, String currency, double amount) {
    setCustomBalance(uuid, currency, getCustomBalance(uuid, currency) - amount);
  }
  
  public static void transferCustomBalance(UUID sender, UUID receiver, String currency, double amount) {
    removeCustomBalance(sender, currency, amount);
    addCustomBalance(receiver, currency, amount);
  }
  
  public static boolean hasCustomBalance(UUID uuid, String currency, double amount) {
    return getCustomBalance(uuid, currency) >= amount;
  }
  
  public static void setPayable(UUID uuid, boolean isPayable) {
    AccountManager.getAccount(uuid).setPayable(isPayable);
  }
  
  public static boolean isPayable(UUID uuid) {
    return AccountManager.getAccount(uuid).getPayable();
  }
  
}
