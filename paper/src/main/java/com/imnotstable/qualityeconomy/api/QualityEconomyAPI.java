package com.imnotstable.qualityeconomy.api;

import com.imnotstable.qualityeconomy.economy.Account;
import com.imnotstable.qualityeconomy.economy.BalanceEntry;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@SuppressWarnings("unused")
public class QualityEconomyAPI {
  
  public static void createAccount(@NotNull UUID uniqueId) {
    com.imnotstable.qualityeconomy.storage.AccountManager.createAccount(uniqueId);
  }
  
  public static boolean hasAccount(@NotNull UUID uniqueId) {
    return com.imnotstable.qualityeconomy.storage.AccountManager.accountExists(uniqueId);
  }
  
  public static Account getAccount(@NotNull UUID uniqueId) {
    return AccountManager.getAccount(uniqueId);
  }
  
  public static BalanceEntry getBalanceEntry(@NotNull UUID uniqueId, @NotNull String currencyName) {
    return getAccount(uniqueId).getBalanceEntry(currencyName);
  }
  
  public static double getBalance(@NotNull UUID uniqueId) {
    return getBalance(uniqueId, "default");
  }
  
  public static double getBalance(@NotNull UUID uniqueId, @NotNull String currencyName) {
    return getBalanceEntry(uniqueId, currencyName).getBalance();
  }
  
  public static boolean hasBalance(@NotNull UUID uniqueId, double balance) {
    return hasBalance(uniqueId, "default", balance);
  }
  
  public static boolean hasBalance(@NotNull UUID uniqueId, @NotNull String currencyName, double balance) {
    return getBalanceEntry(uniqueId, currencyName).getBalance() >= balance;
  }
  
  public static void setBalance(@NotNull UUID uniqueId, double balance) {
    setBalance(uniqueId, "default", balance);
  }
  
  public static void setBalance(@NotNull UUID uniqueId, @NotNull String currencyName, double balance) {
    getBalanceEntry(uniqueId, currencyName).setBalance(balance);
  }
  
  public static void addBalance(@NotNull UUID uniqueId, double balance) {
    addBalance(uniqueId, "default", balance);
  }
  
  public static void addBalance(@NotNull UUID uniqueId, @NotNull String currencyName, double balance) {
    getBalanceEntry(uniqueId, currencyName).increaseBalance(balance);
  }
  
  public static void removeBalance(@NotNull UUID uniqueId, double balance) {
    removeBalance(uniqueId, "default", balance);
  }
  
  public static void removeBalance(@NotNull UUID uniqueId, @NotNull String currencyName, double balance) {
    getBalanceEntry(uniqueId, currencyName).decreaseBalance(balance);
  }
  
  public static void transferBalance(@NotNull UUID senderUniqueId, @NotNull UUID receiverUniqueId, double balance) {
    transferBalance(senderUniqueId, receiverUniqueId, "default", balance);
  }
  
  public static void transferBalance(@NotNull UUID senderUniqueId, @NotNull UUID receiverUniqueId, @NotNull String currencyName, double balance) {
    BalanceEntry senderEntry = getBalanceEntry(senderUniqueId, currencyName);
    BalanceEntry receiverEntry = getBalanceEntry(receiverUniqueId, currencyName);
    senderEntry.decreaseBalance(balance);
    receiverEntry.increaseBalance(balance);
  }
  
  public static boolean isPayable(@NotNull UUID uniqueId) {
    return isPayable(uniqueId, "default");
  }
  
  public static boolean isPayable(@NotNull UUID uniqueId, @NotNull String currencyName) {
    return getBalanceEntry(uniqueId, currencyName).isPayable();
  }
  
  public static void setPayable(@NotNull UUID uniqueId, boolean payable) {
    setPayable(uniqueId, "default", payable);
  }
  
  public static void setPayable(@NotNull UUID uniqueId, @NotNull String currencyName, boolean payable) {
    getBalanceEntry(uniqueId, currencyName).setPayable(payable);
  }
  
}
