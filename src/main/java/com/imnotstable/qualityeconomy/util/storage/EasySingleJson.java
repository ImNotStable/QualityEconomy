package com.imnotstable.qualityeconomy.util.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.util.Debug;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class EasySingleJson extends EasyCurrencies {
  
  protected final File file = new File(QualityEconomy.getInstance().getDataFolder(), "playerdata.json");
  protected JsonObject json;
  
  protected JsonObject serialize(Account account) {
    JsonObject json = new JsonObject();
    json.addProperty("USERNAME", account.getUsername());
    json.addProperty("BALANCE", account.getBalance());
    if (Configuration.isCommandEnabled("pay"))
      json.addProperty("PAYABLE", account.isPayable());
    if (Configuration.isCommandEnabled("request"))
      json.addProperty("REQUESTABLE", account.isRequestable());
    if (Configuration.areCustomCurrenciesEnabled())
      account.getCustomBalances().forEach(json::addProperty);
    return json;
  }
  
  protected void save() {
    Gson gson = new GsonBuilder().create();
    try (FileWriter fileWriter = new FileWriter(file)) {
      fileWriter.write(gson.toJson(json));
    } catch (IOException exception) {
      new Debug.QualityError("Failed to save playerdata.json", exception).log();
    }
  }
  
}
