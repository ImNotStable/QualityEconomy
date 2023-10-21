package com.imnotstable.qualityeconomy.storage;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.storageformats.JsonStorageFormat;
import com.imnotstable.qualityeconomy.storage.storageformats.SQLStorageFormat;
import com.imnotstable.qualityeconomy.storage.storageformats.YamlStorageFormat;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import com.imnotstable.qualityeconomy.util.Error;
import com.imnotstable.qualityeconomy.util.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class StorageManager implements Listener {

  public static boolean lock = false;

  private static StorageFormat activeStorageFormat;

  public static StorageFormat getActiveStorageFormat() {
    return activeStorageFormat;
  }

  public static void initStorageProcesses() {
    if (lock) {
      Logger.log(Component.text("Cancelled initiation of storage processes (ENTRY_LOCK)", NamedTextColor.RED));
      return;
    }
    TestToolkit.Timer timer = new TestToolkit.Timer("Initiating storage processes...");
    StorageFormat storageFormat;
    switch (Configuration.getStorageType()) {
      case "sqlite" -> storageFormat = new SQLStorageFormat(1);
      case "yaml" -> storageFormat = new YamlStorageFormat();
      case "json" -> storageFormat = new JsonStorageFormat();
      case "mysql" -> storageFormat = new SQLStorageFormat(2);
      default -> throw new IllegalStateException("Unexpected value: " + Configuration.getStorageType());
    }
    activeStorageFormat = storageFormat;
    if (!activeStorageFormat.initStorageProcesses()) {
      new Error("Failed to initiate storage processes").log();
      timer.interrupt("Failed to initiate storage processes");
      return;
    }
    AccountManager.setupAccounts();
    Bukkit.getScheduler().scheduleSyncRepeatingTask(QualityEconomy.getInstance(), AccountManager::saveAllAccounts, 1200, 1200);
    long backupInterval = (long) (Configuration.getBackupInterval() * 20 * 60 * 60);
    if (backupInterval > 0)
      Bukkit.getScheduler().scheduleSyncRepeatingTask(QualityEconomy.getInstance(), StorageManager::createBackup, backupInterval, backupInterval);
    timer.end("Initiated storage processes");
  }

  public static void endStorageProcesses() {
    if (lock) {
      Logger.log(Component.text("Cancelled termination of storage processes (ENTRY_LOCK)", NamedTextColor.RED));
      return;
    }
    TestToolkit.Timer timer = new TestToolkit.Timer("Terminating storage processes...");
    AccountManager.saveAllAccounts();
    activeStorageFormat.endStorageProcesses();
    timer.end("Terminated storage processes");
  }

  private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public static void exportDatabase(final String path) {
    if (lock) {
      Logger.log(Component.text("Cancelled database export (ENTRY_LOCK)", NamedTextColor.RED));
      return;
    }
    new BukkitRunnable() {
      @Override
      public void run() {
        TestToolkit.Timer timer = new TestToolkit.Timer("Exporting database...");
        lock = true;
        AccountManager.saveAllAccounts();
        JSONObject rootJson = new JSONObject();
        StorageFormat storageFormat = StorageManager.getActiveStorageFormat();
        storageFormat.getAllAccounts().forEach((uuid, account) -> {
          JSONObject accountJson = new JSONObject();
          accountJson.put("name", account.getName());
          accountJson.put("balance", account.getBalance());
          accountJson.put("payable", account.getPayable());
          rootJson.put(uuid.toString(), accountJson);
        });
        String fileName = String.format("%sQualityEconomy-%s.json", path, LocalDate.now().format(EXPORT_DATE_FORMAT));
        try (FileWriter file = new FileWriter(fileName)) {
          file.write(rootJson.toString());
        } catch (IOException e) {
          e.printStackTrace();
        }
        lock = false;
        timer.end("Exported database");
      }
    }.runTaskAsynchronously(QualityEconomy.getInstance());
  }

  public static void importDatabase(final String fileName) {
    if (lock) {
      Logger.log(Component.text("Cancelled database import (ENTRY_LOCK)", NamedTextColor.RED));
      return;
    }
    new BukkitRunnable() {
      @Override
      public void run() {
        TestToolkit.Timer timer = new TestToolkit.Timer("Importing database...");
        lock = true;
        String path = String.format("plugins/QualityEconomy/%s", fileName);
        AccountManager.clearAccounts();
        StorageFormat storageFormat = StorageManager.getActiveStorageFormat();
        storageFormat.wipeDatabase();
        Collection<Account> accounts = new ArrayList<>();
        try {
          String content = new String(Files.readAllBytes(Paths.get(path)));
          JSONObject rootJson = new JSONObject(content);
          rootJson.keys().forEachRemaining(key -> {
            UUID uuid = UUID.fromString(key);
            JSONObject accountJson = rootJson.getJSONObject(key);
            String name = accountJson.getString("name");
            double balance = accountJson.getDouble("balance");
            boolean payable = accountJson.getBoolean("payable");
            accounts.add(new Account(uuid).setName(name).setBalance(balance).setPayable(payable));
            switch (accounts.size()) {
              case 10, 100, 1000, 2500, 5000, 10000 -> timer.progress();
            }
          });
          storageFormat.createAccounts(accounts);
          timer.progress();
        } catch (IOException e) {
          e.printStackTrace();
        }
        lock = false;
        timer.end("Imported database");
        AccountManager.setupAccounts();
      }
    }.runTaskAsynchronously(QualityEconomy.getInstance());
  }

  public static void createBackup() {
    if (lock) {
      Logger.log(Component.text("Cancelled database backup (ENTRY_LOCK)", NamedTextColor.RED));
      return;
    }
    exportDatabase("plugins/QualityEconomy/backups/");
  }

  public static UUID getUUID(OfflinePlayer player) {
    return player.getUniqueId();
  }

  public static UUID getUUID(String player) {
    return Bukkit.getOfflinePlayer(player).getUniqueId();
  }

  @EventHandler
  public void onPlayerJoinEvent(PlayerJoinEvent event) {
    TestToolkit.Timer timer = new TestToolkit.Timer("Running PlayerJoinEvent...");
    UUID uuid = event.getPlayer().getUniqueId();
    AccountManager.createAccount(uuid);
    AccountManager.updateAccount(AccountManager.getAccount(uuid).setName(event.getPlayer().getName()));
    timer.end("Ran PlayerJoinEvent");
  }

}
