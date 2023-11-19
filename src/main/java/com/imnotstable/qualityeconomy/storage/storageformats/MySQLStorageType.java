package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.Account;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;
import com.imnotstable.qualityeconomy.util.Error;
import com.imnotstable.qualityeconomy.util.Logger;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MySQLStorageType implements StorageType {
  
  private Connection connection;
  
  private String getPath() {
    String address = Configuration.getMySQLInfo().get(0);
    String name = Configuration.getMySQLInfo().get(1);
    String user = Configuration.getMySQLInfo().get(2);
    String password = Configuration.getMySQLInfo().get(3);
    return String.format("jdbc:mysql://%s/%s?user=%s&password=%s", address, name, user, password);
  }
  
  private void connect() {
    try {
      if (connection != null && !connection.isClosed()) {
        new Error("Attempted to connect to database when already connected").log();
        return;
      }
      connection = DriverManager.getConnection(getPath());
    } catch (SQLException exception) {
      new Error("Failed to connect to database", exception).log();
    }
  }
  
  private void closeConnection() {
    try {
      if (connection == null) {
        new Error("Attempted to close connection with database when connection doesn't exist").log();
        return;
      }
      if (connection.isClosed()) {
        new Error("Attempted to close connection with database when connection is already closed").log();
        return;
      }
      connection.close();
    } catch (SQLException exception) {
      new Error("Error while closing database connection", exception).log();
    }
  }
  
  private void createTable() {
    String sql = "CREATE TABLE IF NOT EXISTS playerdata (uuid CHAR(36) PRIMARY KEY, name CHAR(16), balance REAL NOT NULL, payable BOOLEAN NOT NULL);";
    try (Statement stmt = connection.createStatement()) {
      stmt.execute(sql);
      ResultSet tables = connection.getMetaData().getTables(null, null, "playerdata", null);
      if (!tables.next())
        new Error("Failed to create table", tables.getWarnings()).log();
    } catch (SQLException exception) {
      new Error("Failed to create table", exception).log();
    }
  }
  
  @Override
  public boolean initStorageProcesses() {
    connect();
    try {
      if (connection == null || connection.isClosed())
        new Error("Failed to open connection").log();
    } catch (SQLException exception) {
      new Error("Failed to check if connection was opened", exception).log();
    }
    createTable();
    checkCustomCurrencyColumns();
    Logger.log(Component.text("Successfully initiated storage processes", NamedTextColor.GREEN));
    return true;
  }
  
  
  public void checkCustomCurrencyColumns() {
    if (CustomCurrencies.getCustomCurrencies().isEmpty())
      return;
    
    DatabaseMetaData metaData;
    try {
      metaData = connection.getMetaData();
    } catch (SQLException exception) {
      new Error("Failed to get database metadata", exception).log();
      return;
    }
    
    for (String currency : CustomCurrencies.getCustomCurrencies()) {
      try (ResultSet columns = metaData.getColumns(null, null, "playerdata", currency)) {
        if (!columns.next()) {
          addCurrency(currency);
        }
      } catch (SQLException exception) {
        new Error("Failed to check if currency exists (" + currency + ")", exception).log();
      }
    }
  }
  
  @Override
  public void endStorageProcesses() {
    closeConnection();
  }
  
  public void wipeDatabase() {
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate("DELETE FROM playerdata");
    } catch (SQLException exception) {
      new Error("Failed to wipe database", exception).log();
    }
  }
  
  @Override
  public boolean createAccount(Account account) {
    StringBuilder columns = new StringBuilder("uuid, name, balance, payable");
    StringBuilder placeholders = new StringBuilder("?,?,?,?");
    
    List<String> customCurrencies = CustomCurrencies.getCustomCurrencies();
    for (String currency : customCurrencies) {
      columns.append(", ").append(currency);
      placeholders.append(", ?");
    }
    
    String sql = "INSERT INTO playerdata(" + columns + ") VALUES(" + placeholders + ")";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      UUID uuid = account.getUUID();
      pstmt.setString(1, uuid.toString());
      pstmt.setString(2, account.getName());
      pstmt.setDouble(3, account.getBalance());
      pstmt.setBoolean(4, account.getPayable());
      int index = 5;
      for (String currency : customCurrencies) {
        pstmt.setDouble(index, account.getCustomBalance(currency));
        index++;
      }
      int affectedRows = pstmt.executeUpdate();
      
      if (affectedRows == 0) {
        new Error("Failed to create account (" + uuid + ")").log();
      }
      return true;
    } catch (SQLException exception) {
      new Error("Failed to create account (" + account.getUUID().toString() + ")", exception).log();
      return false;
    }
  }
  
  public void createAccounts(Collection<Account> accounts) {
    if (accounts.isEmpty())
      return;
    
    StringBuilder columns = new StringBuilder("uuid, name, balance, payable");
    StringBuilder placeholders = new StringBuilder("?,?,?,?");
    
    List<String> customCurrencies = CustomCurrencies.getCustomCurrencies();
    for (String currency : customCurrencies) {
      columns.append(",").append(currency);
      placeholders.append(",?");
    }
    
    String sql = "INSERT INTO playerdata(" + columns + ") VALUES(" + placeholders + ")";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      connection.setAutoCommit(false);
      
      for (Account account : accounts) {
        UUID uuid = account.getUUID();
        pstmt.setString(1, uuid.toString());
        pstmt.setString(2, account.getName());
        pstmt.setDouble(3, account.getBalance());
        pstmt.setBoolean(4, account.getPayable());
        int index = 5;
        for (String currency : customCurrencies) {
          pstmt.setDouble(index, account.getCustomBalance(currency));
          index++;
        }
        pstmt.addBatch();
      }
      
      pstmt.executeBatch();
      connection.commit();
      
    } catch (SQLException exception1) {
      new Error("Failed to create accounts", exception1).log();
      try {
        connection.rollback();
      } catch (SQLException exception2) {
        new Error("Failed to rollback transaction", exception2).log();
      }
    } finally {
      try {
        connection.setAutoCommit(true);
      } catch (SQLException exception3) {
        new Error("Failed to restore auto-commit mode", exception3).log();
      }
    }
  }
  
  
  @Override
  public boolean accountExists(UUID uuid) {
    try (PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) FROM playerdata WHERE uuid = ?")) {
      pstmt.setString(1, uuid.toString());
      ResultSet rs = pstmt.executeQuery();
      int count = rs.getInt(1);
      
      return count != 0;
    } catch (SQLException exception) {
      new Error("Failed to check if account exists (" + uuid.toString() + ")", exception).log();
      return false;
    }
  }
  
  
  @Override
  public Account getAccount(UUID uuid) {
    try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM playerdata WHERE uuid = ?")) {
      pstmt.setString(1, uuid.toString());
      ResultSet rs = pstmt.executeQuery();
      Map<String, Double> customCurrencies = new HashMap<>();
      for (String currency : CustomCurrencies.getCustomCurrencies()) {
        customCurrencies.put(currency, rs.getDouble(currency));
      }
      return new Account(uuid)
        .setName(rs.getString("name"))
        .setBalance(rs.getDouble("balance"))
        .setPayable(rs.getBoolean("payable"))
        .setCustomBalances(customCurrencies);
    } catch (SQLException exception) {
      new Error("Failed to get account (" + uuid.toString() + ")", exception).log();
      return null;
    }
  }
  
  @Override
  public Map<UUID, Account> getAccounts(Collection<UUID> uuids) {
    Map<UUID, Account> accounts = new HashMap<>();
    
    if (uuids.isEmpty()) {
      return accounts;
    }
    
    String uuidList = uuids.stream().map(UUID::toString).collect(Collectors.joining("','", "'", "'"));
    
    try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM playerdata WHERE uuid IN (" + uuidList + ")")) {
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        Map<String, Double> customCurrencies = new HashMap<>();
        for (String currency : CustomCurrencies.getCustomCurrencies()) {
          customCurrencies.put(currency, rs.getDouble(currency));
        }
        Account account = new Account(uuid)
          .setName(rs.getString("name"))
          .setBalance(rs.getDouble("balance"))
          .setPayable(rs.getBoolean("payable"))
          .setCustomBalances(customCurrencies);
        accounts.put(uuid, account);
      }
    } catch (SQLException exception) {
      new Error("Failed to get accounts", exception).log();
    }
    
    return accounts;
  }
  
  @Override
  public Map<UUID, Account> getAllAccounts() {
    Map<UUID, Account> accounts = new HashMap<>();
    
    try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM playerdata")) {
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        Map<String, Double> customCurrencies = new HashMap<>();
        for (String currency : CustomCurrencies.getCustomCurrencies()) {
          customCurrencies.put(currency, rs.getDouble(currency));
        }
        Account account = new Account(uuid)
          .setName(rs.getString("name"))
          .setBalance(rs.getDouble("balance"))
          .setPayable(rs.getBoolean("payable"))
          .setCustomBalances(customCurrencies);
        accounts.put(uuid, account);
      }
    } catch (SQLException exception) {
      new Error("Failed to get all accounts", exception);
    }
    
    return accounts;
  }
  
  @Override
  public void updateAccount(Account account) {
    StringBuilder sql = new StringBuilder("UPDATE playerdata SET name = ?, balance = ?, payable = ?");
    
    List<String> customCurrencies = CustomCurrencies.getCustomCurrencies();
    for (String currency : customCurrencies) {
      sql.append(", ").append(currency).append(" = ?");
    }
    sql.append(" WHERE uuid = ?");
    
    try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
      pstmt.setString(1, account.getName());
      pstmt.setDouble(2, account.getBalance());
      pstmt.setBoolean(3, account.getPayable());
      pstmt.setString(4, account.getUUID().toString());
      int index = 4;
      for (String currency : customCurrencies) {
        pstmt.setDouble(index, account.getCustomBalance(currency));
        index++;
      }
      pstmt.setString(index, account.getUUID().toString());
      int affectedRows = pstmt.executeUpdate();
      
      if (affectedRows == 0) {
        new Error("Failed to update account (" + account.getUUID().toString() + ")").log();
      }
    } catch (SQLException exception) {
      new Error("Failed to update account (" + account.getUUID().toString() + ")", exception).log();
    }
  }
  
  public void updateAccounts(Collection<Account> accounts) {
    if (accounts.isEmpty())
      return;
    
    StringBuilder sql = new StringBuilder("UPDATE playerdata SET name = ?, balance = ?, payable = ?");
    
    List<String> customCurrencies = CustomCurrencies.getCustomCurrencies();
    for (String currency : customCurrencies) {
      sql.append(", ").append(currency).append(" = ?");
    }
    sql.append(" WHERE uuid = ?");
    
    try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
      connection.setAutoCommit(false);
      
      for (Account account : accounts) {
        pstmt.setString(1, account.getName());
        pstmt.setDouble(2, account.getBalance());
        pstmt.setBoolean(3, account.getPayable());
        int index = 4;
        for (String currency : customCurrencies) {
          pstmt.setDouble(index, account.getCustomBalance(currency));
          index++;
        }
        pstmt.setString(index, account.getUUID().toString());
        pstmt.addBatch();
      }
      
      pstmt.executeBatch();
      connection.commit();
      
    } catch (SQLException exception1) {
      new Error("Failed to update accounts", exception1).log();
      try {
        connection.rollback();
      } catch (SQLException exception2) {
        new Error("Failed to rollback transaction", exception2).log();
      }
    } finally {
      try {
        connection.setAutoCommit(true);
      } catch (SQLException exception3) {
        new Error("Failed to restore auto-commit mode", exception3).log();
      }
    }
  }
  
  
  @Override
  public Collection<UUID> getAllUUIDs() {
    Collection<String> rawUUIDs = new ArrayList<>();
    try (PreparedStatement pstmt = connection.prepareStatement("SELECT uuid FROM playerdata")) {
      ResultSet rs = pstmt.executeQuery();
      while (rs.next())
        rawUUIDs.add(rs.getString("uuid"));
    } catch (SQLException exception) {
      new Error("Failed to get all UUIDs", exception);
    }
    
    return rawUUIDs.stream().map(UUID::fromString).toList();
  }
  
  public void addCurrency(String currencyName) {
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate("ALTER TABLE playerdata ADD COLUMN " + currencyName + " REAL NOT NULL DEFAULT 0.0");
    } catch (SQLException exception) {
      new Error("Failed to add currency to database (" + currencyName + ")", exception).log();
    }
  }
  
  
  public void removeCurrency(String currencyName) {
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate("ALTER TABLE playerdata DROP COLUMN " + currencyName);
    } catch (SQLException exception) {
      new Error("Failed to remove currency from database (" + currencyName + ")", exception).log();
    }
  }
  
}
