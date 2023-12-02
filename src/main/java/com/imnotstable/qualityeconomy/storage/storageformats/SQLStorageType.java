package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.util.ColumnManager;
import com.imnotstable.qualityeconomy.util.Logger;
import com.imnotstable.qualityeconomy.util.QualityError;
import com.imnotstable.qualityeconomy.util.SQLUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SQLStorageType implements StorageType {
  
  static {
    try {
      DriverManager.registerDriver(new org.h2.Driver());
    } catch (SQLException exception) {
      new QualityError("Failed to load H2 Driver", exception).log();
    }
  }
  
  private final int databaseType;
  private HikariDataSource dataSource;
  private ColumnManager columnManager;
  
  public SQLStorageType(int databaseType) {
    this.databaseType = databaseType;
  }
  
  private HikariDataSource getDataSource() throws SQLException {
    HikariConfig hikariConfig = new HikariConfig();
    switch (databaseType) {
      case 1 -> {
        hikariConfig.setJdbcUrl("jdbc:h2:./plugins/QualityEconomy/playerdata");
        hikariConfig.setUsername("sa");
        hikariConfig.setPassword("");
      }
      case 2 -> hikariConfig.setJdbcUrl("jdbc:sqlite:plugins/QualityEconomy/playerdata.db");
      case 3 -> {
        String address = Configuration.getMySQL().get(0);
        String name = Configuration.getMySQL().get(1);
        String user = Configuration.getMySQL().get(2);
        String password = Configuration.getMySQL().get(3);
        hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s/%s", address, name));
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      }
      default -> throw new SQLException("Unsupported Database: " + databaseType);
    }
    hikariConfig.setMaximumPoolSize(10);
    hikariConfig.setMinimumIdle(5);
    hikariConfig.setConnectionTimeout(60000);
    hikariConfig.setMaxLifetime(1800000);
    hikariConfig.setPoolName("QualityEconomyPool");
    return new HikariDataSource(hikariConfig);
  }
  
  private Connection getConnection() throws SQLException {
    try {
      return dataSource.getConnection();
    } catch (SQLException exception) {
      new QualityError("Failed to get connection", exception).log();
      throw exception;
    }
  }
  
  private void connect() {
    if (dataSource != null && !dataSource.isClosed()) {
      new QualityError("Attempted to connect to database when already connected").log();
      return;
    }
    try {
      dataSource = getDataSource();
    } catch (SQLException exception) {
      new QualityError("Failed to connect to database", exception).log();
    }
  }
  
  private void createTable(Connection connection) {
    String sql = "CREATE TABLE IF NOT EXISTS PLAYERDATA (UUID CHAR(36) PRIMARY KEY, NAME VARCHAR(16), BALANCE REAL NOT NULL);";
    try (Statement statement = connection.createStatement()) {
      statement.execute(sql);
    } catch (SQLException exception) {
      new QualityError("Failed to create table", exception).log();
    }
  }
  
  private void closeConnection() {
    if (dataSource == null) {
      new QualityError("Attempted to close datasource when datasource doesn't exist").log();
      return;
    }
    if (dataSource.isClosed()) {
      new QualityError("Attempted to close datasource when datasource is already closed").log();
      return;
    }
    dataSource.close();
  }
  
  @Override
  public boolean initStorageProcesses() {
    connect();
    if (dataSource == null || dataSource.isClosed()) {
      new QualityError("Failed to open datasource").log();
      return false;
    }
    Connection connection;
    try {
      connection = getConnection();
    } catch (SQLException exception) {
      return false;
    }
    createTable(connection);
    columnManager = new ColumnManager(connection);
    togglePayableColumn(connection);
    toggleRequestableColumn(connection);
    try {
      DatabaseMetaData metaData = SQLUtils.getDatabaseMetaData(connection);
      for (String currency : CustomCurrencies.getCustomCurrencies())
        if (!SQLUtils.columnExists(metaData, currency))
          addCurrency(currency);
      connection.close();
    } catch (SQLException exception) {
      new QualityError("Error while loading custom currencies", exception).log();
    }
    Logger.log(Component.text("Successfully initiated storage processes", NamedTextColor.GREEN));
    return true;
  }
  
  @Override
  public void endStorageProcesses() {
    closeConnection();
  }
  
  public void wipeDatabase() {
    try (Connection connection = getConnection();
         Statement statement = connection.createStatement()) {
      statement.executeUpdate("DELETE FROM PLAYERDATA");
    } catch (SQLException exception) {
      new QualityError("Failed to wipe database", exception).log();
    }
  }
  
  @Override
  public boolean createAccount(Account account) {
    try (Connection connection = getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(columnManager.getCreateStatement())) {
      UUID uuid = account.getUUID();
      preparedStatement.setString(1, uuid.toString());
      preparedStatement.setString(2, account.getName());
      preparedStatement.setDouble(3, account.getBalance());
      if (Configuration.isPayCommandEnabled())
        preparedStatement.setBoolean(columnManager.getColumns().indexOf("PAYABLE") + 1, account.isPayable());
      if (Configuration.isRequestCommandEnabled())
        preparedStatement.setBoolean(columnManager.getColumns().indexOf("REQUESTABLE") + 1, account.isRequestable());
      for (String currency : CustomCurrencies.getCustomCurrencies())
        preparedStatement.setDouble(columnManager.getColumns().indexOf(currency) + 1, account.getCustomBalance(currency));
      int affectedRows = preparedStatement.executeUpdate();
      
      if (affectedRows == 0) {
        new QualityError("Failed to create account (" + uuid + ")").log();
      }
      return true;
    } catch (SQLException exception) {
      new QualityError("Failed to create account (" + account.getUUID().toString() + ")", exception).log();
      return false;
    }
  }
  
  public void createAccounts(Collection<Account> accounts) {
    if (accounts.isEmpty())
      return;
    
    Connection connection = null;
    try {
      connection = getConnection();
      try (PreparedStatement preparedStatement = connection.prepareStatement(columnManager.getCreateStatement())) {
        connection.setAutoCommit(false);
        
        for (Account account : accounts) {
          UUID uuid = account.getUUID();
          preparedStatement.setString(1, uuid.toString());
          preparedStatement.setString(2, account.getName());
          preparedStatement.setDouble(3, account.getBalance());
          if (Configuration.isPayCommandEnabled())
            preparedStatement.setBoolean(columnManager.getColumns().indexOf("PAYABLE") + 1, account.isPayable());
          if (Configuration.isRequestCommandEnabled())
            preparedStatement.setBoolean(columnManager.getColumns().indexOf("REQUESTABLE") + 1, account.isRequestable());
          for (String currency : CustomCurrencies.getCustomCurrencies())
            preparedStatement.setDouble(columnManager.getColumns().indexOf(currency) + 1, account.getCustomBalance(currency));
          preparedStatement.addBatch();
        }
        
        preparedStatement.executeBatch();
        connection.commit();
      }
    } catch (SQLException exception) {
      new QualityError("Failed to create accounts", exception).log();
      try {
        if (connection != null) {
          connection.rollback();
        }
      } catch (SQLException exception2) {
        new QualityError("Failed to rollback transaction", exception2).log();
      }
    } finally {
      if (connection != null) {
        try {
          connection.setAutoCommit(true);
          connection.close();
        } catch (SQLException exception) {
          new QualityError("Failed to restore auto-commit mode", exception).log();
        }
      }
    }
  }
  
  
  @Override
  public boolean accountExists(UUID uuid) {
    try (Connection connection = getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM PLAYERDATA WHERE UUID = ?")) {
      preparedStatement.setString(1, uuid.toString());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        return resultSet.next() && resultSet.getInt(1) > 0;
      }
    } catch (SQLException exception) {
      new QualityError("Failed to check if account exists (" + uuid.toString() + ")", exception).log();
      return false;
    }
  }
  
  
  @Override
  public Account getAccount(UUID uuid) {
    try (
      Connection connection = getConnection();
      PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PLAYERDATA WHERE UUID = ?")) {
      preparedStatement.setString(1, uuid.toString());
      ResultSet resultSet = preparedStatement.executeQuery();
      if (!resultSet.next())
        return null;
      Map<String, Double> customCurrencies = new HashMap<>();
      for (String currency : CustomCurrencies.getCustomCurrencies()) {
        customCurrencies.put(currency, resultSet.getDouble(currency));
      }
      Account account = new Account(uuid)
        .setName(resultSet.getString("NAME"))
        .setBalance(resultSet.getDouble("BALANCE"))
        .setCustomBalances(customCurrencies);
      if (Configuration.isPayCommandEnabled())
        account.setPayable(resultSet.getBoolean("PAYABLE"));
      if (Configuration.isRequestCommandEnabled())
        account.setRequestable(resultSet.getBoolean("REQUESTABLE"));
      return account;
    } catch (SQLException exception) {
      new QualityError("Failed to get account (" + uuid.toString() + ")", exception).log();
      return null;
    }
  }
  
  @Override
  public Map<UUID, Account> getAccounts(Collection<UUID> uuids) {
    Map<UUID, Account> accounts = new HashMap<>();
    
    if (uuids.isEmpty())
      return accounts;
    
    String uuidList = uuids.stream().map(UUID::toString).collect(Collectors.joining("','", "'", "'"));
    
    try (Connection connection = getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PLAYERDATA WHERE UUID IN (" + uuidList + ")")) {
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
        Map<String, Double> customCurrencies = new HashMap<>();
        for (String currency : CustomCurrencies.getCustomCurrencies()) {
          customCurrencies.put(currency, resultSet.getDouble(currency));
        }
        Account account = new Account(uuid)
          .setName(resultSet.getString("NAME"))
          .setBalance(resultSet.getDouble("BALANCE"))
          .setCustomBalances(customCurrencies);
        if (Configuration.isPayCommandEnabled())
          account.setPayable(resultSet.getBoolean("PAYABLE"));
        if (Configuration.isRequestCommandEnabled())
          account.setRequestable(resultSet.getBoolean("REQUESTABLE"));
        accounts.put(uuid, account);
      }
    } catch (SQLException exception) {
      new QualityError("Failed to get accounts", exception).log();
    }
    
    return accounts;
  }
  
  @Override
  public Map<UUID, Account> getAllAccounts() {
    Map<UUID, Account> accounts = new HashMap<>();
    
    try (Connection connection = getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PLAYERDATA")) {
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        UUID uuid = UUID.fromString(resultSet.getString("UUID"));
        Map<String, Double> customCurrencies = new HashMap<>();
        for (String currency : CustomCurrencies.getCustomCurrencies()) {
          customCurrencies.put(currency, resultSet.getDouble(currency));
        }
        Account account = new Account(uuid)
          .setName(resultSet.getString("NAME"))
          .setBalance(resultSet.getDouble("BALANCE"))
          .setCustomBalances(customCurrencies);
        if (Configuration.isPayCommandEnabled())
          account.setPayable(resultSet.getBoolean("PAYABLE"));
        if (Configuration.isRequestCommandEnabled())
          account.setRequestable(resultSet.getBoolean("REQUESTABLE"));
        accounts.put(uuid, account);
      }
    } catch (SQLException exception) {
      new QualityError("Failed to get all accounts", exception);
    }
    
    return accounts;
  }
  
  @Override
  public void updateAccount(Account account) {
    try (Connection connection = getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(columnManager.getUpdateStatement())) {
      preparedStatement.setString(1, account.getName());
      preparedStatement.setDouble(2, account.getBalance());
      if (Configuration.isPayCommandEnabled())
        preparedStatement.setBoolean(columnManager.getColumns().indexOf("PAYABLE"), account.isPayable());
      if (Configuration.isRequestCommandEnabled())
        preparedStatement.setBoolean(columnManager.getColumns().indexOf("REQUESTABLE"), account.isRequestable());
      for (String currency : CustomCurrencies.getCustomCurrencies()) {
        preparedStatement.setDouble(columnManager.getColumns().indexOf(currency), account.getCustomBalance(currency));
      }
      preparedStatement.setString(columnManager.getColumns().size(), account.getUUID().toString());
      int affectedRows = preparedStatement.executeUpdate();
      if (affectedRows == 0) {
        new QualityError("Failed to update account (" + account.getUUID().toString() + ")").log();
      }
    } catch (SQLException exception) {
      new QualityError("Failed to update account (" + account.getUUID().toString() + ")", exception).log();
    }
  }
  
  @Override
  public void updateAccounts(Collection<Account> accounts) {
    if (accounts.isEmpty())
      return;
    
    Connection connection = null;
    try {
      connection = getConnection();
      try (PreparedStatement preparedStatement = connection.prepareStatement(columnManager.getUpdateStatement())) {
        connection.setAutoCommit(false);
        
        for (Account account : accounts) {
          preparedStatement.setString(1, account.getName());
          preparedStatement.setDouble(2, account.getBalance());
          if (Configuration.isPayCommandEnabled())
            preparedStatement.setBoolean(columnManager.getColumns().indexOf("PAYABLE"), account.isPayable());
          if (Configuration.isRequestCommandEnabled())
            preparedStatement.setBoolean(columnManager.getColumns().indexOf("REQUESTABLE"), account.isRequestable());
          for (String currency : CustomCurrencies.getCustomCurrencies()) {
            preparedStatement.setDouble(columnManager.getColumns().indexOf(currency), account.getCustomBalance(currency));
          }
          preparedStatement.setString(columnManager.getColumns().size(), account.getUUID().toString());
          preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        connection.commit();
      }
    } catch (SQLException exception) {
      new QualityError("Failed to update accounts", exception).log();
      try {
        if (connection != null) {
          connection.rollback();
        }
      } catch (SQLException exception2) {
        new QualityError("Failed to rollback transaction", exception2).log();
      }
    } finally {
      if (connection != null) {
        try {
          connection.setAutoCommit(true);
          connection.close();
        } catch (SQLException exception) {
          new QualityError("Failed to restore auto-commit mode", exception).log();
        }
      }
    }
  }
  
  @Override
  public Collection<UUID> getAllUUIDs() {
    Collection<String> rawUUIDs = new ArrayList<>();
    try (Connection connection = getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT UUID FROM PLAYERDATA")) {
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next())
        rawUUIDs.add(resultSet.getString("uuid"));
    } catch (SQLException exception) {
      new QualityError("Failed to get all UUIDs", exception);
    }
    
    return rawUUIDs.stream().map(UUID::fromString).toList();
  }
  
  @Override
  public void addCurrency(String currencyName) {
    try (Connection connection = getConnection();
         Statement statement = connection.createStatement()) {
      statement.executeUpdate("ALTER TABLE PLAYERDATA ADD COLUMN " + currencyName + " REAL NOT NULL DEFAULT 0.0");
      columnManager.updateColumns(currencyName, true);
    } catch (SQLException exception) {
      new QualityError("Failed to add currency to database (" + currencyName + ")", exception).log();
    }
  }
  
  @Override
  public void removeCurrency(String currencyName) {
    try (Connection connection = getConnection();
         Statement statement = connection.createStatement()) {
      statement.executeUpdate("ALTER TABLE PLAYERDATA DROP COLUMN " + currencyName);
      columnManager.updateColumns(currencyName, false);
    } catch (SQLException exception) {
      new QualityError("Failed to remove currency from database (" + currencyName + ")", exception).log();
    }
  }
  
  public void togglePayableColumn(Connection connection) {
    try {
      boolean columnExists = SQLUtils.columnExists(SQLUtils.getDatabaseMetaData(connection), "PAYABLE");
      if (Configuration.isPayCommandEnabled() && !columnExists) {
        SQLUtils.addColumn(connection, "PAYABLE", "BOOLEAN NOT NULL DEFAULT TRUE");
        columnManager.updateColumns("PAYABLE", true);
      } else if (!Configuration.isPayCommandEnabled() && columnExists) {
        SQLUtils.dropColumn(connection, "PAYABLE");
        columnManager.updateColumns("PAYABLE", false);
      }
    } catch (SQLException exception) {
      new QualityError("", exception).log();
    }
  }
  
  public void toggleRequestableColumn(Connection connection) {
    try {
      boolean columnExists = SQLUtils.columnExists(SQLUtils.getDatabaseMetaData(connection), "REQUESTABLE");
      if (Configuration.isRequestCommandEnabled() && !columnExists) {
        SQLUtils.addColumn(connection, "REQUESTABLE", "BOOLEAN NOT NULL DEFAULT FALSE");
        columnManager.updateColumns("REQUESTABLE", true);
      } else if (!Configuration.isRequestCommandEnabled() && columnExists) {
        SQLUtils.dropColumn(connection, "REQUESTABLE");
        columnManager.updateColumns("REQUESTABLE", false);
      }
    } catch (SQLException exception) {
      new QualityError("", exception).log();
    }
  }
  
}
