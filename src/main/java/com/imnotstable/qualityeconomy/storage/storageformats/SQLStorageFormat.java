package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.Account;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SQLStorageFormat implements StorageFormat {
  private static final int SQLITE = 1;
  private static final int MYSQL = 2;
  private final int databaseType;
  private Connection connection;
  
  public SQLStorageFormat(int databaseType) {
    this.databaseType = databaseType;
  }
  
  private String getPath() {
    if (databaseType == MYSQL) {
      String host = Configuration.getMySQL().get(0);
      String user = Configuration.getMySQL().get(1);
      String password = Configuration.getMySQL().get(2);
      
      return String.format("jdbc:mysql://localhost:%s/QualityEconomy?user=%s&password=%s", host, user, password);
    }
    return "jdbc:sqlite:plugins/QualityEconomy/playerdata.db";
  }
  
  private void connect() {
    try {
      connection = DriverManager.getConnection(getPath());
    } catch (SQLException exception) {
      if (databaseType == SQLITE)
        new Error("Failed to connect to SQLite database", exception).log();
      else if (databaseType == MYSQL)
        new Error("Failed to connect to MySQL database", exception).log();
      else
        new Error("You fucked my plugin up", exception).log();
    }
  }
  
  private void closeConnection() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException exception) {
      if (databaseType == SQLITE)
        new Error("Error while closing SQLite database connection", exception).log();
      else if (databaseType == MYSQL)
        new Error("Error while closing MySQL database connection", exception).log();
      else
        new Error("You fucked my plugin up", exception).log();
    }
  }
  
  private void createTable() {
    String sql = "CREATE TABLE IF NOT EXISTS playerdata (uuid TEXT PRIMARY KEY, name TEXT, balance REAL NOT NULL, secondaryBalance REAL NOT NULL, payable BOOLEAN NOT NULL);";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.execute();
      DatabaseMetaData dbm = connection.getMetaData();
      ResultSet tables = dbm.getTables(null, null, "playerdata", null);
      if (tables.next())
        Logger.log(Component.text("Successfully created table", NamedTextColor.GREEN));
      else
        new Error("Failed to create table").log();
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
      else
        Logger.log(Component.text("Successfully opened connection", NamedTextColor.GREEN));
    } catch (SQLException exception) {
      new Error("Failed to check if connection is closed", exception).log();
    }
    createTable();
    return true;
  }
  
  @Override
  public void endStorageProcesses() {
    closeConnection();
  }
  
  public void wipeDatabase() {
    try {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("DELETE FROM playerdata");
    } catch (SQLException exception) {
      new Error("Failed to wipe database", exception).log();
    }
  }
  
  @Override
  public boolean createAccount(Account account) {
    try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO playerdata(uuid, name, balance, secondaryBalance, payable) VALUES(?,?,?,?,?)")) {
      UUID uuid = account.getUUID();
      pstmt.setString(1, uuid.toString());
      pstmt.setString(2, account.getName());
      pstmt.setDouble(3, account.getBalance());
      pstmt.setDouble(4, account.getSecondaryBalance());
      pstmt.setBoolean(5, account.getPayable());
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
    try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO playerdata(uuid, name, balance, secondaryBalance, payable) VALUES(?,?,?,?,?)")) {
      int i = 0;
      for (Account account : accounts) {
        UUID uuid = account.getUUID();
        try {
          pstmt.setString(1, uuid.toString());
          pstmt.setString(2, account.getName());
          pstmt.setDouble(3, account.getBalance());
          pstmt.setDouble(4, account.getSecondaryBalance());
          pstmt.setBoolean(5, account.getPayable());
          pstmt.addBatch();
        } catch (SQLException exception) {
          new Error("Failed to create account (" + uuid + ")", exception).log();
        }
        i++;
      }
      
      int[] affectedRows = pstmt.executeBatch();
      i = 0;
      
      for (Account account : accounts) {
        if (affectedRows[i] == 0) {
          new Error("Failed to create account (" + account.getUUID().toString() + ")").log();
        }
        i++;
      }
      
    } catch (SQLException exception) {
      new Error("Failed to create accounts", exception).log();
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
      return new Account(uuid).setName(rs.getString("name")).setBalance(rs.getDouble("balance")).setSecondaryBalance(rs.getDouble("secondaryBalance")).setPayable(rs.getBoolean("payable"));
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
        Account account = new Account(uuid)
          .setName(rs.getString("name"))
          .setBalance(rs.getDouble("balance"))
          .setSecondaryBalance(rs.getDouble("secondaryBalance"))
          .setPayable(rs.getBoolean("payable"));
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
        Account account = new Account(uuid)
          .setName(rs.getString("name"))
          .setBalance(rs.getDouble("balance"))
          .setSecondaryBalance(rs.getDouble("secondaryBalance"))
          .setPayable(rs.getBoolean("payable"));
        accounts.put(uuid, account);
      }
    } catch (SQLException exception) {
      new Error("Failed to get all accounts", exception);
    }
    
    return accounts;
  }
  
  
  @Override
  public void updateAccount(Account account) {
    
    try (PreparedStatement pstmt = connection.prepareStatement("UPDATE playerdata SET name = ?, balance = ?, secondaryBalance = ?, payable = ? WHERE uuid = ?")) {
      pstmt.setString(1, account.getName());
      pstmt.setDouble(2, account.getBalance());
      pstmt.setDouble(3, account.getSecondaryBalance());
      pstmt.setBoolean(4, account.getPayable());
      pstmt.setString(5, account.getUUID().toString());
      int affectedRows = pstmt.executeUpdate();
      
      if (affectedRows == 0) {
        new Error("Failed to update account (" + account.getUUID().toString() + ")").log();
      }
    } catch (SQLException exception) {
      new Error("Failed to update account (" + account.getUUID().toString() + ")", exception).log();
    }
  }
  
  @Override
  public void updateAccounts(Collection<Account> accounts) {
    if (accounts.isEmpty()) {
      return;
    }
    
    try (PreparedStatement pstmt = connection.prepareStatement("UPDATE playerdata SET name = ?, balance = ?, secondaryBalance = ?, payable = ? WHERE uuid = ?")) {
      int i = 0;
      for (Account account : accounts) {
        try {
          pstmt.setString(1, account.getName());
          pstmt.setDouble(2, account.getBalance());
          pstmt.setDouble(3, account.getSecondaryBalance());
          pstmt.setBoolean(4, account.getPayable());
          pstmt.setString(5, account.getUUID().toString());
          pstmt.addBatch();
        } catch (SQLException exception) {
          new Error("Failed to update account (" + account.getUUID().toString() + ")", exception).log();
        }
        i++;
      }
      
      int[] affectedRows = pstmt.executeBatch();
      i = 0;
      
      for (Account account : accounts) {
        if (affectedRows[i] == 0) {
          new Error("Failed to update account (" + account.getUUID().toString() + ")").log();
        }
        i++;
      }
      
    } catch (SQLException exception) {
      new Error("Failed to update accounts", exception).log();
    }
  }
  
  @Override
  public Collection<UUID> getAllUUIDs() {
    Collection<UUID> uuids = new ArrayList<>();
    
    try (PreparedStatement pstmt = connection.prepareStatement("SELECT uuid FROM playerdata")) {
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        uuids.add(UUID.fromString(rs.getString("uuid")));
      }
    } catch (SQLException exception) {
      new Error("Failed to get all UUIDs", exception);
    }
    
    return uuids;
  }
  
}
