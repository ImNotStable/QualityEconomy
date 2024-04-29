package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.economy.Account;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface StorageType {
  
  boolean initStorageProcesses();
  
  void endStorageProcesses();
  
  void wipeDatabase();
  
  void createAccount(@NotNull Account account);
  
  void createAccounts(@NotNull Collection<Account> accounts);
  
  void saveAllAccounts();
  
  @NotNull
  Map<UUID, Account> getAllAccounts();
  
}
