package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.economy.Account;
import com.imnotstable.qualityeconomy.economy.BalanceEntry;
import com.imnotstable.qualityeconomy.storage.AccountManager;
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
    if (dataSource == null) {
      Logger.logError("Attempted to initiate datasource when datasource doesn't exist");
      return false;
    }
    if (dataSource.isClosed()) {
      Logger.logError("Attempted to initiate datasource when datasource is already closed");
      return false;
    }
    try (Connection connection = getConnection()) {
      createAccountsTable(connection);
      createBalancesTable(connection);
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
      dropBalancesTable(connection);
      dropAccountsTable(connection);
      createAccountsTable(connection);
      createBalancesTable(connection);
    } catch (SQLException exception) {
      Logger.logError("Failed to wipe database", exception);
    }
  }
  
  @Override
  public void createAccount(@NotNull Account account) {
    try (Connection connection = getConnection();
         PreparedStatement accountStatement = connection.prepareStatement(getInsertAccountStatement());
         PreparedStatement balanceStatement = connection.prepareStatement(getInsertBalanceStatement())) {
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
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNTS");
         PreparedStatement balanceStatement = connection.prepareStatement("SELECT * FROM BALANCES WHERE UUID = ?")) {
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        UUID uuid = UUID.fromString(resultSet.getString("UUID"));
        Account account = new Account(uuid)
          .setUsername(resultSet.getString("USERNAME"));
        balanceStatement.setString(1, uuid.toString());
        ResultSet balanceResultSet = balanceStatement.executeQuery();
        while (balanceResultSet.next()) {
          account.updateBalanceEntry(new BalanceEntry(
            balanceResultSet.getString("CURRENCY"),
            balanceResultSet.getDouble("BALANCE"),
            balanceResultSet.getBoolean("PAYABLE")));
        }
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
           PreparedStatement balanceStatement = connection.prepareStatement(getUpsertBalanceStatement())) {
        for (Account account : AccountManager.getAllAccounts()) {
          accountStatement.setString(1, account.getUsername());
          accountStatement.setString(2, account.getUniqueId().toString());
          for (BalanceEntry entry : account.getBalanceEntries()) {
            balanceStatement.setString(1, account.getUniqueId().toString());
            balanceStatement.setString(2, entry.getCurrency());
            balanceStatement.setDouble(3, entry.getBalance());
            balanceStatement.setBoolean(4, entry.isPayable());
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
