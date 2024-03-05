package com.imnotstable.qualityeconomy.storage.accounts;

import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.Misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AccountManager {
  
  private static final ConcurrentMap<UUID, Account> accounts = new ConcurrentHashMap<>();
  
  public static Account createAccount(UUID uuid) {
    if (accountExists(uuid))
      return accounts.get(uuid);
    Account account = new Account(uuid);
    StorageManager.getActiveStorageType().createAccount(account);
    accounts.put(uuid, account);
    return account;
  }
  
  public static Collection<Account> getAllAccounts() {
    return accounts.values();
  }
  
  public static Account getAccount(UUID uuid) {
    return createAccount(uuid);
  }
  
  public static boolean accountExists(UUID uuid) {
    return accounts.containsKey(uuid);
  }
  
  public static void setupAccounts() {
    Debug.Timer timer = new Debug.Timer("setupAccounts()");
    clearAccounts();
    accounts.putAll(StorageManager.getActiveStorageType().getAllAccounts());
    timer.end();
  }
  
  public static void saveAllAccounts() {
    Debug.Timer timer = new Debug.Timer("saveAllAccounts() [" + accounts.size() + "]");
    StorageManager.getActiveStorageType().saveAllAccounts();
    timer.end();
  }
  
  public static void clearAccounts() {
    Debug.Timer timer = new Debug.Timer("clearAccounts()");
    accounts.clear();
    timer.end();
  }
  
  public static void createFakeAccounts(int entries) {
    Misc.runAsync(() -> {
      Debug.Timer timer = new Debug.Timer(String.format("createFakeAccounts(%d)", entries));
      Collection<Account> accounts = new ArrayList<>();
      Random random = new Random();
      Collection<String> currencies = Configuration.isCustomCurrenciesEnabled() ? StorageManager.getActiveStorageType().getCurrencies() : new ArrayList<>();
      for (int i = 0; i < entries; ++i) {
        UUID uuid = UUID.randomUUID();
        HashMap<String, Double> customBalances = new HashMap<>();
        for (String currency : currencies) {
          customBalances.put(currency, random.nextDouble(1_000_000_000_000_000.0));
        }
        accounts.add(new Account(uuid).setUsername(uuid.toString().split("-")[0])
          .setBalance(random.nextDouble(1_000_000_000_000_000.0))
          .setCustomBalances(customBalances).setPayable(false)
        );
      }
      StorageManager.getActiveStorageType().createAccounts(accounts);
      setupAccounts();
      timer.end();
    });
  }
  
  public static void changeAllAccounts() {
    Misc.runAsync(() -> {
      Debug.Timer timer = new Debug.Timer("changeAllAccounts()");
      Random random = new Random();
      Collection<String> currencies = Configuration.isCustomCurrenciesEnabled() ? StorageManager.getActiveStorageType().getCurrencies() : new ArrayList<>();
      accounts.values().forEach(account -> {
        HashMap<String, Double> customBalances = new HashMap<>();
        for (String currency : currencies) {
          customBalances.put(currency, random.nextDouble(1_000_000_000_000_000.0));
        }
        account.setBalance(random.nextDouble(1_000_000_000_000_000.0)).setCustomBalances(customBalances);
      });
      timer.end();
    });
  }
  
}
