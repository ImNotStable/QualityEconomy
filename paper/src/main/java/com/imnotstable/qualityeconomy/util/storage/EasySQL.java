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
  
  @Getter
  protected final String insertAccountStatement = "INSERT INTO ACCOUNTS(UUID,USERNAME) VALUES(?,?);";
  @Getter
  protected final String updateAccountStatement = "UPDATE ACCOUNTS SET USERNAME = ? WHERE UUID = ?;";
  @Getter
  protected final String insertBalanceStatement = "INSERT INTO BALANCES(UUID,CURRENCY,BALANCE,PAYABLE) VALUES(?,?,?,?);";
  @Getter
  protected final String updateBalanceStatement = "UPDATE BALANCES SET BALANCE = ?, PAYABLE = ? WHERE UUID = ? AND CURRENCY = ?;";
  
  static {
    try {
      DriverManager.registerDriver(new org.h2.Driver());
      DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
    } catch (SQLException exception) {
      Logger.logError("Failed to load JBDC Drivers", exception);
    }
  }
  
  private final SQLDriver databaseType;
  protected HikariDataSource dataSource;
  
  protected EasySQL(SQLDriver databaseType) {
    this.databaseType = databaseType;
  }
  
  protected Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }
  
  protected void open() {
    HikariConfig hikariConfig = new HikariConfig();
    switch (databaseType) {
      case H2 -> hikariConfig.setJdbcUrl("jdbc:h2:./plugins/QualityEconomy/playerdata");
      case SQLITE -> hikariConfig.setJdbcUrl("jdbc:sqlite:./plugins/QualityEconomy/playerdata.sqlite");
      case MYSQL -> setupDatasource(hikariConfig, "mysql");
      case MARIADB -> setupDatasource(hikariConfig, "mariadb");
    }
    Map<String, Object> settings = QualityEconomy.getQualityConfig().DATABASE_INFORMATION_ADVANCED_SETTINGS;
    hikariConfig.setMaximumPoolSize((int) settings.getOrDefault("maximum-pool-size", 10));
    hikariConfig.setMinimumIdle((int) settings.getOrDefault("minimum-idle", 10));
    hikariConfig.setMaxLifetime((int) settings.getOrDefault("maximum-liftime", 1800000));
    hikariConfig.setKeepaliveTime((int) settings.getOrDefault("keepalive-time", 0));
    hikariConfig.setConnectionTimeout((int) settings.getOrDefault("connection-timeout", 5000));
    hikariConfig.setPoolName("QualityEconomyPool");
    dataSource = new HikariDataSource(hikariConfig);
  }
  
  protected void close() {
    dataSource.close();
  }
  
  private void setupDatasource(HikariConfig hikariConfig, String type) {
    Map<String, String> databaseInfo = QualityEconomy.getQualityConfig().DATABASE_INFORMATION;
    String database = databaseInfo.getOrDefault("database", "qualityeconomy");
    String address = databaseInfo.getOrDefault("address", "localhost");
    String port = databaseInfo.getOrDefault("port", "3306");
    String username = databaseInfo.getOrDefault("username", "root");
    String password = databaseInfo.getOrDefault("password", "root");
    hikariConfig.setJdbcUrl(String.format("jdbc:%s://%s:%s/%s", type, address, port, database));
    hikariConfig.setUsername(username);
    hikariConfig.setPassword(password);
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
  
  protected void createBalanceTable(Connection connection) throws SQLException {
    executeStatement(connection, """
    CREATE TABLE IF NOT EXISTS BALANCES(
    UUID CHAR(36),
    CURRENCY VARCHAR(255),
    BALANCE FLOAT(53) NOT NULL,
    PAYABLE BOOLEAN,
    FOREIGN KEY(UUID) REFERENCES ACCOUNTS(UUID));
    """);
  }
  
  protected void dropBalanceTable(Connection connection) throws SQLException {
    executeStatement(connection, "DROP TABLE BALANCES;");
  }
  
  protected void executeStatement(Connection connection, String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }
  
}
