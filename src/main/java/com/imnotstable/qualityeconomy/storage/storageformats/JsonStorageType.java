package com.imnotstable.qualityeconomy.storage.storageformats;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.Misc;
import com.imnotstable.qualityeconomy.util.storage.EasyJson;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JsonStorageType extends EasyJson implements StorageType {
  
  @Override
  public boolean initStorageProcesses() {
    if (json != null) return false;
    try {
      if (!file.exists()) {
        if (!file.createNewFile())
          return false;
        json = new JsonObject();
      } else {
        try (FileReader reader = new FileReader(file)) {
          json = new Gson().fromJson(reader, JsonObject.class);
        }
      }
      json.getAsJsonArray("custom-currencies").forEach(currency -> currencies.add(currency.getAsString()));
    } catch (IOException exception) {
      new Debug.QualityError("Failed to initiate storage processes", exception).log();
      return false;
    }
    return true;
  }
  
  @Override
  public void endStorageProcesses() {
    if (json == null) return;
    if (file.exists())
      save();
    json = null;
  }
  
  @Override
  public void wipeDatabase() {
    file.delete();
    endStorageProcesses();
    initStorageProcesses();
  }
  
  @Override
  public void createAccount(Account account) {
    json.add(String.valueOf(account.getUUID()), serialize(account));
    save();
  }
  
  @Override
  public void createAccounts(Collection<Account> accounts) {
    accounts.forEach(account -> json.add(String.valueOf(account.getUUID()), serialize(account)));
    save();
  }
  
  @Override
  public void updateAccounts(Collection<Account> accounts) {
    accounts.forEach(account -> json.add(String.valueOf(account.getUUID()), serialize(account)));
    save();
  }
  
  @Override
  public Map<UUID, Account> getAllAccounts() {
    HashMap<UUID, Account> accounts = new HashMap<>();
    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
      if (!Misc.isValidUUID(entry.getKey()))
        continue;
      UUID uuid = UUID.fromString(entry.getKey());
      JsonObject accountJson = entry.getValue().getAsJsonObject();
      Account account = new Account(uuid)
        .setUsername(accountJson.get("USERNAME").getAsString())
        .setBalance(accountJson.get("BALANCE").getAsDouble());
      if (Configuration.isCommandEnabled("pay"))
        account.setPayable(accountJson.get("PAYABLE").getAsBoolean());
      if (Configuration.isCommandEnabled("request"))
        account.setPayable(accountJson.get("REQUESTABLE").getAsBoolean());
      if (Configuration.areCustomCurrenciesEnabled())
        for (String currency : getCurrencies())
          account.setCustomBalance(currency, accountJson.get(currency).getAsDouble());
      accounts.put(uuid, account);
    }
    return accounts;
  }
  
  @Override
  public void addCurrency(String currency) {
    currency = super.addCurrencyAttempt(currency);
    if (currency == null)
      return;
    JsonArray currencies = json.getAsJsonArray("custom-currencies");
    if (currencies == null || currencies.isEmpty())
      currencies = new JsonArray();
    currencies.add(currency);
    json.add("custom-currencies", currencies);
    super.addCurrencySuccess(currency);
    save();
  }
  
  @Override
  public void removeCurrency(String currency) {
    currency = super.removeCurrencyAttempt(currency);
    if (currency == null)
      return;
    JsonArray currencies = json.getAsJsonArray("custom-currencies");
    currencies.remove(new Gson().toJsonTree(currency));
    json.add("custom-currencies", currencies);
    super.removeCurrencySuccess(currency);
    save();
  }
  
}
