package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.Account;
import com.imnotstable.qualityeconomy.storage.StorageFormat;
import com.imnotstable.qualityeconomy.util.Error;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JsonStorageFormat implements StorageFormat {
  
  private final HashMap<UUID, JSONObject> accounts = new HashMap<>();
  private final String PATH = QualityEconomy.getInstance().getDataFolder().getPath() + "/playerdata/";
  
  private File getFile(UUID uuid) {
    return new File(PATH + uuid + ".json");
  }
  
  private JSONObject getConfiguration(UUID uuid) {
    return accounts.containsKey(uuid) ? accounts.get(uuid) : createAccount(new Account(uuid)) ? accounts.get(uuid) : null;
  }
  
  @Override
  public boolean initStorageProcesses() {
    //noinspection ResultOfMethodCallIgnored
    new File(PATH).mkdir();
    return true;
  }
  
  @Override
  public void endStorageProcesses() {
  }
  
  @Override
  public void wipeDatabase() {
    File dir = new File(PATH);
    
    if (!dir.exists())
      return;
    
    if (!dir.isDirectory())
      return;
    
    if (!dir.delete()) {
      new Error("Failed to wipe database").log();
    }
    
    //noinspection ResultOfMethodCallIgnored
    dir.mkdir();
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
        configuration.put("secondaryBalance", account.getSecondaryBalance());
        configuration.put("payable", account.getPayable());
      } else {
        String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
        configuration = new JSONObject(content);
      }
      accounts.put(uuid, configuration);
      try {
        Files.write(Paths.get(getFile(account.getUUID()).getPath()), configuration.toString().getBytes());
      } catch (IOException exception) {
        new Error("Failed to save account (" + account.getUUID() + ")", exception).log();
      }
      return accountExists(uuid);
    } catch (IOException exception) {
      new Error("Failed to create account (" + uuid.toString() + ")", exception).log();
    }
    return false;
  }
  
  @Override
  public void createAccounts(Collection<Account> accounts) {
    accounts.forEach(this::createAccount);
  }
  
  @Override
  public boolean accountExists(UUID uuid) {
    return accounts.containsKey(uuid) || getFile(uuid).exists();
  }
  
  public Account getAccount(UUID uuid) {
    JSONObject configuration = getConfiguration(uuid);
    String name = configuration != null ? configuration.getString("name") : "null";
    double balance = configuration != null ? configuration.getDouble("balance") : 0d;
    double secondaryBalance = configuration != null ? configuration.getDouble("secondaryBalance") : 0d;
    boolean payable = configuration == null || configuration.getBoolean("payable");
    return new Account(uuid).setName(name).setBalance(balance).setSecondaryBalance(secondaryBalance).setPayable(payable);
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
      configuration.put("secondaryBalance", account.getSecondaryBalance());
      configuration.put("payable", account.getPayable());
    }
    try {
      Files.write(Paths.get(getFile(account.getUUID()).getPath()), accounts.get(account.getUUID()).toString().getBytes());
    } catch (IOException exception) {
      new Error("Failed to save account (" + account.getUUID() + ")", exception).log();
    }
  }
  
  @Override
  public void updateAccounts(Collection<Account> accounts) {
    accounts.forEach(this::updateAccount);
  }
  
  @Override
  public Collection<UUID> getAllUUIDs() {
    Collection<UUID> uuids = new ArrayList<>();
    
    File[] files = new File(PATH).listFiles();
    
    if (files == null) {
      return uuids;
    }
    
    for (File file : files) {
      if (!file.isFile()) {
        continue;
      }
      
      String name = file.getName();
      
      if (!name.endsWith(".json")) {
        continue;
      }
      
      int lastIndex = name.lastIndexOf('.');
      
      if (lastIndex > 0) {
        String uuidString = name.substring(0, lastIndex);
        if (com.imnotstable.qualityeconomy.util.UUID.isValidUUID(uuidString)) {
          uuids.add(UUID.fromString(uuidString));
        }
      }
    }
    
    return uuids;
  }
  
}
