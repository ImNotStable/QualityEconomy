package com.imnotstable.qualityeconomy.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQLUtils {
  
  public static DatabaseMetaData getDatabaseMetaData(Connection connection) {
    try {
      return connection.getMetaData();
    } catch (SQLException exception) {
      new QualityError("Failed to get database metadata", exception).log();
    }
    return null;
  }
  
  public static boolean[] columnsExist(DatabaseMetaData metaData, String[] columns) throws SQLException {
    boolean[] column = new boolean[columns.length];
    for (int i = 0; i < columns.length; i++)
      column[i] = columnExists(metaData, columns[i]);
    return column;
  }
  
  public static boolean columnExists(DatabaseMetaData metaData, String column) throws SQLException {
    
    try (ResultSet columns = metaData.getColumns(null, null, "PLAYERDATA", column)) {
      return columns.next();
    } catch (SQLException exception) {
      new QualityError("Failed to check if column exists (" + column + ")", exception).log();
      throw exception;
    }
  }
  
  public static void addColumn(Connection connection, String columnName, String type) {
    columnName = columnName.toUpperCase();
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate("ALTER TABLE PLAYERDATA ADD COLUMN " + columnName + " " + type);
    } catch (SQLException exception) {
      new QualityError("Failed to add column to database (" + columnName + ")", exception).log();
    }
  }
  
  public static void dropColumn(Connection connection, String columnName) {
    columnName = columnName.toUpperCase();
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate("ALTER TABLE PLAYERDATA DROP COLUMN " + columnName);
    } catch (SQLException exception) {
      new QualityError("Failed to add column to database (" + columnName + ")", exception).log();
    }
  }
  
  public static List<String> getColumns(DatabaseMetaData metaData) {
    List<String> columns = new ArrayList<>();
    try (ResultSet rs = metaData.getColumns(null, null, "PLAYERDATA", null)) {
      while (rs.next()) {
        String columnName = rs.getString("COLUMN_NAME");
        columns.add(columnName);
      }
    } catch (SQLException exception) {
      new QualityError("Failed to get name of all columns", exception).log();
    }
    return columns;
  }
  
}
