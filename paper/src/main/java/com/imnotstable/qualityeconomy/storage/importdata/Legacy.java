package com.imnotstable.qualityeconomy.storage.importdata;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.economy.Account;
import com.imnotstable.qualityeconomy.economy.BalanceEntry;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import com.imnotstable.qualityeconomy.util.debug.Timer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Legacy implements ImportData<JsonObject> {
  
  public boolean importData(JsonObject rootJSON) {
    Timer timer = new Timer("importDatabase()");
    AccountManager.clearAccounts();
    StorageManager.getActiveStorageType().wipeDatabase();
    Collection<Account> accounts = new ArrayList<>();
    List<String> currencies = new ArrayList<>();
    if (rootJSON.has("CUSTOM-CURRENCIES")) {
      JsonArray currenciesJSON = rootJSON.getAsJsonArray("CUSTOM-CURRENCIES");
      YamlConfiguration currenciesYAML = YamlConfiguration.loadConfiguration(QualityEconomy.getCurrencyConfig().getFile());
      for (JsonElement currencyJSON : currenciesJSON) {
        String currency = currencyJSON.getAsString();
        currencies.add(currency);
        currenciesYAML.set("currencies.admin-commands", List.of());
        currenciesYAML.set("currencies.view-commands", List.of());
        currenciesYAML.set("currencies.transfer-commands", List.of());
        currenciesYAML.set("currencies.leaderboard-commands", List.of());
        currenciesYAML.set("currencies.singular-name", currency);
        currenciesYAML.set("currencies.plural-name", currency + "s");
        currenciesYAML.set("currencies.symbol", currency.toCharArray()[0]);
        currenciesYAML.set("currencies.symbol-position", "before");
        currenciesYAML.set("currencies.decimal-places", 2);
        currenciesYAML.set("currencies.starting-balance", 0.0);
      }
      rootJSON.remove("CUSTOM-CURRENCIES");
      try {
        currenciesYAML.save(QualityEconomy.getCurrencyConfig().getFile());
      } catch (IOException exception) {
        Logger.logError("Failed to save currency config while importing", exception);
        return false;
      }
    }
    rootJSON.entrySet().forEach(entry -> {
      JsonObject accountJSON = entry.getValue().getAsJsonObject();
      UUID uuid = UUID.fromString(entry.getKey());
      String name = accountJSON.get("NAME").getAsString();
      Collection<BalanceEntry> balances = new ArrayList<>();
      balances.add(new BalanceEntry("default", accountJSON.get("BALANCE").getAsDouble(), accountJSON.get("PAYABLE").getAsBoolean()));
      accountJSON.entrySet().forEach(rawEntry -> {
        if (currencies.contains(rawEntry.getKey()))
          balances.add(new BalanceEntry(rawEntry.getKey(), rawEntry.getValue().getAsDouble(), true));
      });
      accounts.add(new Account(uuid).setUsername(name).initializeBalanceEntries(balances));
    });
    StorageManager.getActiveStorageType().wipeDatabase();
    StorageManager.getActiveStorageType().createAccounts(accounts);
    AccountManager.setupAccounts();
    timer.end();
    return true;
  }
  
}
