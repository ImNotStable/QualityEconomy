package com.imnotstable.qualityeconomy.storage.importdata;

import com.google.gson.JsonObject;
import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.economy.Account;
import com.imnotstable.qualityeconomy.economy.BalanceEntry;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class V1_5_2 implements ImportData<JsonObject> {
  
  public boolean importData(JsonObject rootJSON) {
    // Currency Import
    JsonObject currenciesJSON = rootJSON.getAsJsonObject("CURRENCIES");
    YamlConfiguration currenciesYAML = YamlConfiguration.loadConfiguration(QualityEconomy.getCurrencyConfig().getFile());
    currenciesJSON.entrySet().forEach(currencyJSON -> {
      String currency = currencyJSON.getKey();
      JsonObject currencyData = currenciesJSON.getAsJsonObject(currency);
      for (String commandKey : new String[]{"admin-commands", "view-commands", "transfer-commands", "leaderboard-commands"}) {
        List<String> commands = new ArrayList<>();
        currencyData.getAsJsonArray(commandKey.toUpperCase()).forEach(command -> commands.add(command.getAsString()));
        currenciesYAML.set("currencies." + currency + "." + commandKey, commands);
      }
      currenciesYAML.set("currencies." + currency + ".leaderboard-refresh-interval", currencyData.get("LEADERBOARD-REFRESH-INTERVAL").getAsString());
      currenciesYAML.set("currencies." + currency + ".starting-balance", currencyData.get("STARTING-BALANCE").getAsDouble());
      currenciesYAML.set("currencies." + currency + ".singular-name", currencyData.get("SINGULAR-NAME").getAsString());
      currenciesYAML.set("currencies." + currency + ".plural-name", currencyData.get("PLURAL-NAME").getAsString());
      currenciesYAML.set("currencies." + currency + ".format-type", currencyData.get("FORMAT-TYPE").getAsString());
      currenciesYAML.set("currencies." + currency + ".decimal-places", currencyData.get("DECIMAL-PLACES").getAsInt());
      currenciesYAML.set("currencies." + currency + ".symbol", currencyData.get("SYMBOL").getAsString());
      currenciesYAML.set("currencies." + currency + ".symbol-position", currencyData.get("SYMBOL-POSITION").getAsString());
      currenciesYAML.set("currencies." + currency + ".custom-events", currencyData.get("CUSTOM-EVENTS").getAsString());
      currenciesYAML.set("currencies." + currency + ".transaction-logging", currencyData.get("TRANSACTION-LOGGING").getAsDouble());
      JsonObject messagesJSON = currencyData.getAsJsonObject("MESSAGES");
      for (String messageKey : messagesJSON.keySet()) {
        currenciesYAML.set("currencies." + currency + ".messages." + messageKey, messagesJSON.get(messageKey).getAsString());
      }
    });
    try {
      currenciesYAML.save(QualityEconomy.getCurrencyConfig().getFile());
    } catch (IOException exception) {
      Logger.logError("Failed to save currency config while importing", exception);
      return false;
    }
    // Account Import
    JsonObject accountsJSON = rootJSON.getAsJsonObject("ACCOUNTS");
    Collection<Account> accounts = new ArrayList<>();
    accountsJSON.entrySet().forEach(accountJSON -> {
      Account account = new Account(UUID.fromString(accountJSON.getKey()));
      JsonObject accountData = accountJSON.getValue().getAsJsonObject();
      account.setUsername(accountData.get("USERNAME").getAsString());
      Collection<BalanceEntry> balances = new ArrayList<>();
      accountData.getAsJsonObject("BALANCES").entrySet().forEach(balanceJSON -> {
        JsonObject balanceData = balanceJSON.getValue().getAsJsonObject();
        balances.add(new BalanceEntry(balanceJSON.getKey(), balanceData.get("BALANCE").getAsDouble(), balanceData.get("PAYABLE").getAsBoolean()));
      });
      accounts.add(account.initializeBalanceEntries(balances));
    });
    StorageManager.getActiveStorageType().wipeDatabase();
    StorageManager.getActiveStorageType().createAccounts(accounts);
    AccountManager.setupAccounts();
    return true;
  }
  
}
