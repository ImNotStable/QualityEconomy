package com.imnotstable.qualityeconomy.storage.accounts;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.storage.storageformats.StorageType;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AccountManager {
  
  private static final Map<UUID, Account> accounts = new HashMap<>();
  
  public static Account createAccount(UUID uuid) {
    synchronized (StorageManager.lock) {
      TestToolkit.Timer timer = new TestToolkit.Timer("Creating account...");
      OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
      if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline())
        return new Account(uuid).setName(uuid.toString()).setPayable(false);
      StorageType activeStorageType = StorageManager.getActiveStorageFormat();
      Account account;
      if (!accountExists(uuid)) {
        account = new Account(uuid);
        activeStorageType.createAccount(account);
      } else
        account = getAccount(uuid);
      accounts.put(uuid, account.setName(offlinePlayer.getName()));
      timer.end("Created account");
      return account;
    }
  }
  
  public static Collection<Account> getAllAccounts() {
    synchronized (StorageManager.lock) {
      return accounts.values();
    }
  }
  
  public static Account getAccount(UUID uuid) {
    synchronized (StorageManager.lock) {
      TestToolkit.Timer timer = new TestToolkit.Timer("Retrieving account...");
      Account account = accounts.computeIfAbsent(uuid, AccountManager::createAccount);
      timer.end("Retrieved account");
      return account;
    }
  }
  
  public static boolean accountExists(UUID uuid) {
    synchronized (StorageManager.lock) {
      TestToolkit.Timer timer = new TestToolkit.Timer("Checking if account exists");
      boolean accountExists = accounts.containsKey(uuid);
      timer.end("Checked if account exists");
      return accountExists;
    }
  }
  
  public static void updateAccount(Account account) {
    synchronized (StorageManager.lock) {
      TestToolkit.Timer timer = new TestToolkit.Timer("Updating account...");
      accounts.put(account.getUUID(), account);
      timer.end("Updated account");
    }
  }
  
  public static void setupAccounts() {
    synchronized (StorageManager.lock) {
      TestToolkit.Timer timer = new TestToolkit.Timer("Setting up all accounts...");
      clearAccounts();
      accounts.putAll(StorageManager.getActiveStorageFormat().getAllAccounts());
      timer.end("Setup all accounts");
    }
  }
  
  public static void saveAllAccounts() {
    synchronized (StorageManager.lock) {
      TestToolkit.Timer timer = new TestToolkit.Timer("Saving all accounts...");
      StorageManager.getActiveStorageFormat().updateAccounts(AccountManager.accounts.values());
      timer.end("Saved all accounts");
    }
  }
  
  public static void clearAccounts() {
    synchronized (StorageManager.lock) {
      TestToolkit.Timer timer = new TestToolkit.Timer("Clearing accounts...");
      accounts.clear();
      timer.end("Cleared accounts");
    }
  }
  
  public static void createFakeAccounts(int entries) {
    synchronized (StorageManager.lock) {
      new BukkitRunnable() {
        @Override
        public void run() {
          TestToolkit.Timer timer = new TestToolkit.Timer(String.format("Creating %d fake entries", entries));
          Collection<Account> accounts = new ArrayList<>();
          Random random = new Random();
          for (int i = 0; i < entries; ++i) {
            UUID uuid = UUID.randomUUID();
            accounts.add(new Account(uuid).setName(uuid.toString().split("-")[0]).setBalance(random.nextDouble(1_000_000_000_000_000D)).setPayable(false));
          }
          StorageManager.getActiveStorageFormat().createAccounts(accounts);
          setupAccounts();
          timer.end(String.format("Created %d fake entries", entries));
        }
      }.runTaskAsynchronously(QualityEconomy.getInstance());
    }
  }
  
}