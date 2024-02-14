package com.imnotstable.qualityeconomy.storage.accounts;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.Debug;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AccountManager {
  
  private static final ConcurrentMap<UUID, Account> accounts = new ConcurrentHashMap<>();
  
  public static Account createAccount(UUID uuid) {
    Debug.Timer timer = new Debug.Timer("createAccount()");
    String username = "";
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
    if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline())
      username = offlinePlayer.getName();
    else {
      Entity entity = Bukkit.getEntity(uuid);
      if (entity != null)
        username = entity.getName();
    }
    Account account;
    if (!accountExists(uuid)) {
      account = new Account(uuid).setUsername(username);
      StorageManager.getActiveStorageType().createAccount(account);
    } else
      account = getAccount(uuid).setUsername(username);
    accounts.put(uuid, account);
    timer.end();
    return account;
  }
  
  public static Collection<Account> getAllAccounts() {
    return new HashSet<>(accounts.values());
  }
  
  public static Account getAccount(UUID uuid) {
    Debug.Timer timer = new Debug.Timer("getAccount()");
    Account account = accounts.computeIfAbsent(uuid, AccountManager::createAccount);
    timer.end();
    return account;
  }
  
  public static boolean accountExists(UUID uuid) {
    Debug.Timer timer = new Debug.Timer("accountExists()");
    boolean accountExists = accounts.containsKey(uuid);
    timer.end();
    return accountExists;
  }
  
  public static void updateAccount(Account account) {
    Debug.Timer timer = new Debug.Timer("updateAccount()");
    accounts.put(account.getUniqueId(), account);
    timer.end();
  }
  
  public static void setupAccounts() {
    new BukkitRunnable() {
      @Override
      public void run() {
        Debug.Timer timer = new Debug.Timer("setupAccounts()");
        clearAccounts();
        accounts.putAll(StorageManager.getActiveStorageType().getAllAccounts());
        timer.end();
      }
    }.runTaskAsynchronously(QualityEconomy.getInstance());
  }
  
  public static void saveAllAccounts() {
    Debug.Timer timer = new Debug.Timer("saveAllAccounts()");
    StorageManager.getActiveStorageType().updateAccounts(AccountManager.accounts.values());
    timer.end();
  }
  
  public static void clearAccounts() {
    Debug.Timer timer = new Debug.Timer("clearAccounts()");
    accounts.clear();
    timer.end();
  }
  
  public static void createFakeAccounts(int entries) {
    new BukkitRunnable() {
      @Override
      public void run() {
        Debug.Timer timer = new Debug.Timer(String.format("createFakeAccounts(%d)", entries));
        Collection<Account> accounts = new ArrayList<>();
        Random random = new Random();
        Collection<String> currencies = Configuration.areCustomCurrenciesEnabled() ? StorageManager.getActiveStorageType().getCurrencies() : new ArrayList<>();
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
      }
    }.runTaskAsynchronously(QualityEconomy.getInstance());
  }
  
  public static void changeAllAccounts() {
    new BukkitRunnable() {
      @Override
      public void run() {
        Debug.Timer timer = new Debug.Timer("changeAllAccounts()");
        Random random = new Random();
        Collection<String> currencies = Configuration.areCustomCurrenciesEnabled() ? StorageManager.getActiveStorageType().getCurrencies() : new ArrayList<>();
        accounts.values().forEach(account -> {
          HashMap<String, Double> customBalances = new HashMap<>();
          for (String currency : currencies) {
            customBalances.put(currency, random.nextDouble(1_000_000_000_000_000.0));
          }
          account.setBalance(random.nextDouble(1_000_000_000_000_000.0)).setCustomBalances(customBalances);
        });
        StorageManager.getActiveStorageType().updateAccounts(accounts.values());
        setupAccounts();
        timer.end();
      }
    }.runTaskAsynchronously(QualityEconomy.getInstance());
  }
  
}