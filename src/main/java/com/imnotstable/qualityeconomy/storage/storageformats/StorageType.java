package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.storage.accounts.Account;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface StorageType {
  
  Connection getConnection() throws SQLException;
  
  boolean initStorageProcesses();
  
  void endStorageProcesses();
  
  void wipeDatabase();
  
  void createAccount(Account account);
  
  void createAccounts(Collection<Account> accounts);
  
  Map<UUID, Account> getAllAccounts();
  
  void updateAccounts(Collection<Account> accounts);
  
  List<String> getCurrencies();
  
  void addCurrency(String currencyName);
  
  void removeCurrency(String currencyName);
  
}
