package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.Account;
import com.imnotstable.qualityeconomy.util.Error;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class YamlStorageFormat implements StorageFormat {
  
  private final HashMap<UUID, YamlConfiguration> configurations = new HashMap<>();
  private final String PATH = QualityEconomy.getInstance().getDataFolder().getPath() + "/playerdata/";
  
  private File getFile(UUID uuid) {
    return new File(PATH + uuid + ".yml");
  }
  
  private YamlConfiguration getConfiguration(UUID uuid) {
    return configurations.containsKey(uuid) ? configurations.get(uuid) : createAccount(new Account(uuid)) ? configurations.get(uuid) : null;
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
    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(getFile(uuid));
    configuration.set("name", account.getName());
    configuration.set("balance", account.getBalance());
    configuration.set("secondaryBalance", account.getSecondaryBalance());
    configuration.set("payable", account.getPayable());
    configurations.put(uuid, configuration);
    try {
      configuration.save(getFile(account.getUUID()));
    } catch (IOException exception) {
      new Error("Failed to save account (" + account.getUUID() + ")", exception).log();
    }
    return accountExists(uuid);
  }
  
  @Override
  public void createAccounts(Collection<Account> accounts) {
    accounts.forEach(this::createAccount);
  }
  
  @Override
  public boolean accountExists(UUID uuid) {
    return configurations.containsKey(uuid) || getFile(uuid).exists();
  }
  
  
  @Override
  public Account getAccount(UUID uuid) {
    YamlConfiguration configuration = getConfiguration(uuid);
    String name = configuration != null ? configuration.getString("name") : uuid.toString();
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
    YamlConfiguration configuration = getConfiguration(account.getUUID());
    if (configuration != null) {
      configuration.set("name", account.getName());
      configuration.set("balance", account.getBalance());
      configuration.set("secondaryBalance", account.getSecondaryBalance());
      configuration.set("payable", account.getPayable());
    }
    try {
      configurations.get(account.getUUID()).save(getFile(account.getUUID()));
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
      
      if (!name.endsWith(".yml")) {
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
