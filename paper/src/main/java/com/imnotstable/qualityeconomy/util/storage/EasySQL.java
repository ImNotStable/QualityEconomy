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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EasySQL {
  
  private static final int H2 = 1;
  private static final int SQLITE = 2;
  private static final int MYSQL = 3;
  private static final int MARIADB = 4;
  private static final int POSTGRESQL = 5;
  
  @Getter
  protected final String INSERT_ACCOUNT = "INSERT INTO ACCOUNTS(UUID,USERNAME) VALUES(?,?);";
  @Getter
  protected final String UPDATE_ACCOUNT = "UPDATE ACCOUNTS SET USERNAME = ? WHERE UUID = ?;";
  @Getter
  protected final String INSERT_BALANCE = "INSERT INTO BALANCE(UUID,CURRENCY,BALANCE,PAYABLE) VALUES(?,?,?,?);";
  @Getter
  protected final String UPDATE_BALANCE = "UPDATE BALANCE SET BALANCE = ?, PAYABLE = ? WHERE UUID = ? AND CURRENCY = ?;";
  
  static {
    try {
      DriverManager.registerDriver(new org.h2.Driver());
      DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
      DriverManager.registerDriver(new org.postgresql.Driver());
    } catch (SQLException exception) {
      Logger.logError("Failed to load JBDC Drivers", exception);
    }
  }
  
  private final int databaseType;
  protected HikariDataSource dataSource;
  protected List<String> columns = new ArrayList<>();
  
  protected EasySQL(int databaseType) {
    this.databaseType = databaseType;
  }
  
  protected Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }
  
  protected void open() {
    openDataSource();
  }
  
  protected void close() {
    dataSource.close();
  }
  
  protected void openDataSource() {
    HikariConfig hikariConfig = new HikariConfig();
    switch (databaseType) {
      case H2 -> hikariConfig.setJdbcUrl("jdbc:h2:./plugins/QualityEconomy/playerdata");
      case SQLITE -> hikariConfig.setJdbcUrl("jdbc:sqlite:./plugins/QualityEconomy/playerdata.sqlite");
      case MYSQL -> setupDatasource(hikariConfig, "mysql");
      case MARIADB -> setupDatasource(hikariConfig, "mariadb");
      case POSTGRESQL -> setupDatasource(hikariConfig, "postgresql");
      default -> {
        Logger.logError("Invalid database type: " + databaseType);
        return;
      }
    }
    hikariConfig.setPoolName("QualityEconomyPool");
    dataSource = new HikariDataSource(hikariConfig);
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
    Map<String, Object> settings = QualityEconomy.getQualityConfig().DATABASE_INFORMATION_ADVANCED_SETTINGS;
    hikariConfig.setMaximumPoolSize((int) settings.getOrDefault("maximum-pool-size", 10));
    hikariConfig.setMinimumIdle((int) settings.getOrDefault("minimum-idle", 10));
    hikariConfig.setMaxLifetime((int) settings.getOrDefault("maximum-liftime", 1800000));
    hikariConfig.setKeepaliveTime((int) settings.getOrDefault("keepalive-time", 0));
    hikariConfig.setConnectionTimeout((int) settings.getOrDefault("connection-timeout", 5000));
  }
  
  protected void createAccountsTable(Connection connection) throws SQLException {
    executeStatement(connection, """
      CREATE TABLE IF NOT EXISTS ACCOUNTS(
      UUID CHAR(36) PRIMARY KEY,
      USERNAME VARCHAR(16),
      );
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
