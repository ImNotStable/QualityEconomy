package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.util.storage.EasyCurrencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RedisStorageType extends EasyCurrencies implements StorageType {
  
  @Override
  public boolean initStorageProcesses() throws UnsupportedOperationException {
    throw new UnsupportedOperationException("Redis is not implemented yet.");
  }
  
  @Override
  public void endStorageProcesses() {
  }
  
  @Override
  public void wipeDatabase() {
  
  }
  
  @Override
  public void createAccount(Account account) {
  }
  
  @Override
  public void createAccounts(Collection<Account> accounts) {
  }
  
  @Override
  public void updateAccounts(Collection<Account> accounts) {
  }
  
  @Override
  public Map<UUID, Account> getAllAccounts() {
    return new HashMap<>();
  }
  
  @Override
  public List<String> getCurrencies() {
    return new ArrayList<>();
  }
  
  @Override
  public void addCurrency(String currency) {
  }
  
  @Override
  public void removeCurrency(String currency) {
  }
  
}
