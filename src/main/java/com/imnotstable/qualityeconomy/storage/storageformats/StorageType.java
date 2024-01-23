package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.storage.accounts.Account;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface StorageType {
  
  boolean initStorageProcesses();
  
  void endStorageProcesses();
  
  void wipeDatabase();
  
  void createAccount(Account account);
  
  void createAccounts(Collection<Account> accounts);
  
  void updateAccounts(Collection<Account> accounts);
  
  Map<UUID, Account> getAllAccounts();
  
  List<String> getCurrencies();
  
  void addCurrency(String currency);
  
  void removeCurrency(String currency);
  
}
