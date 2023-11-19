package com.imnotstable.qualityeconomy.storage;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.storageformats.H2StorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.JsonStorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.MySQLStorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.SQLiteStorageType;
import com.imnotstable.qualityeconomy.storage.storageformats.StorageType;
import com.imnotstable.qualityeconomy.util.Error;
import com.imnotstable.qualityeconomy.util.Logger;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StorageManager implements Listener {
  
  private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH-mm");
  public static final Object lock = new Object();
  private static StorageType activeStorageType;
  private static int backupSchedulerID = 0;
  
  public static StorageType getActiveStorageFormat() {
    return activeStorageType;
  }
  
  public static void initStorageProcesses() {
    synchronized (lock) {
      TestToolkit.Timer timer = new TestToolkit.Timer("Initiating storage processes...");
      switch (Configuration.getStorageType()) {
        case "sqlite" -> activeStorageType = new SQLiteStorageType();
        case "mysql" -> activeStorageType = new MySQLStorageType();
        case "h2" -> activeStorageType = new H2StorageType();
        case "json" -> activeStorageType = new JsonStorageType();
        default -> {
          new Error("Unexpected Storage Type: " + Configuration.getStorageType()).log();
          timer.interrupt("Failed to initiate storage processes");
          Bukkit.getPluginManager().disablePlugin(QualityEconomy.getInstance());
          return;
        }
      }
      if (!activeStorageType.initStorageProcesses()) {
        new Error("Failed to initiate storage processes").log();
        timer.interrupt("Failed to initiate storage processes");
        return;
      }
      AccountManager.setupAccounts();
      Bukkit.getScheduler().scheduleSyncRepeatingTask(QualityEconomy.getInstance(), AccountManager::saveAllAccounts, 1200, 1200);
      if (Configuration.getBackupInterval() > 0)
        backupSchedulerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
          QualityEconomy.getInstance(),
          () -> exportDatabase("plugins/QualityEconomy/backups/"),
          Configuration.getBackupInterval(),
          Configuration.getBackupInterval()
        );
      timer.end("Initiated storage processes");
    }
  }
  
  public static void endStorageProcesses() {
    synchronized (lock) {
      TestToolkit.Timer timer = new TestToolkit.Timer("Terminating storage processes...");
      AccountManager.saveAllAccounts();
      
      if (backupSchedulerID != 0) {
        Bukkit.getScheduler().cancelTask(backupSchedulerID);
        backupSchedulerID = 0;
      }
      
      activeStorageType.endStorageProcesses();
      timer.end("Terminated storage processes");
    }
  }
  
  public static void exportDatabase(final String path) {
    synchronized (lock) {
      new BukkitRunnable() {
        @Override
        public void run() {
          AccountManager.saveAllAccounts();
          TestToolkit.Timer timer = new TestToolkit.Timer("Exporting database...");
          File dir = new File(path);
          if (!dir.exists() || !dir.isDirectory()) {
            Logger.log(Component.text("Specified directory not found...", NamedTextColor.RED));
            Logger.log(Component.text("Creating directory...", NamedTextColor.GRAY));
            Logger.log(dir.mkdir() ?
              Component.text("Successfully created directory", NamedTextColor.GREEN) :
              Component.text("Failed to create directory", NamedTextColor.RED));
          }
          JSONObject rootJson = new JSONObject();
          rootJson.put("custom-currencies", CustomCurrencies.getCustomCurrencies());
          StorageType storageType = StorageManager.getActiveStorageFormat();
          storageType.getAllAccounts().forEach((uuid, account) -> {
            JSONObject accountJson = new JSONObject();
            accountJson.put("name", account.getName());
            accountJson.put("balance", account.getBalance());
            accountJson.put("payable", account.getPayable());
            for (String currency : CustomCurrencies.getCustomCurrencies())
              accountJson.put(currency, account.getCustomBalance(currency));
            rootJson.put(uuid.toString(), accountJson);
          });
          String fileName = String.format("%sQualityEconomy %s.json", path, LocalDateTime.now().format(EXPORT_DATE_FORMAT));
          try (FileWriter file = new FileWriter(fileName)) {
            file.write(rootJson.toString());
          } catch (IOException exception) {
            new Error("Error while exporting playerdata", exception).log();
          }
          timer.end("Exported database");
        }
      }.runTaskAsynchronously(QualityEconomy.getInstance());
    }
  }
  
  public static void importDatabase(final String fileName) {
    synchronized (lock) {
      new BukkitRunnable() {
        @Override
        public void run() {
          TestToolkit.Timer timer = new TestToolkit.Timer("Importing database...");
          String path = String.format("plugins/QualityEconomy/%s", fileName);
          AccountManager.clearAccounts();
          StorageType storageType = StorageManager.getActiveStorageFormat();
          storageType.wipeDatabase();
          for (String currency : CustomCurrencies.getCustomCurrencies())
            CustomCurrencies.deleteCustomCurrency(currency);
          Collection<Account> accounts = new ArrayList<>();
          try {
            String content = new String(Files.readAllBytes(Paths.get(path)));
            JSONObject rootJson = new JSONObject(content);
            
            List<String> customCurrencies = new ArrayList<>();
            if (!rootJson.isNull("custom-currencies"))
              for (int i = 0; i < rootJson.getJSONArray("custom-currencies").length(); i++) {
                String currency = rootJson.getJSONArray("custom-currencies").getString(i);
                customCurrencies.add(currency);
                CustomCurrencies.createCustomCurrency(currency);
              }
            
            rootJson.keySet().stream().filter(key -> !key.equalsIgnoreCase("custom-currencies")).forEach(key -> {
              UUID uuid = UUID.fromString(key);
              JSONObject accountJson = rootJson.getJSONObject(key);
              String name = accountJson.getString("name");
              double balance = accountJson.getDouble("balance");
              boolean payable = accountJson.getBoolean("payable");
              Map<String, Double> balanceMap = new HashMap<>();
              for (String currency : customCurrencies) {
                balanceMap.put(currency, accountJson.getDouble(currency));
              }
              accounts.add(new Account(uuid).setName(name).setBalance(balance).setPayable(payable).setCustomBalances(balanceMap));
            });
            storageType.createAccounts(accounts);
            timer.progress();
          } catch (IOException exception) {
            new Error("Error while importing playerdata", exception).log();
          }
          timer.end("Imported database");
          AccountManager.setupAccounts();
        }
      }.runTaskAsynchronously(QualityEconomy.getInstance());
    }
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
    AccountManager.createAccount(event.getPlayer().getUniqueId());
    timer.end("Ran PlayerJoinEvent");
  }
  
}
