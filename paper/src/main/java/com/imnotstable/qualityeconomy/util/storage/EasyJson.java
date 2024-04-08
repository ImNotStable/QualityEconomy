package com.imnotstable.qualityeconomy.util.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.util.debug.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EasyJson extends EasyCurrencies {
  
  protected final File file = new File(QualityEconomy.getInstance().getDataFolder(), "playerdata.json");
  protected JsonObject json;
  
  protected JsonObject serialize(Account account) {
    JsonObject json = new JsonObject();
    json.addProperty("USERNAME", account.getUsername());
    json.addProperty("BALANCE", account.getBalance());
    if (QualityEconomy.getQualityConfig().COMMANDS_PAY)
      json.addProperty("PAYABLE", account.isPayable());
    if (QualityEconomy.getQualityConfig().COMMANDS_REQUEST)
      json.addProperty("REQUESTABLE", account.isRequestable());
    if (QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES)
      account.getCustomBalances().forEach(json::addProperty);
    return json;
  }
  
  protected void toggleCustomCurrencies() {
    if (QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES) {
      if (!json.has("custom-currencies"))
        json.add("custom-currencies", new JsonArray());
      json.getAsJsonArray("custom-currencies").forEach(currency -> currencies.add(currency.getAsString()));
    } else {
      json.remove("custom-currencies");
    }
  }
  
  
  protected Set<Map.Entry<String, JsonElement>> getEntrySet() {
    Set<Map.Entry<String, JsonElement>> entrySet = new HashSet<>(json.entrySet());
    entrySet.removeIf(entry -> entry.getKey().equals("custom-currencies"));
    return entrySet;
  }
  
  protected void save() {
    Gson gson = new GsonBuilder().create();
    try (FileWriter fileWriter = new FileWriter(file)) {
      fileWriter.write(gson.toJson(json));
    } catch (IOException exception) {
      Logger.logError("Failed to save playerdata.json", exception);
    }
  }
  
}
