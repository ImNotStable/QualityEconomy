package com.imnotstable.qualityeconomy.storage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.storage.storageformats.StorageType;
import com.imnotstable.qualityeconomy.util.Logger;
import com.imnotstable.qualityeconomy.util.QualityError;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.scheduler.BukkitRunnable;

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
    new BukkitRunnable() {
      @Override
      public void run() {
        TestToolkit.Timer timer = new TestToolkit.Timer("Exporting database...");
        AccountManager.saveAllAccounts();
        File directory = new File(path);
        if (!directory.exists() || !directory.isDirectory()) {
          if (!directory.mkdir())
            Logger.log(Component.text("Failed to create directory for database export", NamedTextColor.RED));
        }
        Gson gson = new Gson();
        JsonObject rootJson = new JsonObject();
        rootJson.add("custom-currencies", gson.toJsonTree(CustomCurrencies.getCustomCurrencies()));
        StorageManager.getActiveStorageFormat().getAllAccounts().forEach((uuid, account) -> {
          JsonObject accountJson = new JsonObject();
          accountJson.addProperty("name", account.getName());
          accountJson.addProperty("balance", account.getBalance());
          accountJson.addProperty("payable", account.isPayable());
          accountJson.addProperty("requestable", account.isRequestable());
          account.getCustomBalances().forEach(accountJson::addProperty);
          rootJson.add(uuid.toString(), accountJson);
        });
        String fileName = String.format("%sQualityEconomy %s.json", path, LocalDateTime.now().format(EXPORT_DATE_FORMAT));
        try (FileWriter file = new FileWriter(fileName)) {
          file.write(gson.toJson(rootJson));
        } catch (IOException exception) {
          new QualityError("Error while exporting playerdata", exception).log();
        }
        timer.end("Exported database");
      }
    }.runTaskAsynchronously(QualityEconomy.getInstance());
  }
  
  
  public static void importDatabase(final String fileName) {
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
          JsonObject rootJson = JsonParser.parseString(content).getAsJsonObject();
          
          List<String> customCurrencies = new ArrayList<>();
          if (rootJson.get("custom-currencies") != null) {
            JsonArray currenciesArray = rootJson.getAsJsonArray("custom-currencies");
            for (JsonElement currencyElement : currenciesArray) {
              String currency = currencyElement.getAsString();
              customCurrencies.add(currency);
              CustomCurrencies.createCustomCurrency(currency);
            }
          }
          
          rootJson.entrySet().stream()
            .filter(entry -> !entry.getKey().equalsIgnoreCase("custom-currencies"))
            .forEach(entry -> {
              UUID uuid = UUID.fromString(entry.getKey());
              JsonObject accountJson = entry.getValue().getAsJsonObject();
              String name = accountJson.get("name").getAsString();
              double balance = accountJson.get("balance").getAsDouble();
              boolean payable = accountJson.get("payable").getAsBoolean();
              boolean requestable = accountJson.get("requestable").getAsBoolean();
              Map<String, Double> balanceMap = new HashMap<>();
              for (String currency : customCurrencies) {
                balanceMap.put(currency, accountJson.get(currency).getAsDouble());
              }
              accounts.add(new Account(uuid).setName(name).setBalance(balance).setPayable(payable).setRequestable(requestable).setCustomBalances(balanceMap));
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
