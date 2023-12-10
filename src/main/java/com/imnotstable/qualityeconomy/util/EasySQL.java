package com.imnotstable.qualityeconomy.util;

import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EasySQL {

  public static final int H2 = 1;
  public static final int SQLITE = 2;
  public static final int MYSQL = 3;
  public static final int MARIADB = 4;
  
  static {
    try {
      DriverManager.registerDriver(new org.h2.Driver());
      DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
    } catch (SQLException exception) {
      new QualityError("Failed to load H2 Driver", exception).log();
    }
  }
  
  private final int databaseType;
  protected HikariDataSource dataSource;
  protected List<String> columns = new ArrayList<>();
  protected @Getter String insertStatement;
  protected @Getter String updateStatement;
  
  protected EasySQL(int databaseType) {
    this.databaseType = databaseType;
    openDataSource();
    try (Connection connection = openConnection()) {
      createTable(connection, "PLAYERDATA", "UUID CHAR(36) PRIMARY KEY, USERNAME VARCHAR(16), BALANCE REAL NOT NULL");
      columns = getColumns(connection);
    } catch (SQLException exception) {
      new QualityError("Failed to start database", exception).log();
    }
    generateStatements();
  }
  
  protected void close() {
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
  
  protected void openDataSource() {
    HikariConfig hikariConfig = new HikariConfig();
    switch (databaseType) {
      case H2 -> {
        hikariConfig.setJdbcUrl("jdbc:h2:./plugins/QualityEconomy/playerdata");
        hikariConfig.setUsername("sa");
        hikariConfig.setPassword("");
      }
      case SQLITE -> hikariConfig.setJdbcUrl("jdbc:sqlite:plugins/QualityEconomy/playerdata.db");
      case MYSQL -> {
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
      case MARIADB -> {
        String address = Configuration.getMySQL().get(0);
        String name = Configuration.getMySQL().get(1);
        String user = Configuration.getMySQL().get(2);
        String password = Configuration.getMySQL().get(3);
        hikariConfig.setJdbcUrl(String.format("jdbc:mariadb://%s/%s", address, name));
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      }
      default -> {
        new QualityError("Invalid database type: " + databaseType).log();
        return;
      }
    }
    hikariConfig.setMaximumPoolSize(10);
    hikariConfig.setMinimumIdle(5);
    hikariConfig.setConnectionTimeout(60000);
    hikariConfig.setMaxLifetime(1800000);
    hikariConfig.setPoolName("QualityEconomyPool");
    dataSource = new HikariDataSource(hikariConfig);
  }
  
  private Connection openConnection() throws SQLException {
    return dataSource.getConnection();
  }
  
  protected void createTable(Connection connection, String table, String types) throws SQLException {
    executeStatement(connection, "CREATE TABLE IF NOT EXISTS " + table + "(" + types + ");");
  }
  
  protected void dropTable(Connection connection, String table) throws SQLException {
    executeStatement(connection, "DROP TABLE" + table + ";");
  }
  
  protected boolean tableExists(DatabaseMetaData metaData, String table) throws SQLException {
    try (ResultSet resultSet = metaData.getTables(null, null, table, null)) {
      return resultSet.next();
    }
  }
  
  protected DatabaseMetaData getMetaData(Connection connection) throws SQLException {
    return connection.getMetaData();
  }
  
  protected void executeStatement(Connection connection, String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }
  
  protected List<String> getColumns(Connection connection) throws SQLException {
    List<String> columns = new ArrayList<>();
    try (ResultSet rs = getMetaData(connection).getColumns(null, null, "PLAYERDATA", null)) {
      while (rs.next()) {
        columns.add(rs.getString("COLUMN_NAME"));
      }
    } catch (SQLException exception) {
      new QualityError("Failed to get columns of database", exception).log();
      throw exception;
    }
    return columns;
  }
  
  protected boolean columnExists(DatabaseMetaData metaData, String column) throws SQLException {
    try (ResultSet columns = metaData.getColumns(null, null, "PLAYERDATA", column)) {
      return columns.next();
    } catch (SQLException exception) {
      new QualityError("Failed to check if column exists", exception).log();
      throw exception;
    }
  }
  
  protected void addColumn(Connection connection, String column, String type) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate("ALTER TABLE PLAYERDATA ADD COLUMN " + column + " " + type);
      columns.add(column);
      generateStatements();
    }
  }
  
  protected void dropColumn(Connection connection, String column) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate("ALTER TABLE PLAYERDATA DROP COLUMN " + column);
      columns.remove(column);
      generateStatements();
    }
  }
  
  private void generateStatements() {
    //Create Account
    StringBuilder insert1 = new StringBuilder("UUID,USERNAME,BALANCE");
    StringBuilder insert2 = new StringBuilder("?,?,?");
    //Update Account
    StringBuilder update = new StringBuilder("UPDATE PLAYERDATA SET USERNAME = ?, BALANCE = ?");
    
    for (String column : columns) {
      if (column.equals("UUID") || column.equals("USERNAME") || column.equals("BALANCE"))
        continue;
      insert1.append(",").append(column);
      insert2.append(",?");
      update.append(", ").append(column).append(" = ?");
    }
    update.append(" WHERE UUID = ?");
    
    insertStatement = "INSERT INTO PLAYERDATA(" + insert1 + ") VALUES(" + insert2 + ")";
    updateStatement = update.toString();
  }
  
}
