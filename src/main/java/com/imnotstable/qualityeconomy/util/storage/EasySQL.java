package com.imnotstable.qualityeconomy.util.storage;

import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.Misc;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EasySQL extends EasyCurrencies {
  
  private static final int H2 = 1;
  private static final int SQLITE = 2;
  private static final int MYSQL = 3;
  private static final int MARIADB = 4;
  private static final int POSTGRESQL = 5;
  
  static {
    try {
      DriverManager.registerDriver(new org.h2.Driver());
      DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
      DriverManager.registerDriver(new org.postgresql.Driver());
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to load JBDC Drivers", exception).log();
    }
  }
  
  private final int databaseType;
  protected HikariDataSource dataSource;
  protected List<String> columns = new ArrayList<>();
  protected @Getter String insertStatement;
  protected @Getter String updateStatement;
  
  protected EasySQL(int databaseType) {
    this.databaseType = databaseType;
    open();
  }
  
  protected Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }
  
  protected void open() {
    openDataSource();
    try (Connection connection = getConnection()) {
      createPlayerDataTable(connection);
      columns = getColumns(connection);
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to start database", exception).log();
    }
    generateStatements();
  }
  
  protected void close() {
    if (dataSource == null) {
      new Debug.QualityError("Attempted to close datasource when datasource doesn't exist").log();
      return;
    }
    if (dataSource.isClosed()) {
      new Debug.QualityError("Attempted to close datasource when datasource is already closed").log();
      return;
    }
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
        new Debug.QualityError("Invalid database type: " + databaseType).log();
        return;
      }
    }
    hikariConfig.setPoolName("QualityEconomyPool");
    dataSource = new HikariDataSource(hikariConfig);
  }
  
  private void setupDatasource(HikariConfig hikariConfig, String type) {
    String database = Configuration.getDatabaseInfo(0, "qualityeconomy");
    String address = Configuration.getDatabaseInfo(1, "localhost");
    String port = Configuration.getDatabaseInfo(2, "3306");
    String username = Configuration.getDatabaseInfo(3, "root");
    String password = Configuration.getDatabaseInfo(4, "root");
    hikariConfig.setJdbcUrl(String.format("jdbc:%s://%s:%s/%s", type, address, port, database));
    hikariConfig.setUsername(username);
    hikariConfig.setPassword(password);
    Map<String, Integer> settings = Configuration.getAdvancedSettings();
    hikariConfig.setMaximumPoolSize(settings.get("maximum-pool-size"));
    hikariConfig.setMinimumIdle(settings.get("minimum-idle"));
    hikariConfig.setMaxLifetime(settings.get("maximum-liftime"));
    hikariConfig.setKeepaliveTime(settings.get("keepalive-time"));
    hikariConfig.setConnectionTimeout(settings.get("connection-timeout"));
  }
  
  protected void createPlayerDataTable(Connection connection) throws SQLException {
    executeStatement(connection, "CREATE TABLE IF NOT EXISTS PLAYERDATA(UUID CHAR(36) PRIMARY KEY, USERNAME VARCHAR(16), BALANCE FLOAT(53) NOT NULL);");
  }
  
  protected void dropPlayerDataTable(Connection connection) throws SQLException {
    executeStatement(connection, "DROP TABLE PLAYERDATA;");
  }
  
  protected void createCurrencyTable(Connection connection) throws SQLException {
    executeStatement(connection, "CREATE TABLE IF NOT EXISTS CURRENCIES(CURRENCY VARCHAR(255) PRIMARY KEY);");
  }
  
  protected void dropCurrencyTable(Connection connection) throws SQLException {
    executeStatement(connection, "DROP TABLE CURRENCIES;");
  }
  
  protected boolean currencyTableExists(DatabaseMetaData metaData) throws SQLException {
    try (ResultSet resultSet = metaData.getTables(null, null, "CURRENCIES", null)) {
      return resultSet.next();
    }
  }
  
  protected void executeStatement(Connection connection, String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }
  
  protected List<String> getColumns(Connection connection) throws SQLException {
    List<String> columns = new ArrayList<>();
    try (ResultSet rs = connection.getMetaData().getColumns(null, null, "PLAYERDATA", null)) {
      while (rs.next()) {
        columns.add(rs.getString("COLUMN_NAME"));
      }
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to get columns of database", exception).log();
      throw exception;
    }
    return columns;
  }
  
  protected boolean columnExists(DatabaseMetaData metaData, String column) throws SQLException {
    try (ResultSet columns = metaData.getColumns(null, null, "PLAYERDATA", column)) {
      return columns.next();
    } catch (SQLException exception) {
      new Debug.QualityError("Failed to check if column exists", exception).log();
      throw exception;
    }
  }
  
  protected void addColumn(Connection connection, String column, String type, String def) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate(String.format("ALTER TABLE PLAYERDATA ADD COLUMN %s %s NOT NULL DEFAULT %s;", column, type, def));
      columns.add(column);
      generateStatements();
    }
  }
  
  protected void dropColumn(Connection connection, String column) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate(String.format("ALTER TABLE PLAYERDATA DROP COLUMN %s;", column));
      columns.remove(column);
      generateStatements();
    }
  }
  
  protected void createAccountSetter(PreparedStatement preparedStatement, Account account) throws SQLException {
    UUID uuid = account.getUniqueId();
    preparedStatement.setString(1, uuid.toString());
    preparedStatement.setString(2, account.getUsername());
    preparedStatement.setDouble(3, account.getBalance());
    if (Configuration.isCommandEnabled("pay"))
      preparedStatement.setBoolean(columns.indexOf("PAYABLE") + 1, account.isPayable());
    if (Configuration.isCommandEnabled("request"))
      preparedStatement.setBoolean(columns.indexOf("REQUESTABLE") + 1, account.isRequestable());
    if (Configuration.isCustomCurrenciesEnabled())
      for (String currency : currencies)
        preparedStatement.setDouble(columns.indexOf(currency) + 1, account.getCustomBalance(currency));
  }
  
  private void generateStatements() {
    //Create Account
    StringBuilder insert1 = new StringBuilder("UUID,USERNAME,BALANCE");
    StringBuilder insert2 = new StringBuilder("?,?,?");
    //Update Account
    StringBuilder update = new StringBuilder("UPDATE PLAYERDATA SET USERNAME = ?, BALANCE = ?");
    
    for (String column : columns) {
      if (Misc.equals(column, "UUID", "USERNAME", "BALANCE"))
        continue;
      insert1.append(",").append(column);
      insert2.append(",?");
      update.append(", ").append(column).append(" = ?");
    }
    
    insertStatement = "INSERT INTO PLAYERDATA(" + insert1 + ") VALUES(" + insert2 + ");";
    updateStatement = update.append(" WHERE UUID = ?;").toString();
  }
  
}
