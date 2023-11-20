package com.imnotstable.qualityeconomy.storage;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.storage.storageformats.StorageType;
import com.imnotstable.qualityeconomy.util.QualityError;
import com.imnotstable.qualityeconomy.util.Logger;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

public class DBTransferUtils {
  
  private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH-mm");
  
  public static void exportDatabase(final String path) {
    synchronized (StorageManager.lock) {
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
            accountJson.put("name", account.getName())
              .put("balance", account.getBalance())
              .put("payable", account.getPayable());
            for (String currency : CustomCurrencies.getCustomCurrencies())
              accountJson.put(currency, account.getCustomBalance(currency));
            rootJson.put(uuid.toString(), accountJson);
          });
          String fileName = String.format("%sQualityEconomy %s.json", path, LocalDateTime.now().format(EXPORT_DATE_FORMAT));
          try (FileWriter file = new FileWriter(fileName)) {
            file.write(rootJson.toString());
          } catch (IOException exception) {
            new QualityError("Error while exporting playerdata", exception).log();
          }
          timer.end("Exported database");
        }
      }.runTaskAsynchronously(QualityEconomy.getInstance());
    }
  }
  
  public static void importDatabase(final String fileName) {
    synchronized (StorageManager.lock) {
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
            new QualityError("Error while importing playerdata", exception).log();
          }
          timer.end("Imported database");
          AccountManager.setupAccounts();
        }
      }.runTaskAsynchronously(QualityEconomy.getInstance());
    }
  }
  
}
