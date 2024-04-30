package com.imnotstable.qualityeconomy.util.storage;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class EasySQL {
  
  static {
    try {
      DriverManager.registerDriver(new org.h2.Driver());
      DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
    } catch (SQLException exception) {
      Logger.logError("Failed to load JBDC Drivers", exception);
    }
  }
  
  @Getter
  protected final String insertAccountStatement = "INSERT INTO ACCOUNTS(UUID,USERNAME) VALUES(?,?);";
  @Getter
  protected final String updateAccountStatement = "UPDATE ACCOUNTS SET USERNAME = ? WHERE UUID = ?;";
  @Getter
  protected final String insertBalanceStatement = "INSERT INTO BALANCES(UUID,CURRENCY,BALANCE,PAYABLE) VALUES(?,?,?,?);";
  @Getter
  protected final String upsertBalanceStatement;
  protected HikariDataSource dataSource;
  
  protected EasySQL(SQLDriver driver) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(getJDBCUrl(driver));
    upsertBalanceStatement = driver.getUPSERT_BALANCE_STATEMENT();
    Map<String, Object> settings = QualityEconomy.getQualityConfig().DATABASE_INFORMATION_ADVANCED_SETTINGS;
    hikariConfig.setMaximumPoolSize((int) settings.getOrDefault("maximum-pool-size", 10));
    hikariConfig.setMinimumIdle((int) settings.getOrDefault("minimum-idle", 10));
    hikariConfig.setMaxLifetime((int) settings.getOrDefault("maximum-liftime", 1800000));
    hikariConfig.setKeepaliveTime((int) settings.getOrDefault("keepalive-time", 0));
    hikariConfig.setConnectionTimeout((int) settings.getOrDefault("connection-timeout", 5000));
    hikariConfig.setAutoCommit(false);
    hikariConfig.setPoolName("QualityEconomyPool");
    dataSource = new HikariDataSource(hikariConfig);
  }
  
  protected Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }
  
  protected void close() {
    dataSource.close();
  }
  
  private String getJDBCUrl(SQLDriver driver) {
    String url = driver.getJDBC_URL();
    if (driver == SQLDriver.MYSQL || driver == SQLDriver.MARIADB) {
      Map<String, String> databaseInfo = QualityEconomy.getQualityConfig().DATABASE_INFORMATION;
      String username = databaseInfo.getOrDefault("username", "root");
      String password = databaseInfo.getOrDefault("password", "root");
      String address = databaseInfo.getOrDefault("address", "localhost");
      String port = databaseInfo.getOrDefault("port", "3306");
      String database = databaseInfo.getOrDefault("database", "qualityeconomy");
      url = String.format(url, username, password, address, port, database);
    }
    return url;
  }
  
  protected void createAccountsTable(Connection connection) throws SQLException {
    executeStatement(connection, """
      CREATE TABLE IF NOT EXISTS ACCOUNTS(
      UUID CHAR(36) PRIMARY KEY,
      USERNAME VARCHAR(16));
      """);
  }
  
  protected void dropAccountsTable(Connection connection) throws SQLException {
    executeStatement(connection, "DROP TABLE ACCOUNTS;");
  }
  
  protected void createBalancesTable(Connection connection) throws SQLException {
    executeStatement(connection, """
      CREATE TABLE IF NOT EXISTS BALANCES(
      UUID CHAR(36),
      CURRENCY VARCHAR(255),
      BALANCE FLOAT(53),
      PAYABLE BOOLEAN,
      FOREIGN KEY(UUID) REFERENCES ACCOUNTS(UUID),
      UNIQUE(UUID, CURRENCY));
      """);
  }
  
  protected void dropBalancesTable(Connection connection) throws SQLException {
    executeStatement(connection, "DROP TABLE BALANCES;");
  }
  
  protected void executeStatement(Connection connection, String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }
  
}
