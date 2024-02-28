package com.imnotstable.qualityeconomy.api;

import com.imnotstable.qualityeconomy.commands.RequestCommand;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public final class QualityEconomyAPI {
  
  public static void createAccount(@NotNull UUID uuid) {
    AccountManager.createAccount(uuid);
  }
  
  public static @NotNull Account getAccount(@NotNull UUID uuid) {
    return AccountManager.getAccount(uuid);
  }
  
  public static boolean hasAccount(@NotNull UUID uuid) {
    return AccountManager.accountExists(uuid);
  }
  
  public static double getBalance(@NotNull UUID uuid) {
    return getAccount(uuid).getBalance();
  }
  
  public static void setBalance(@NotNull UUID uuid, double amount) {
    AccountManager.updateAccount(getAccount(uuid).setBalance(amount));
  }
  
  public static void addBalance(@NotNull UUID uuid, double amount) {
    setBalance(uuid, getBalance(uuid) + amount);
  }
  
  public static void removeBalance(@NotNull UUID uuid, double amount) {
    setBalance(uuid, getBalance(uuid) - amount);
  }
  
  public static void transferBalance(@NotNull UUID sender, @NotNull UUID receiver, double amount) {
    removeBalance(sender, amount);
    addBalance(receiver, amount);
  }
  
  public static boolean hasBalance(@NotNull UUID uuid, double amount) {
    return getBalance(uuid) >= amount;
  }
  
  public static double getCustomBalance(@NotNull UUID uuid, @NotNull String currency) {
    return getAccount(uuid).getCustomBalance(currency);
  }
  
  public static void setCustomBalance(@NotNull UUID uuid, @NotNull String currency, double amount) {
    AccountManager.updateAccount(getAccount(uuid).setCustomBalance(currency, amount));
  }
  
  public static void addCustomBalance(@NotNull UUID uuid, @NotNull String currency, double amount) {
    setCustomBalance(uuid, currency, getCustomBalance(uuid, currency) + amount);
  }
  
  public static void removeCustomBalance(@NotNull UUID uuid, @NotNull String currency, double amount) {
    setCustomBalance(uuid, currency, getCustomBalance(uuid, currency) - amount);
  }
  
  public static void transferCustomBalance(@NotNull UUID sender, @NotNull UUID receiver, @NotNull String currency, double amount) {
    removeCustomBalance(sender, currency, amount);
    addCustomBalance(receiver, currency, amount);
  }
  
  public static boolean hasCustomBalance(@NotNull UUID uuid, @NotNull String currency, double amount) {
    return getCustomBalance(uuid, currency) >= amount;
  }
  
  public static void setPayable(@NotNull UUID uuid, boolean isPayable) {
    getAccount(uuid).setPayable(isPayable);
  }
  
  public static boolean isPayable(@NotNull UUID uuid) {
    return getAccount(uuid).isPayable();
  }
  
  public static void setRequestable(@NotNull UUID uuid, boolean isRequestable) {
    getAccount(uuid).setRequestable(isRequestable);
  }
  
  public static boolean isRequestable(@NotNull UUID uuid) {
    return getAccount(uuid).isRequestable();
  }
  
  public static void createRequest(@NotNull UUID requester, @NotNull UUID requestee, double amount) {
    if (RequestCommand.getRequests() == null)
      return;
    RequestCommand.getRequests().computeIfAbsent(requestee, uuid -> new ConcurrentHashMap<>()).put(requester, amount);
  }
  
  public static void deleteRequest(@NotNull UUID requester, @NotNull UUID requestee) {
    if (RequestCommand.getRequests() == null)
      return;
    RequestCommand.getRequests().get(requestee).remove(requester);
  }
  
  public static boolean requestExists(@NotNull UUID requester, @NotNull UUID requestee) {
    if (RequestCommand.getRequests() == null)
      return false;
    return RequestCommand.getRequests().get(requestee).containsKey(requester);
  }
  
  public static boolean hasRequest(@NotNull UUID requestee) {
    if (RequestCommand.getRequests() == null) return false;
    return RequestCommand.getRequests().containsKey(requestee) && !RequestCommand.getRequests().get(requestee).isEmpty();
  }
  
  public static void answerRequest(@NotNull UUID requester, @NotNull UUID requestee, boolean answer) {
    if (RequestCommand.getRequests() == null) return;
    if (answer) {
      double amount = RequestCommand.getRequests().get(requestee).get(requester);
      transferBalance(requestee, requester, amount);
    }
    deleteRequest(requester, requestee);
  }
  
  public static void createCustomCurrency(@NotNull String currency) {
    StorageManager.addCurrency(currency);
  }
  
  public static void deleteCustomCurrency(@NotNull String currency) {
    StorageManager.removeCurrency(currency);
  }
  
  public static boolean doesCustomCurrencyExist(@NotNull String currency) {
    return getCustomCurrencies().contains(currency);
  }
  
  public static @NotNull Set<String> getCustomCurrencies() {
    return StorageManager.getActiveStorageType().getCurrencies();
  }
  
}
