package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.commands.CommandManager;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.EasySQL;
import com.imnotstable.qualityeconomy.util.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SQLStorageType extends EasySQL implements StorageType {
  
  private final List<String> currencies = new ArrayList<>();
  
  public SQLStorageType(int databaseType) {
    super(databaseType);
  }
  
  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }
  
  @Override
  public boolean initStorageProcesses() {
    if (dataSource == null || dataSource.isClosed())
      return false;
    try (Connection connection = getConnection()) {
      toggleCurrencyTable(connection);
      toggleCurrencyColumns(connection);
      togglePayableColumn(connection);
      toggleRequestableColumn(connection);
    } catch (SQLException exception) {
      new Debug.QualityError("Error while initiating storage processes", exception).log();
      return false;
    }
    Logger.log(Component.text("Successfully initiated storage processes", NamedTextColor.GREEN));
    return true;
  }
  
  @Override
  public void endStorageProcesses() {
    close();
  }
  
  public synchronized void wipeDatabase() {
    try (Connection connection = getConnection();
         Statement statement = connection.createStatement()) {
      statement.executeUpdate("DROP TABLE PLAYERDATA");
      if (Configuration.areCustomCurrenciesEnabled())
        statement.executeUpdate("DROP TABLE CURRENCIES");
      close();
      open();
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to wipe database", exception).log();
    }
  }
  
  @Override
  public synchronized void createAccount(Account account) {
    try (Connection connection = getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(getInsertStatement())) {
      UUID uuid = account.getUUID();
      preparedStatement.setString(1, uuid.toString());
      preparedStatement.setString(2, account.getName());
      preparedStatement.setDouble(3, account.getBalance());
      if (Configuration.isCommandEnabled("pay"))
        preparedStatement.setBoolean(columns.indexOf("PAYABLE") + 1, account.isPayable());
      if (Configuration.isCommandEnabled("request"))
        preparedStatement.setBoolean(columns.indexOf("REQUESTABLE") + 1, account.isRequestable());
      if (Configuration.areCustomCurrenciesEnabled())
        for (String currency : currencies)
          preparedStatement.setDouble(columns.indexOf(currency) + 1, account.getCustomBalance(currency));
      int affectedRows = preparedStatement.executeUpdate();
      
      if (affectedRows == 0) {
        new Debug.QualityError("Failed to create account (" + uuid + ")").log();
      }
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to create account (" + account.getUUID().toString() + ")", exception).log();
    }
  }
  
  public synchronized void createAccounts(Collection<Account> accounts) {
    if (accounts.isEmpty())
      return;
    
    try (Connection connection = getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertStatement())) {
        connection.setAutoCommit(false);
        
        for (Account account : accounts) {
          UUID uuid = account.getUUID();
          preparedStatement.setString(1, uuid.toString());
          preparedStatement.setString(2, account.getName());
          preparedStatement.setDouble(3, account.getBalance());
          if (Configuration.isCommandEnabled("pay"))
            preparedStatement.setBoolean(columns.indexOf("PAYABLE") + 1, account.isPayable());
          if (Configuration.isCommandEnabled("request"))
            preparedStatement.setBoolean(columns.indexOf("REQUESTABLE") + 1, account.isRequestable());
          if (Configuration.areCustomCurrenciesEnabled())
            for (String currency : currencies)
              preparedStatement.setDouble(columns.indexOf(currency) + 1, account.getCustomBalance(currency));
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
  public synchronized Map<UUID, Account> getAllAccounts() {
    Map<UUID, Account> accounts = new HashMap<>();
    
    try (Connection connection = getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PLAYERDATA")) {
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        UUID uuid = UUID.fromString(resultSet.getString("UUID"));
        Account account = new Account(uuid)
          .setName(resultSet.getString("USERNAME"))
          .setBalance(resultSet.getDouble("BALANCE"));
        if (Configuration.isCommandEnabled("pay"))
          account.setPayable(resultSet.getBoolean("PAYABLE"));
        if (Configuration.isCommandEnabled("request"))
          account.setRequestable(resultSet.getBoolean("REQUESTABLE"));
        if (Configuration.areCustomCurrenciesEnabled()) {
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
  public synchronized void updateAccounts(Collection<Account> accounts) {
    if (accounts.isEmpty())
      return;
    
    try (Connection connection = getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(getUpdateStatement())) {
        connection.setAutoCommit(false);
        for (Account account : accounts) {
          preparedStatement.setString(1, account.getName());
          preparedStatement.setDouble(2, account.getBalance());
          if (Configuration.isCommandEnabled("pay"))
            preparedStatement.setBoolean(columns.indexOf("PAYABLE"), account.isPayable());
          if (Configuration.isCommandEnabled("request"))
            preparedStatement.setBoolean(columns.indexOf("REQUESTABLE"), account.isRequestable());
          if (Configuration.areCustomCurrenciesEnabled())
            for (String currency : currencies)
              preparedStatement.setDouble(columns.indexOf(currency), account.getCustomBalance(currency));
          preparedStatement.setString(columns.size(), account.getUUID().toString());
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
  public List<String> getCurrencies() {
    if (!Configuration.areCustomCurrenciesEnabled()) {
      new Debug.QualityError("This feature is disabled within QualityEconomy's configuration").log();
      return new ArrayList<>();
    }
    return new ArrayList<>(currencies);
  }
  
  @Override
  public void addCurrency(String currency) {
    if (!Configuration.areCustomCurrenciesEnabled()) {
      new Debug.QualityError("This feature is disabled within QualityEconomy's configuration").log();
      return;
    }
    currency = currency.toUpperCase();
    if (List.of("UUID", "NAME", "BALANCE", "PAYABLE", "REQUESTABLE").contains(currency)) {
      new Debug.QualityError("Failed to create currency \"" + currency + "\"", "Name cannot be \"UUID\", \"NAME\", \"BALANCE\", \"PAYABLE\", \"REQUESTABLE\"").log();
      return;
    }
    if (currencies.contains(currency)) {
      new Debug.QualityError("Failed to create currency \"" + currency + "\"", "Currency already exists").log();
    }
    try (Connection connection = getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO CURRENCIES(CURRENCY) VALUES(?)")) {
        preparedStatement.setString(1, currency);
        preparedStatement.executeUpdate();
        addColumn(connection, currency, "FLOAT(53) NOT NULL DEFAULT 0.0");
        currencies.add(currency);
        CommandManager.getCommand("custombalance").register();
        CommandManager.getCommand("customeconomy").register();
      } catch (SQLException exception) {
        new Debug.QualityError("Failed to add currency to database (" + currency + ")", exception).log();
        connection.rollback();
      }
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to retrieve connection to database or rollback", exception).log();
    }
  }
  
  @Override
  public void removeCurrency(String currency) {
    if (!Configuration.areCustomCurrenciesEnabled()) {
      new Debug.QualityError("This feature is disabled within QualityEconomy's configuration").log();
      return;
    }
    currency = currency.toUpperCase();
    if (!currencies.contains(currency)) {
      new Debug.QualityError("Failed to delete currency \"" + currency + "\"", "Currency doesn't exist").log();
    }
    try (Connection connection = getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM CURRENCIES WHERE CURRENCY = ?")) {
        preparedStatement.setString(1, currency);
        preparedStatement.executeUpdate();
        dropColumn(connection, currency);
        currencies.remove(currency);
        if (currencies.isEmpty()) {
          CommandManager.getCommand("custombalance").unregister();
          CommandManager.getCommand("customeconomy").unregister();
        }
      } catch (SQLException exception) {
        new Debug.QualityError("Failed to remove currency from database (" + currency + ")", exception).log();
        connection.rollback();
      }
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to retrieve connection to database or rollback", exception).log();
    }
  }
  
  private void toggleCurrencyTable(Connection connection) throws SQLException {
    boolean tableExists = tableExists(getMetaData(connection), "CURRENCIES");
    if (Configuration.areCustomCurrenciesEnabled() && !tableExists)
      createTable(connection, "CURRENCIES", "CURRENCY VARCHAR(255) PRIMARY KEY");
    else if (!Configuration.areCustomCurrenciesEnabled() && tableExists)
      dropTable(connection, "CURRENCIES");
    
    if (tableExists) {
      try (ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM CURRENCIES")) {
        while (resultSet.next())
          currencies.add(resultSet.getString(1));
      }
      if (!currencies.isEmpty()) {
        CommandManager.getCommand("custombalance").register();
        CommandManager.getCommand("customeconomy").register();
      }
    }
  }
  
  private void toggleCurrencyColumns(Connection connection) throws SQLException {
    DatabaseMetaData metaData = getMetaData(connection);
    if (Configuration.areCustomCurrenciesEnabled())
      for (String currency : currencies) {
        if (!columnExists(metaData, currency))
          addColumn(connection, currency, "FLOAT(53) NOT NULL DEFAULT 0.0");
      }
    else {
      for (String currency : currencies) {
        if (columnExists(metaData, currency))
          dropColumn(connection, currency);
      }
    }
  }
  
  private void togglePayableColumn(Connection connection) throws SQLException {
    boolean columnExists = columnExists(getMetaData(connection), "PAYABLE");
    if (Configuration.isCommandEnabled("pay") && !columnExists)
      addColumn(connection, "PAYABLE", "BOOLEAN NOT NULL DEFAULT TRUE");
    else if (!Configuration.isCommandEnabled("pay") && columnExists)
      dropColumn(connection, "PAYABLE");
  }
  
  private void toggleRequestableColumn(Connection connection) throws SQLException {
    boolean columnExists = columnExists(getMetaData(connection), "REQUESTABLE");
    if (Configuration.isCommandEnabled("request") && !columnExists)
      addColumn(connection, "REQUESTABLE", "BOOLEAN NOT NULL DEFAULT FALSE");
    else if (!Configuration.isCommandEnabled("request") && columnExists)
      dropColumn(connection, "REQUESTABLE");
  }
  
}
