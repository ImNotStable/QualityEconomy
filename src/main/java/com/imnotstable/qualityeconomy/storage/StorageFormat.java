package com.imnotstable.qualityeconomy.storage;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface StorageFormat {

  boolean initStorageProcesses();

  void endStorageProcesses();

  void wipeDatabase();

  boolean createAccount(Account account);

  void createAccounts(Collection<Account> accounts);

  boolean accountExists(UUID uuid);

  Account getAccount(UUID uuid);

  Map<UUID, Account> getAccounts(Collection<UUID> uuids);

  Map<UUID, Account> getAllAccounts();

  void updateAccount(Account account);

  void updateAccounts(Collection<Account> accounts);

  Collection<UUID> getAllUUIDs();

}
