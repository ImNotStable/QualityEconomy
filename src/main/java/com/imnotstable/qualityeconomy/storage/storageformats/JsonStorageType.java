package com.imnotstable.qualityeconomy.storage.storageformats;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.util.Misc;
import com.imnotstable.qualityeconomy.util.QualityError;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonStorageType implements StorageType {
  
  private final HashMap<UUID, JsonObject> configurations = new HashMap<>();
  private final File DIRECTORY = new File(QualityEconomy.getInstance().getDataFolder() + "/playerdata");
  private final Gson gson = new Gson();
  
  private File getFile(UUID uuid) {
    return new File(DIRECTORY, uuid + ".json");
  }
  
  private JsonObject getConfiguration(UUID uuid) {
    return configurations.containsKey(uuid) ? configurations.get(uuid) : createAccount(new Account(uuid)) ? configurations.get(uuid) : null;
  }
  
  @Override
  public boolean initStorageProcesses() {
    return DIRECTORY.mkdir();
  }
  
  @Override
  public void endStorageProcesses() {
  }
  
  @Override
  public void wipeDatabase() {
    if (!DIRECTORY.isDirectory())
      return;
    
    if (!DIRECTORY.delete()) {
      new QualityError("Failed to wipe playerdata").log();
      return;
    }
    
    DIRECTORY.mkdir();
  }
  
  @Override
  public boolean createAccount(Account account) {
    UUID uuid = account.getUUID();
    File file = getFile(uuid);
    
    JsonObject configuration;
    try {
      if (!file.exists()) {
        configuration = new JsonObject();
        configuration.addProperty("name", account.getName());
        configuration.addProperty("balance", account.getBalance());
        configuration.addProperty("payable", account.getPayable());
        for (String currency : CustomCurrencies.getCustomCurrencies()) {
          configuration.addProperty(currency, account.getCustomBalance(currency));
        }
      } else {
        String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
        configuration = JsonParser.parseString(content).getAsJsonObject();
      }
      configurations.put(uuid, configuration);
      try {
        Files.write(Paths.get(getFile(account.getUUID()).getPath()), gson.toJson(configuration).getBytes());
      } catch (IOException exception) {
        new QualityError("Failed to save account (" + account.getUUID() + ")", exception).log();
      }
      return accountExists(uuid);
    } catch (IOException exception) {
      new QualityError("Failed to create account (" + uuid.toString() + ")", exception).log();
    }
    return false;
  }
  
  @Override
  public void createAccounts(Collection<Account> accounts) {
    accounts.forEach(this::createAccount);
  }
  
  @Override
  public boolean accountExists(UUID uuid) {
    return configurations.containsKey(uuid) || getFile(uuid).exists();
  }
  
  public Account getAccount(UUID uuid) {
    JsonObject configuration = getConfiguration(uuid);
    if (!accountExists(uuid))
      return new Account(uuid);
    
    Map<String, Double> balanceMap = new HashMap<>();
    for (String currency : CustomCurrencies.getCustomCurrencies()) {
      balanceMap.put(currency, configuration != null ? configuration.get(currency).getAsDouble() : 0.0);
    }
    
    return new Account(uuid)
      .setName(Optional.ofNullable(configuration.get("name").getAsString()).orElse("NULL"))
      .setBalance(Optional.of(configuration.get("balance").getAsDouble()).orElse(0.0))
      .setPayable(Optional.of(configuration.get("payable").getAsBoolean()).orElse(true))
      .setCustomBalances(balanceMap);
  }

  
  @Override
  public Map<UUID, Account> getAccounts(Collection<UUID> uuids) {
    Map<UUID, Account> accounts = new HashMap<>();
    uuids.forEach(uuid -> accounts.put(uuid, getAccount(uuid)));
    return accounts;
  }
  
  @Override
  public Map<UUID, Account> getAllAccounts() {
    return getAccounts(getAllUUIDs());
  }
  
  @Override
  public void updateAccount(Account account) {
    JsonObject configuration = getConfiguration(account.getUUID());
    if (configuration != null) {
      configuration.addProperty("name", account.getName());
      configuration.addProperty("balance", account.getBalance());
      configuration.addProperty("payable", account.getPayable());
      for (String currency : CustomCurrencies.getCustomCurrencies()) {
        configuration.addProperty(currency, account.getCustomBalance(currency));
      }
      try {
        Files.write(Paths.get(getFile(account.getUUID()).getPath()), gson.toJson(configuration).getBytes());
      } catch (IOException exception) {
        new QualityError("Failed to save account (" + account.getUUID() + ")", exception).log();
      }
    } else {
      new QualityError("Failed to find account (" + account.getUUID().toString() + ")").log();
    }
  }

  
  @Override
  public void updateAccounts(Collection<Account> accounts) {
    accounts.forEach(this::updateAccount);
  }
  
  @Override
  public Collection<UUID> getAllUUIDs() {
    Path dirPath = Paths.get(DIRECTORY.getPath());
    
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, "*.json")) {
      return StreamSupport.stream(stream.spliterator(), false)
        .map(path -> path.getFileName().toString())
        .map(fileName -> fileName.substring(0, fileName.lastIndexOf('.')))
        .filter(Misc::isValidUUID)
        .map(UUID::fromString)
        .collect(Collectors.toList());
    } catch (IOException exception) {
      new QualityError("Failed to collect all uuids.", exception).log();
    }
    return Collections.emptyList();
  }

  @Override
  public void addCurrency(String currencyName) {}
  
  @Override
  public void removeCurrency(String currencyName) {}
  
}
