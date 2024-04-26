package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.storage.accounts.BalanceEntry;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import com.imnotstable.qualityeconomy.util.storage.EasySQL;
import com.imnotstable.qualityeconomy.util.storage.SQLDriver;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SQLStorageType extends EasySQL implements StorageType {
  
  public SQLStorageType(SQLDriver databaseType) {
    super(databaseType);
  }
  
  @Override
  public boolean initStorageProcesses() {
    if (dataSource != null && !dataSource.isClosed()) {
      Logger.logError("Attempted to open datasource when datasource already exists");
      return false;
    }
    open();
    try (Connection connection = getConnection()) {
      createAccountsTable(connection);
      createBalanceTable(connection);
    } catch (SQLException exception) {
      Logger.logError("Error while initiating storage processes", exception);
      return false;
    }
    return true;
  }
  
  @Override
  public void endStorageProcesses() {
    if (dataSource == null) {
      Logger.logError("Attempted to close datasource when datasource doesn't exist");
      return;
    }
    if (dataSource.isClosed()) {
      Logger.logError("Attempted to close datasource when datasource is already closed");
      return;
    }
    close();
  }
  
  @Override
  public void wipeDatabase() {
    try (Connection connection = getConnection()) {
      dropAccountsTable(connection);
      dropBalanceTable(connection);
      endStorageProcesses();
      initStorageProcesses();
    } catch (SQLException exception) {
      Logger.logError("Failed to wipe database", exception);
    }
  }
  
  @Override
  public void createAccount(@NotNull Account account) {
    try (Connection connection = getConnection();
         PreparedStatement accountStatement = connection.prepareStatement(getInsertAccountStatement());
         PreparedStatement balanceStatement = connection.prepareStatement(getInsertBalanceStatement())) {
      connection.setAutoCommit(false);
      UUID uuid = account.getUniqueId();
      accountStatement.setString(1, uuid.toString());
      accountStatement.setString(2, account.getUsername());
      for (BalanceEntry entry : account.getBalanceEntries()) {
        balanceStatement.setString(1, uuid.toString());
        balanceStatement.setString(2, entry.getCurrency());
        balanceStatement.setDouble(3, entry.getBalance());
        balanceStatement.setBoolean(4, entry.isPayable());
        balanceStatement.addBatch();
      }
      int affectedAccountRows = accountStatement.executeUpdate();
      int[] affectedBalanceRows = balanceStatement.executeBatch();
      connection.commit();
      if (affectedAccountRows == 0) {
        Logger.logError("Failed to create account (" + account.getUniqueId() + ")");
      }
      for (int affectedRows : affectedBalanceRows)
        if (affectedRows == 0)
          Logger.logError("Failed to create balance entry for account (" + account.getUniqueId() + ")");
    } catch (SQLException exception) {
      Logger.logError("Failed to create account (" + account.getUniqueId() + ")", exception);
    }
  }
  
  @Override
  public void createAccounts(@NotNull Collection<Account> accounts) {
    if (accounts.isEmpty())
      return;
    
    try (Connection connection = getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertAccountStatement());
           PreparedStatement balanceStatement = connection.prepareStatement(getInsertBalanceStatement())) {
        connection.setAutoCommit(false);
        
        for (Account account : accounts) {
          UUID uuid = account.getUniqueId();
          preparedStatement.setString(1, uuid.toString());
          preparedStatement.setString(2, account.getUsername());
          preparedStatement.addBatch();
          for (BalanceEntry entry : account.getBalanceEntries()) {
            balanceStatement.setString(1, uuid.toString());
            balanceStatement.setString(2, entry.getCurrency());
            balanceStatement.setDouble(3, entry.getBalance());
            balanceStatement.setBoolean(4, entry.isPayable());
            balanceStatement.addBatch();
          }
        }
        preparedStatement.executeBatch();
        balanceStatement.executeBatch();
        connection.commit();
      } catch (SQLException exception) {
        Logger.logError("Failed to create accounts", exception);
        connection.rollback();
      }
    } catch (SQLException exception) {
      Logger.logError("Failed to rollback transaction", exception);
    }
  }
  
  @Override
  public @NotNull Map<UUID, Account> getAllAccounts() {
    Map<UUID, Account> accounts = new HashMap<>();
    
    try (Connection connection = getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNTS")) {
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        UUID uuid = UUID.fromString(resultSet.getString("UUID"));
        Account account = new Account(uuid)
          .setUsername(resultSet.getString("USERNAME"));
        
        accounts.put(uuid, account);
      }
    } catch (SQLException exception) {
      Logger.logError("Failed to get all accounts", exception);
    }
    return accounts;
  }
  
  @Override
  public void saveAllAccounts() {
    try (Connection connection = getConnection()) {
      try (PreparedStatement accountStatement = connection.prepareStatement(getUpdateAccountStatement());
           PreparedStatement balanceStatement = connection.prepareStatement(getUpdateBalanceStatement())) {
        connection.setAutoCommit(false);
        for (Account account : AccountManager.getAllAccounts()) {
          accountStatement.setString(1, account.getUsername());
          accountStatement.setString(2, account.getUniqueId().toString());
          for (BalanceEntry entry : account.getBalanceEntries()) {
            balanceStatement.setDouble(1, entry.getBalance());
            balanceStatement.setBoolean(2, entry.isPayable());
            balanceStatement.setString(3, account.getUniqueId().toString());
            balanceStatement.setString(4, entry.getCurrency());
            balanceStatement.addBatch();
          }
          accountStatement.addBatch();
        }
        accountStatement.executeBatch();
        balanceStatement.executeBatch();
        connection.commit();
      } catch (SQLException exception) {
        Logger.logError("Failed to update accounts", exception);
        connection.rollback();
      }
    } catch (SQLException exception) {
      Logger.logError("Failed to rollback transaction", exception);
    }
  }
  
}
