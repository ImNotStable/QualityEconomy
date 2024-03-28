package com.imnotstable.qualityeconomy.util.storage;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.Misc;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;

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
        new Debug.QualityError("Invalid database type: " + databaseType).log();
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
    Debug.Logger.log(Strings.join(columns, ','));
    Debug.Logger.log(uuid + "," + account.getUsername() + "," + account.getBalance());
    if (QualityEconomy.getQualityConfig().COMMANDS_PAY) {
      Debug.Logger.log("isPayable: " + account.isPayable());
      preparedStatement.setBoolean(columns.indexOf("PAYABLE") + 1, account.isPayable());
    }
    if (QualityEconomy.getQualityConfig().COMMANDS_REQUEST)
      preparedStatement.setBoolean(columns.indexOf("REQUESTABLE") + 1, account.isRequestable());
    if (QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES)
      for (String currency : currencies)
        preparedStatement.setDouble(columns.indexOf(currency) + 1, account.getCustomBalance(currency));
  }
  
  protected void generateStatements() {
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
