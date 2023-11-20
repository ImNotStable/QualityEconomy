package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;
import com.imnotstable.qualityeconomy.util.QualityError;
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

public class H2StorageType implements StorageType {
  
  static {
    try {
      DriverManager.registerDriver(new org.h2.Driver());
    } catch (SQLException exception) {
      new QualityError("Failed to load H2 Driver", exception).log();
    }
  }
  
  private Connection connection;
  
  private void connect() {
    try {
      if (connection != null && !connection.isClosed()) {
        new QualityError("Attempted to connect to database when already connected").log();
        return;
      }
      connection = DriverManager.getConnection("jdbc:h2:./plugins/QualityEconomy/playerdata", "sa", "");
    } catch (SQLException exception) {
      new QualityError("Failed to connect to database", exception).log();
    }
  }
  
  private void closeConnection() {
    try {
      if (connection == null) {
        new QualityError("Attempted to close connection with database when connection doesn't exist").log();
        return;
      }
      if (connection.isClosed()) {
        new QualityError("Attempted to close connection with database when connection is already closed").log();
        return;
      }
      connection.close();
    } catch (SQLException exception) {
      new QualityError("Error while closing database connection", exception).log();
    }
  }
  
  private void createTable() {
    String sql = "CREATE TABLE IF NOT EXISTS PLAYERDATA (uuid VARCHAR(36) PRIMARY KEY, name VARCHAR(16), balance REAL NOT NULL, payable BOOLEAN NOT NULL);";
    try (Statement stmt = connection.createStatement()) {
      stmt.execute(sql);
    } catch (SQLException exception) {
      new QualityError("Failed to create table", exception).log();
    }
  }
  
  @Override
  public boolean initStorageProcesses() {
    connect();
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
      new QualityError("Failed to get database metadata", exception).log();
      return;
    }
    
    for (String currency : CustomCurrencies.getCustomCurrencies()) {
      currency = currency.toUpperCase();
      try (ResultSet columns = metaData.getColumns(null, null, "PLAYERDATA", currency)) {
        if (!columns.next()) {
          addCurrency(currency);
        }
      } catch (SQLException exception) {
        new QualityError("Failed to check if currency exists (" + currency + ")", exception).log();
      }
    }
  }
  
  @Override
  public void endStorageProcesses() {
    closeConnection();
  }
  
  public void wipeDatabase() {
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate("DELETE FROM PLAYERDATA");
    } catch (SQLException exception) {
      new QualityError("Failed to wipe database", exception).log();
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
    
    String sql = "INSERT INTO PLAYERDATA(" + columns + ") VALUES(" + placeholders + ")";
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
    
    StringBuilder columns = new StringBuilder("uuid, name, balance, payable");
    StringBuilder placeholders = new StringBuilder("?,?,?,?");
    
    List<String> customCurrencies = CustomCurrencies.getCustomCurrencies();
    for (String currency : customCurrencies) {
      columns.append(",").append(currency);
      placeholders.append(",?");
    }
    
    String sql = "INSERT INTO PLAYERDATA(" + columns + ") VALUES(" + placeholders + ")";
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
      new QualityError("Failed to create accounts", exception1).log();
      try {
        connection.rollback();
      } catch (SQLException exception2) {
        new QualityError("Failed to rollback transaction", exception2).log();
      }
    } finally {
      try {
        connection.setAutoCommit(true);
      } catch (SQLException exception3) {
        new QualityError("Failed to restore auto-commit mode", exception3).log();
      }
    }
  }
  
  @Override
  public boolean accountExists(UUID uuid) {
    try (PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) FROM PLAYERDATA WHERE uuid = ?")) {
      pstmt.setString(1, uuid.toString());
      ResultSet rs = pstmt.executeQuery();
      int count = rs.getInt(1);
      
      return count != 0;
    } catch (SQLException exception) {
      new QualityError("Failed to check if account exists (" + uuid.toString() + ")", exception).log();
      return false;
    }
  }
  
  @Override
  public Account getAccount(UUID uuid) {
    try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM PLAYERDATA WHERE uuid = ?")) {
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
      new QualityError("Failed to get account (" + uuid.toString() + ")", exception).log();
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
    
    try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM PLAYERDATA WHERE uuid IN (" + uuidList + ")")) {
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
      new QualityError("Failed to get accounts", exception).log();
    }
    
    return accounts;
  }
  
  @Override
  public Map<UUID, Account> getAllAccounts() {
    Map<UUID, Account> accounts = new HashMap<>();
    
    try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM PLAYERDATA")) {
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
      new QualityError("Failed to get all accounts", exception);
    }
    
    return accounts;
  }
  
  @Override
  public void updateAccount(Account account) {
    StringBuilder sql = new StringBuilder("UPDATE PLAYERDATA SET name = ?, balance = ?, payable = ?");
    
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
        new QualityError("Failed to update account (" + account.getUUID().toString() + ")").log();
      }
    } catch (SQLException exception) {
      new QualityError("Failed to update account (" + account.getUUID().toString() + ")", exception).log();
    }
  }
  
  public void updateAccounts(Collection<Account> accounts) {
    if (accounts.isEmpty())
      return;
    
    StringBuilder sql = new StringBuilder("UPDATE PLAYERDATA SET name = ?, balance = ?, payable = ?");
    
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
      new QualityError("Failed to update accounts", exception1).log();
      try {
        connection.rollback();
      } catch (SQLException exception2) {
        new QualityError("Failed to rollback transaction", exception2).log();
      }
    } finally {
      try {
        connection.setAutoCommit(true);
      } catch (SQLException exception3) {
        new QualityError("Failed to restore auto-commit mode", exception3).log();
      }
    }
  }
  
  @Override
  public Collection<UUID> getAllUUIDs() {
    Collection<String> rawUUIDs = new ArrayList<>();
    try (PreparedStatement pstmt = connection.prepareStatement("SELECT uuid FROM PLAYERDATA")) {
      ResultSet rs = pstmt.executeQuery();
      while (rs.next())
        rawUUIDs.add(rs.getString("uuid"));
    } catch (SQLException exception) {
      new QualityError("Failed to get all UUIDs", exception);
    }
    
    return rawUUIDs.stream().map(UUID::fromString).toList();
  }
  
  @Override
  public void addCurrency(String currencyName) {
    try (Statement stmt = connection.createStatement()) {
      currencyName = currencyName.toUpperCase();
      stmt.executeUpdate("ALTER TABLE PLAYERDATA ADD COLUMN " + currencyName + " REAL NOT NULL DEFAULT 0.0");
    } catch (SQLException exception) {
      new QualityError("Failed to add currency to database (" + currencyName + ")", exception).log();
    }
  }
  
  @Override
  public void removeCurrency(String currencyName) {
    try (Statement stmt = connection.createStatement()) {
      currencyName = currencyName.toUpperCase();
      stmt.executeUpdate("ALTER TABLE PLAYERDATA DROP COLUMN " + currencyName);
    } catch (SQLException exception) {
      new QualityError("Failed to remove currency from database (" + currencyName + ")", exception).log();
    }
  }
  
}
