package com.imnotstable.qualityeconomy.storage.storageformats;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import com.imnotstable.qualityeconomy.util.storage.EasyJson;
import org.jetbrains.annotations.NotNull;

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
      toggleCustomCurrencies();
      save();
    } catch (IOException exception) {
      Logger.logError("Failed to initiate storage processes", exception);
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
  public void createAccount(@NotNull Account account) {
    json.add(String.valueOf(account.getUniqueId()), serialize(account));
    save();
  }
  
  @Override
  public void createAccounts(@NotNull Collection<Account> accounts) {
    accounts.forEach(account -> json.add(String.valueOf(account.getUniqueId()), serialize(account)));
    save();
  }
  
  @Override
  public void saveAllAccounts() {
    AccountManager.getAllAccounts().stream()
      .filter(Account::requiresUpdate)
      .forEach(account -> json.add(String.valueOf(account.getUniqueId()), serialize(account.update())));
    save();
  }
  
  @Override
  public @NotNull Map<UUID, Account> getAllAccounts() {
    HashMap<UUID, Account> accounts = new HashMap<>();
    for (Map.Entry<String, JsonElement> entry : getEntrySet()) {
      UUID uuid = UUID.fromString(entry.getKey());
      JsonObject accountJson = entry.getValue().getAsJsonObject();
      Account account = new Account(uuid)
        .setUsername(accountJson.get("USERNAME").getAsString())
        .setBalance(accountJson.get("BALANCE").getAsDouble());
      if (QualityEconomy.getQualityConfig().COMMANDS_PAY)
        account.setPayable(accountJson.get("PAYABLE").getAsBoolean());
      if (QualityEconomy.getQualityConfig().COMMANDS_REQUEST)
        account.setPayable(accountJson.get("REQUESTABLE").getAsBoolean());
      if (QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES)
        for (String currency : getCurrencies())
          account.setCustomBalance(currency, accountJson.get(currency).getAsDouble());
      accounts.put(uuid, account);
    }
    return accounts;
  }
  
  @Override
  public boolean addCurrency(@NotNull String currency) {
    JsonArray currencies = json.getAsJsonArray("custom-currencies");
    if (currencies == null || currencies.isEmpty())
      currencies = new JsonArray();
    currencies.add(currency);
    json.add("custom-currencies", currencies);
    for (Map.Entry<String, JsonElement> entry : getEntrySet()) {
      JsonObject accountJson = entry.getValue().getAsJsonObject();
      accountJson.addProperty(currency, 0);
    }
    save();
    super.currencies.add(currency);
    return true;
  }
  
  @Override
  public boolean removeCurrency(@NotNull String currency) {
    JsonArray currencies = json.getAsJsonArray("custom-currencies");
    currencies.remove(new Gson().toJsonTree(currency));
    json.add("custom-currencies", currencies);
    for (Map.Entry<String, JsonElement> entry : getEntrySet()) {
      JsonObject accountJson = entry.getValue().getAsJsonObject();
      accountJson.remove(currency);
    }
    save();
    super.currencies.remove(currency);
    return true;
  }
  
}
