package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.storage.accounts.Account;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface StorageType {
  
  boolean initStorageProcesses();
  
  void endStorageProcesses();
  
  void wipeDatabase();
  
  void createAccount(@NotNull Account account);
  
  void createAccounts(@NotNull Collection<Account> accounts);
  
  void updateAccounts(@NotNull Collection<Account> accounts);
  
  @NotNull Map<UUID, Account> getAllAccounts();
  
  @NotNull List<String> getCurrencies();
  
  void addCurrency(@NotNull String currency);
  
  void removeCurrency(@NotNull String currency);
  
}
