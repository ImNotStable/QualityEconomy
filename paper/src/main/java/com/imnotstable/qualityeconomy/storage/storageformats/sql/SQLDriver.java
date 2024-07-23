package com.imnotstable.qualityeconomy.storage.storageformats.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SQLDriver {
  
  H2("jdbc:h2:./plugins/QualityEconomy/playerdata;MODE=MariaDB",
    "INSERT INTO BALANCES(UUID,CURRENCY,BALANCE,PAYABLE) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE BALANCE = BALANCES.BALANCE + VALUES(BALANCE), PAYABLE = VALUES(PAYABLE);"),
  SQLITE("jdbc:sqlite:./plugins/QualityEconomy/playerdata.sqlite",
    "INSERT INTO BALANCES(UUID, CURRENCY, BALANCE, PAYABLE) VALUES (?, ?, ?, ?) ON CONFLICT (UUID, CURRENCY) DO UPDATE SET BALANCE = BALANCES.BALANCE + EXCLUDED.BALANCE, PAYABLE = excluded.PAYABLE;"),
  MYSQL("jdbc:mysql://%s:%s@%s:%s/%s",
    "INSERT INTO BALANCES(UUID,CURRENCY,BALANCE,PAYABLE) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE BALANCE = BALANCES.BALANCE + VALUES(BALANCE), PAYABLE = VALUES(PAYABLE);"),
  MARIADB("jdbc:mariadb://%s:%s@%s:%s/%s",
    "INSERT INTO BALANCES(UUID,CURRENCY,BALANCE,PAYABLE) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE BALANCE = BALANCES.BALANCE + VALUES(BALANCE), PAYABLE = VALUES(PAYABLE);");
  
  private final String JDBC_URL;
  private final String UPSERT_BALANCE_STATEMENT;
  
}
