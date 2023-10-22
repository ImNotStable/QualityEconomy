package com.imnotstable.qualityeconomy.storage;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Logger;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AccountManager {
  
  private static final Map<UUID, Account> accounts = new HashMap<>();
  
  public synchronized static Account createAccount(UUID uuid) {
    TestToolkit.Timer timer = new TestToolkit.Timer("Creating account...");
    StorageFormat activeStorageFormat = StorageManager.getActiveStorageFormat();
    if (!activeStorageFormat.accountExists(uuid)) {
      Account account = new Account(uuid).setName(Bukkit.getOfflinePlayer(uuid).getName()).setBalance(0).setPayable(true);
      activeStorageFormat.createAccount(account);
    }
    Account account = activeStorageFormat.getAccount(uuid);
    accounts.put(uuid, account);
    timer.end("Created account");
    return account;
  }
  
  public synchronized static Collection<Account> getAllAccounts() {
    return accounts.values();
  }
  
  public synchronized static Account getAccount(UUID uuid) {
    TestToolkit.Timer timer = new TestToolkit.Timer("Getting account...");
    Account account = accounts.computeIfAbsent(uuid, AccountManager::createAccount);
    timer.end("Got account");
    return account;
  }
  
  public synchronized static boolean accountExists(UUID uuid) {
    TestToolkit.Timer timer = new TestToolkit.Timer("Checking if account exists");
    boolean bool = accounts.containsKey(uuid);
    timer.end("Checked if account exists");
    return bool;
  }
  
  public synchronized static void updateAccount(Account account) {
    TestToolkit.Timer timer = new TestToolkit.Timer("Updating account...");
    accounts.put(account.getUUID(), account);
    timer.end("Updated account");
  }
  
  public synchronized static void setupAccounts() {
    if (StorageManager.lock) {
      Logger.log(Component.text("Cancelled account setup process (ENTRY_LOCK)", NamedTextColor.RED));
      return;
    }
    TestToolkit.Timer timer = new TestToolkit.Timer("Setting up all accounts...");
    StorageManager.lock = true;
    clearAccounts();
    StorageManager.getActiveStorageFormat().getAllAccounts()
      .forEach((uuid, account) -> AccountManager.accounts.put(uuid, new Account(uuid).setName(account.getName()).setBalance(account.getBalance()).setPayable(account.getPayable())));
    StorageManager.lock = false;
    timer.end("Setup all accounts");
  }
  
  public synchronized static void saveAllAccounts() {
    if (StorageManager.lock) {
      Logger.log(Component.text("Cancelled account saving process (ENTRY_LOCK)", NamedTextColor.RED));
      return;
    }
    TestToolkit.Timer timer = new TestToolkit.Timer("Saving all accounts...");
    StorageManager.lock = true;
    StorageManager.getActiveStorageFormat().updateAccounts(AccountManager.accounts.values());
    StorageManager.lock = false;
    timer.end("Saved all accounts");
  }
  
  public synchronized static void clearAccounts() {
    TestToolkit.Timer timer = new TestToolkit.Timer("Clearing accounts...");
    accounts.clear();
    timer.end("Cleared accounts");
  }
  
  public synchronized static void createFakeAccounts(int entries) {
    if (StorageManager.lock) {
      Logger.log(Component.text("Cancelled account fake entry creation process (ENTRY_LOCK)", NamedTextColor.RED));
      return;
    }
    new BukkitRunnable() {
      @Override
      public void run() {
        TestToolkit.Timer timer = new TestToolkit.Timer(String.format("Creating %d fake entries", entries));
        StorageManager.lock = true;
        Collection<Account> accounts = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < entries; ++i) {
          UUID uuid = UUID.randomUUID();
          accounts.add(new Account(uuid).setName(uuid.toString().split("-")[0]).setBalance(random.nextDouble(1_000_000_000_000_000D)).setPayable(false));
        }
        StorageManager.getActiveStorageFormat().createAccounts(accounts);
        StorageManager.lock = false;
        timer.end(String.format("Created %d fake entries", entries));
      }
    }.runTaskAsynchronously(QualityEconomy.getInstance());
  }
  
}