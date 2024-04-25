package com.imnotstable.qualityeconomy.api;

import org.jetbrains.annotations.NotNull;

public class QualityEconomyAPI {
  
  public static void createAccount(@NotNull java.util.UUID uniqueId) {
    com.imnotstable.qualityeconomy.storage.accounts.AccountManager.createAccount(uniqueId);
  }
  
  public static boolean hasAccount(@NotNull java.util.UUID uniqueId) {
    return com.imnotstable.qualityeconomy.storage.accounts.AccountManager.accountExists(uniqueId);
  }
  
  public static com.imnotstable.qualityeconomy.storage.accounts.Account getAccount(@NotNull java.util.UUID uniqueId) {
    return com.imnotstable.qualityeconomy.storage.accounts.AccountManager.getAccount(uniqueId);
  }
  
  public static double getBalance(@NotNull java.util.UUID uniqueId) {
    return getBalance(uniqueId, "default");
  }
  
  public static double getBalance(@NotNull java.util.UUID uniqueId, @NotNull String currencyName) {
    return getBalanceEntry(uniqueId, currencyName).getBalance();
  }
  
  public static boolean hasBalance(@NotNull java.util.UUID uniqueId, double balance) {
    return hasBalance(uniqueId, "default", balance);
  }
  
  public static boolean hasBalance(@NotNull java.util.UUID uniqueId, @NotNull String currencyName, double balance) {
    return getBalanceEntry(uniqueId, currencyName).getBalance() >= balance;
  }
  
  public static void setBalance(@NotNull java.util.UUID uniqueId, double balance) {
    setBalance(uniqueId, "default", balance);
  }
  
  public static void setBalance(@NotNull java.util.UUID uniqueId, @NotNull String currencyName, double balance) {
    getBalanceEntry(uniqueId, currencyName).setBalance(balance);
  }
  
  public static void addBalance(@NotNull java.util.UUID uniqueId, double balance) {
    addBalance(uniqueId, "default", balance);
  }
  
  public static void addBalance(@NotNull java.util.UUID uniqueId, @NotNull String currencyName, double balance) {
    getBalanceEntry(uniqueId, currencyName).increaseBalance(balance);
  }
  
  public static void removeBalance(@NotNull java.util.UUID uniqueId, double balance) {
    removeBalance(uniqueId, "default", balance);
  }
  
  public static void removeBalance(@NotNull java.util.UUID uniqueId, @NotNull String currencyName, double balance) {
    getBalanceEntry(uniqueId, currencyName).decreaseBalance(balance);
  }
  
  public static void transferBalance(@NotNull java.util.UUID senderUniqueId, @NotNull java.util.UUID receiverUniqueId, double balance) {
    transferBalance(senderUniqueId, receiverUniqueId, "default", balance);
  }
  
  public static void transferBalance(@NotNull java.util.UUID senderUniqueId, @NotNull java.util.UUID receiverUniqueId, @NotNull String currencyName, double balance) {
    com.imnotstable.qualityeconomy.storage.accounts.BalanceEntry senderEntry = getBalanceEntry(senderUniqueId, currencyName);
    com.imnotstable.qualityeconomy.storage.accounts.BalanceEntry receiverEntry = getBalanceEntry(receiverUniqueId, currencyName);
    senderEntry.decreaseBalance(balance);
    receiverEntry.increaseBalance(balance);
  }
  
  public static boolean isPayable(@NotNull java.util.UUID uniqueId) {
    return isPayable(uniqueId, "default");
  }
  
  public static boolean isPayable(@NotNull java.util.UUID uniqueId, @NotNull String currencyName) {
    return getBalanceEntry(uniqueId, currencyName).isPayable();
  }
  
  public static void setPayable(@NotNull java.util.UUID uniqueId, boolean payable) {
    setPayable(uniqueId, "default", payable);
  }
  
  public static void setPayable(@NotNull java.util.UUID uniqueId, @NotNull String currencyName, boolean payable) {
    getBalanceEntry(uniqueId, currencyName).setPayable(payable);
  }
  
  public static com.imnotstable.qualityeconomy.storage.accounts.BalanceEntry getBalanceEntry(@NotNull java.util.UUID uniqueId, @NotNull String currencyName) {
    return getAccount(uniqueId).getBalanceEntry(currencyName);
  }
  
}
