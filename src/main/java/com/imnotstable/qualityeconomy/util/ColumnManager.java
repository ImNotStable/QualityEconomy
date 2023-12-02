package com.imnotstable.qualityeconomy.util;

import lombok.Getter;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class ColumnManager {
  
  private final @Getter List<String> columns = new ArrayList<>();
  private @Getter String createStatement;
  private @Getter String updateStatement;
  
  public ColumnManager(Connection connection) {
    columns.addAll(SQLUtils.getColumns(SQLUtils.getDatabaseMetaData(connection)));
    loadStatements();
  }
  
  public void updateColumns(String column, boolean add) {
    if (add) {
      columns.add(column);
    } else {
      columns.remove(column);
    }
    loadStatements();
  }
  
  public void loadStatements() {
    
    //Create
    StringBuilder columnNames = new StringBuilder("UUID, NAME, BALANCE");
    StringBuilder placeholders = new StringBuilder("?,?,?");
    //Update
    StringBuilder sql = new StringBuilder("UPDATE PLAYERDATA SET NAME = ?, BALANCE = ?");
    
    for (String columnName : columns) {
      if (columnName.equals("UUID") || columnName.equals("NAME") || columnName.equals("BALANCE"))
        continue;
      //Create
      columnNames.append(", ").append(columnName);
      placeholders.append(",?");
      //Update
      sql.append(", ").append(columnName).append(" = ?");
    }
    
    //Create
    createStatement = "INSERT INTO PLAYERDATA(" + columnNames + ") VALUES(" + placeholders + ")";
    //Update
    sql.append(" WHERE UUID = ?");
    updateStatement = sql.toString();
  }
  
  
}
