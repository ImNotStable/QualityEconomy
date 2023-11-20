package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;
import com.imnotstable.qualityeconomy.util.QualityError;
import com.imnotstable.qualityeconomy.util.Misc;
import org.json.JSONObject;

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
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonStorageType implements StorageType {
  
  private final HashMap<UUID, JSONObject> configurations = new HashMap<>();
  private final File DIRECTORY = new File(QualityEconomy.getInstance().getDataFolder() + "/playerdata");
  
  private File getFile(UUID uuid) {
    return new File(DIRECTORY, uuid + ".json");
  }
  
  private JSONObject getConfiguration(UUID uuid) {
    return configurations.containsKey(uuid) ? configurations.get(uuid) : createAccount(new Account(uuid)) ? configurations.get(uuid) : null;
  }
  
  @Override
  public boolean initStorageProcesses() {
    DIRECTORY.mkdir();
    return true;
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
    
    JSONObject configuration;
    try {
      if (!file.exists()) {
        configuration = new JSONObject();
        configuration.put("name", account.getName());
        configuration.put("balance", account.getBalance());
        configuration.put("payable", account.getPayable());
        for (String currency : CustomCurrencies.getCustomCurrencies()) {
          configuration.put(currency, account.getCustomBalance(currency));
        }
      } else {
        String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
        configuration = new JSONObject(content);
      }
      configurations.put(uuid, configuration);
      try {
        Files.write(Paths.get(getFile(account.getUUID()).getPath()), configuration.toString().getBytes());
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
    JSONObject configuration = getConfiguration(uuid);
    if (!accountExists(uuid))
      return new Account(uuid);
    
    Map<String, Double> balanceMap = new HashMap<>();
    for (String currency : CustomCurrencies.getCustomCurrencies()) {
      balanceMap.put(currency, configuration != null ? configuration.optDouble(currency, 0.0) : 0.0);
    }
    
    return new Account(uuid)
      .setName(configuration.optString("name"))
      .setBalance(configuration.optDouble("balance"))
      .setPayable(configuration.optBoolean("payable"))
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
    JSONObject configuration = getConfiguration(account.getUUID());
    if (configuration != null) {
      configuration.put("name", account.getName());
      configuration.put("balance", account.getBalance());
      configuration.put("payable", account.getPayable());
      for (String currency : CustomCurrencies.getCustomCurrencies()) {
        configuration.put(currency, account.getCustomBalance(currency));
      }
      try {
        Files.write(Paths.get(getFile(account.getUUID()).getPath()), configurations.get(account.getUUID()).toString().getBytes());
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
