package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.commands.CommandManager;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.storage.EasySQL;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SQLStorageType extends EasySQL implements StorageType {
  
  public SQLStorageType(int databaseType) {
    super(databaseType);
  }
  
  @Override
  public boolean initStorageProcesses() {
    if (dataSource != null && !dataSource.isClosed()) {
      new Debug.QualityError("Attempted to open datasource when datasource already exists").log();
      return false;
    }
    open();
    try (Connection connection = getConnection()) {
      createPlayerDataTable(connection);
      toggleCurrencyTable(connection);
      toggleColumns(connection);
      columns = getColumns(connection);
      generateStatements();
    } catch (SQLException exception) {
      new Debug.QualityError("Error while initiating storage processes", exception).log();
      return false;
    }
    return true;
  }
  
  @Override
  public void endStorageProcesses() {
    if (dataSource == null) {
      new Debug.QualityError("Attempted to close datasource when datasource doesn't exist").log();
      return;
    }
    if (dataSource.isClosed()) {
      new Debug.QualityError("Attempted to close datasource when datasource is already closed").log();
      return;
    }
    close();
  }
  
  @Override
  public synchronized void wipeDatabase() {
    try (Connection connection = getConnection()) {
      dropPlayerDataTable(connection);
      if (QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES)
        dropCurrencyTable(connection);
      endStorageProcesses();
      initStorageProcesses();
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to wipe database", exception).log();
    }
  }
  
  @Override
  public synchronized void createAccount(@NotNull Account account) {
    try (Connection connection = getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(getInsertStatement())) {
      createAccountSetter(preparedStatement, account);
      int affectedRows = preparedStatement.executeUpdate();
      
      if (affectedRows == 0) {
        new Debug.QualityError("Failed to create account (" + account.getUniqueId() + ")").log();
      }
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to create account (" + account.getUniqueId() + ")", exception).log();
    }
  }
  
  @Override
  public synchronized void createAccounts(@NotNull Collection<Account> accounts) {
    if (accounts.isEmpty())
      return;
    
    try (Connection connection = getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertStatement())) {
        connection.setAutoCommit(false);
        
        for (Account account : accounts) {
          createAccountSetter(preparedStatement, account);
          preparedStatement.addBatch();
        }
        
        preparedStatement.executeBatch();
        connection.commit();
      } catch (SQLException exception) {
        new Debug.QualityError("Failed to create accounts", exception).log();
        connection.rollback();
      }
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to rollback transaction", exception).log();
    }
  }
  
  @Override
  public synchronized @NotNull Map<UUID, Account> getAllAccounts() {
    Map<UUID, Account> accounts = new HashMap<>();
    
    try (Connection connection = getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PLAYERDATA")) {
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        UUID uuid = UUID.fromString(resultSet.getString("UUID"));
        Account account = new Account(uuid)
          .setUsername(resultSet.getString("USERNAME"))
          .setBalance(resultSet.getDouble("BALANCE"));
        if (QualityEconomy.getQualityConfig().COMMANDS_PAY)
          account.setPayable(resultSet.getBoolean("PAYABLE"));
        if (QualityEconomy.getQualityConfig().COMMANDS_REQUEST)
          account.setRequestable(resultSet.getBoolean("REQUESTABLE"));
        if (QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES) {
          Map<String, Double> customCurrencies = new HashMap<>();
          for (String currency : currencies) {
            customCurrencies.put(currency, resultSet.getDouble(currency));
          }
          account.setCustomBalances(customCurrencies);
        }
        accounts.put(uuid, account);
      }
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to get all accounts", exception);
    }
    return accounts;
  }
  
  @Override
  public synchronized void saveAllAccounts() {
    try (Connection connection = getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(getUpdateStatement())) {
        connection.setAutoCommit(false);
        for (Account account : AccountManager.getAllAccounts()) {
          if (!account.requiresUpdate())
            continue;
          account.update();
          preparedStatement.setString(1, account.getUsername());
          preparedStatement.setDouble(2, account.getBalance());
          if (QualityEconomy.getQualityConfig().COMMANDS_PAY)
            preparedStatement.setBoolean(columns.indexOf("PAYABLE"), account.isPayable());
          if (QualityEconomy.getQualityConfig().COMMANDS_REQUEST)
            preparedStatement.setBoolean(columns.indexOf("REQUESTABLE"), account.isRequestable());
          if (QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES)
            for (String currency : currencies)
              preparedStatement.setDouble(columns.indexOf(currency), account.getCustomBalance(currency));
          preparedStatement.setString(columns.size(), account.getUniqueId().toString());
          preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        connection.commit();
      } catch (SQLException exception) {
        new Debug.QualityError("Failed to update accounts", exception).log();
        connection.rollback();
      }
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to rollback transaction", exception).log();
    }
  }
  
  @Override
  public boolean addCurrency(@NotNull String currency) {
    try (Connection connection = getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO CURRENCIES(CURRENCY) VALUES(?)")) {
        preparedStatement.setString(1, currency);
        preparedStatement.executeUpdate();
        addColumn(connection, currency, "FLOAT(53)", "0.0");
      } catch (SQLException exception) {
        new Debug.QualityError("Failed to add currency to database (" + currency + ")", exception).log();
        connection.rollback();
        return false;
      }
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to retrieve connection to database or rollback", exception).log();
      return false;
    }
    super.currencies.add(currency);
    return true;
  }
  
  @Override
  public boolean removeCurrency(@NotNull String currency) {
    try (Connection connection = getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM CURRENCIES WHERE CURRENCY = ?")) {
        preparedStatement.setString(1, currency);
        preparedStatement.executeUpdate();
        dropColumn(connection, currency);
      } catch (SQLException exception) {
        new Debug.QualityError("Failed to remove currency from database (" + currency + ")", exception).log();
        connection.rollback();
        return false;
      }
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to retrieve connection to database or rollback", exception).log();
      return false;
    }
    super.currencies.remove(currency);
    return true;
  }
  
  private void toggleCurrencyTable(Connection connection) throws SQLException {
    boolean tableExists = currencyTableExists(connection.getMetaData());
    if (tableExists) {
      try (ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM CURRENCIES")) {
        while (resultSet.next())
          currencies.add(resultSet.getString(1));
      }
    }
    if (QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES && !tableExists)
      createCurrencyTable(connection);
    else if (!QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES && tableExists)
      dropCurrencyTable(connection);
  }
  
  private void toggleColumns(Connection connection) throws SQLException {
    DatabaseMetaData metaData = connection.getMetaData();
    if (QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES) {
      CommandManager.getCommand("custombalance").register();
      CommandManager.getCommand("customeconomy").register();
      for (String currency : currencies)
        if (!columnExists(metaData, currency))
          addColumn(connection, currency, "FLOAT(53)", "0.0");
    } else {
      CommandManager.getCommand("custombalance").unregister();
      CommandManager.getCommand("customeconomy").unregister();
      for (String currency : currencies)
        if (columnExists(metaData, currency))
          dropColumn(connection, currency);
    }
    boolean payableExists = columnExists(metaData, "PAYABLE");
    if (QualityEconomy.getQualityConfig().COMMANDS_PAY && !payableExists)
      addColumn(connection, "PAYABLE", "BOOLEAN", "TRUE");
    else if (!QualityEconomy.getQualityConfig().COMMANDS_PAY && payableExists)
      dropColumn(connection, "PAYABLE");
    boolean requestableExists = columnExists(metaData, "REQUESTABLE");
    if (QualityEconomy.getQualityConfig().COMMANDS_REQUEST && !requestableExists)
      addColumn(connection, "REQUESTABLE", "BOOLEAN", "FALSE");
    else if (!QualityEconomy.getQualityConfig().COMMANDS_REQUEST && requestableExists)
      dropColumn(connection, "REQUESTABLE");
  }
  
}
