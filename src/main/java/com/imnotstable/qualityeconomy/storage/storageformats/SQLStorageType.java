package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.commands.CommandManager;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.Logger;
import com.imnotstable.qualityeconomy.util.storage.EasySQL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    if (dataSource == null || dataSource.isClosed())
      return false;
    try (Connection connection = getConnection()) {
      toggleCurrencyTable(connection);
      toggleColumns(connection);
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
  
  @Override
  public synchronized void wipeDatabase() {
    try (Connection connection = getConnection()) {
      dropPlayerDataTable(connection);
      if (Configuration.areCustomCurrenciesEnabled())
        dropCurrencyTable(connection);
      close();
      open();
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
        new Debug.QualityError("Failed to create account (" + account.getUUID().toString() + ")").log();
      }
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to create account (" + account.getUUID().toString() + ")", exception).log();
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
  public synchronized Map<UUID, Account> getAllAccounts() {
    Map<UUID, Account> accounts = new HashMap<>();
    
    try (Connection connection = getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PLAYERDATA")) {
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        UUID uuid = UUID.fromString(resultSet.getString("UUID"));
        Account account = new Account(uuid)
          .setUsername(resultSet.getString("USERNAME"))
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
  public synchronized void updateAccounts(@NotNull Collection<Account> accounts) {
    if (accounts.isEmpty())
      return;
    
    try (Connection connection = getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(getUpdateStatement())) {
        connection.setAutoCommit(false);
        for (Account account : accounts) {
          preparedStatement.setString(1, account.getUsername());
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
  public void addCurrency(@NotNull String currency) {
    currency = addCurrencyAttempt(currency);
    try (Connection connection = getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO CURRENCIES(CURRENCY) VALUES(?)")) {
        preparedStatement.setString(1, currency);
        preparedStatement.executeUpdate();
        addColumn(connection, currency, "FLOAT(53)", "0.0");
        addCurrencySuccess(currency);
      } catch (SQLException exception) {
        new Debug.QualityError("Failed to add currency to database (" + currency + ")", exception).log();
        connection.rollback();
      }
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to retrieve connection to database or rollback", exception).log();
    }
  }
  
  @Override
  public void removeCurrency(@NotNull String currency) {
    currency = removeCurrencyAttempt(currency);
    try (Connection connection = getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM CURRENCIES WHERE CURRENCY = ?")) {
        preparedStatement.setString(1, currency);
        preparedStatement.executeUpdate();
        dropColumn(connection, currency);
        removeCurrencySuccess(currency);
      } catch (SQLException exception) {
        new Debug.QualityError("Failed to remove currency from database (" + currency + ")", exception).log();
        connection.rollback();
      }
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to retrieve connection to database or rollback", exception).log();
    }
  }
  
  private void toggleCurrencyTable(Connection connection) throws SQLException {
    boolean tableExists = currencyTableExists(getMetaData(connection));
    if (Configuration.areCustomCurrenciesEnabled() && !tableExists)
      createCurrencyTable(connection);
    else if (!Configuration.areCustomCurrenciesEnabled() && tableExists)
      dropCurrencyTable(connection);
    
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
  
  private void toggleColumns(Connection connection) throws SQLException {
    DatabaseMetaData metaData = getMetaData(connection);
    if (Configuration.areCustomCurrenciesEnabled()) {
      for (String currency : currencies)
        if (!columnExists(metaData, currency))
          addColumn(connection, currency, "FLOAT(53)", "0.0");
    }
    else {
      for (String currency : currencies)
        if (columnExists(metaData, currency))
          dropColumn(connection, currency);
    }
    boolean payableExists = columnExists(metaData, "PAYABLE");
    if (Configuration.isCommandEnabled("pay") && !payableExists)
      addColumn(connection, "PAYABLE", "BOOLEAN", "TRUE");
    else if (!Configuration.isCommandEnabled("pay") && payableExists)
      dropColumn(connection, "PAYABLE");
    boolean requestableExists = columnExists(metaData, "REQUESTABLE");
    if (Configuration.isCommandEnabled("request") && !requestableExists)
      addColumn(connection, "REQUESTABLE", "BOOLEAN", "FALSE");
    else if (!Configuration.isCommandEnabled("request") && requestableExists)
      dropColumn(connection, "REQUESTABLE");
  }
  
}
