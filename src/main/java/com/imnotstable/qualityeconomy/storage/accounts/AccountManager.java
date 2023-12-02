package com.imnotstable.qualityeconomy.storage.accounts;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

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
    TestToolkit.Timer timer = new TestToolkit.Timer("Creating account...");
    Account account;
    if (!accountExists(uuid)) {
      account = new Account(uuid);
      StorageManager.getActiveStorageFormat().createAccount(account);
    } else
      account = getAccount(uuid);
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
    if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline())
      account.setName(offlinePlayer.getName());
    else {
      Entity entity = Bukkit.getEntity(uuid);
      if (entity != null)
        account.setName(entity.getName());
    }
    accounts.put(uuid, account);
    timer.end("Created account");
    return account;
  }
  
  public static Collection<Account> getAllAccounts() {
    return accounts.values();
  }
  
  public static Account getAccount(UUID uuid) {
    TestToolkit.Timer timer = new TestToolkit.Timer("Retrieving account...");
    Account account = accounts.computeIfAbsent(uuid, AccountManager::createAccount);
    timer.end("Retrieved account");
    return account;
  }
  
  public static boolean accountExists(UUID uuid) {
    TestToolkit.Timer timer = new TestToolkit.Timer("Checking if account exists");
    boolean accountExists = accounts.containsKey(uuid);
    timer.end("Checked if account exists");
    return accountExists;
  }
  
  public static void updateAccount(Account account) {
    TestToolkit.Timer timer = new TestToolkit.Timer("Updating account...");
    accounts.put(account.getUUID(), account);
    timer.end("Updated account");
  }
  
  public static void setupAccounts() {
    TestToolkit.Timer timer = new TestToolkit.Timer("Setting up all accounts...");
    clearAccounts();
    accounts.putAll(StorageManager.getActiveStorageFormat().getAllAccounts());
    timer.end("Setup all accounts");
  }
  
  public static void saveAllAccounts() {
    TestToolkit.Timer timer = new TestToolkit.Timer("Saving all accounts...");
    StorageManager.getActiveStorageFormat().updateAccounts(AccountManager.accounts.values());
    timer.end("Saved all accounts");
  }
  
  public static void clearAccounts() {
    TestToolkit.Timer timer = new TestToolkit.Timer("Clearing accounts...");
    accounts.clear();
    timer.end("Cleared accounts");
  }
  
  public static void createFakeAccounts(int entries) {
    new BukkitRunnable() {
      @Override
      public void run() {
        TestToolkit.Timer timer = new TestToolkit.Timer(String.format("Creating %d fake entries", entries));
        Collection<Account> accounts = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < entries; ++i) {
          UUID uuid = UUID.randomUUID();
          accounts.add(new Account(uuid).setName(uuid.toString().split("-")[0]).setBalance(random.nextDouble(1_000_000_000_000_000.0)).setPayable(false));
        }
        StorageManager.getActiveStorageFormat().createAccounts(accounts);
        setupAccounts();
        timer.end(String.format("Created %d fake entries", entries));
      }
    }.runTaskAsynchronously(QualityEconomy.getInstance());
  }
  
  public static void changeAllAccounts() {
    new BukkitRunnable() {
      @Override
      public void run() {
        TestToolkit.Timer timer = new TestToolkit.Timer("Changing all accounts");
        Random random = new Random();
        accounts.values().forEach(account -> {
          HashMap<String, Double> customBalances = new HashMap<>();
          for (String currency : CustomCurrencies.getCustomCurrencies()) {
            customBalances.put(currency, random.nextDouble(1_000_000_000_000_000.0));
          }
          account.setBalance(random.nextDouble(1_000_000_000_000_000.0)).setCustomBalances(customBalances);
        });
        StorageManager.getActiveStorageFormat().updateAccounts(accounts.values());
        setupAccounts();
        timer.end("Changed all accounts");
      }
    }.runTaskAsynchronously(QualityEconomy.getInstance());
  }
  
}