package com.imnotstable.qualityeconomy.storage;

import com.imnotstable.qualityeconomy.economy.Account;
import com.imnotstable.qualityeconomy.util.debug.Timer;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AccountManager {
  
  private static final ConcurrentMap<UUID, Account> accounts = new ConcurrentHashMap<>();
  
  public static Account createAccount(UUID uuid) {
    if (accountExists(uuid))
      throw new IllegalArgumentException("Account already exists: " + uuid);
    Account account = new Account(uuid);
    StorageManager.getActiveStorageType().createAccount(account);
    accounts.put(uuid, account);
    return account;
  }
  
  public static Collection<Account> getAllAccounts() {
    return accounts.values();
  }
  
  public static Account getAccount(UUID uuid) {
    return accountExists(uuid) ? accounts.get(uuid) : createAccount(uuid);
  }
  
  public static boolean accountExists(UUID uuid) {
    return accounts.containsKey(uuid);
  }
  
  public static void setupAccounts() {
    Timer timer = new Timer("setupAccounts()");
    clearAccounts();
    accounts.putAll(StorageManager.getActiveStorageType().getAllAccounts());
    timer.end();
  }
  
  public static void saveAllAccounts() {
    Timer timer = new Timer("saveAllAccounts() [" + accounts.size() + "]");
    StorageManager.getActiveStorageType().saveAllAccounts();
    timer.end();
  }
  
  public static void clearAccounts() {
    accounts.clear();
  }
  
}
