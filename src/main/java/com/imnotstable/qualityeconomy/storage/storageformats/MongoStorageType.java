package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.Misc;
import com.imnotstable.qualityeconomy.util.storage.EasyMongo;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MongoStorageType extends EasyMongo implements StorageType {
  
  @Override
  public synchronized boolean initStorageProcesses() {
    if (client != null)
      return false;
    MongoClientSettings settings = MongoClientSettings.builder()
      .applyConnectionString(new ConnectionString(getConnectionString()))
      .uuidRepresentation(UuidRepresentation.STANDARD)
      .build();
    client = MongoClients.create(settings);
    data = client.getDatabase("DATA");
    playerDataCollection = data.getCollection("PLAYERDATA");
    Misc.runAsync(() -> {
      toggleCurrencyCollection();
      togglePayable();
      toggleRequestable();
    });
    return true;
  }
  
  @Override
  public synchronized void endStorageProcesses() {
    if (client == null)
      return;
    client.close();
    client = null;
  }
  
  @Override
  public synchronized void wipeDatabase() {
    data.drop();
    endStorageProcesses();
    initStorageProcesses();
  }
  
  @Override
  public synchronized void createAccount(@NotNull Account account) {
    Document document = createDocument(account);
    if (!playerDataCollection.insertOne(document).wasAcknowledged())
      new Debug.QualityError("Failed to create account (" + account.getUniqueId() + ")").log();
  }
  
  @Override
  public synchronized void createAccounts(@NotNull Collection<Account> accounts) {
    List<Document> documents = new ArrayList<>();
    accounts.forEach(account -> documents.add(createDocument(account)));
    if (!playerDataCollection.insertMany(documents).wasAcknowledged())
      new Debug.QualityError("Failed to create accounts").log();
  }
  
  @Override
  public synchronized void saveAllAccounts() {
    boolean isPayEnabled = Configuration.isCommandEnabled("pay");
    boolean isRequestEnabled = Configuration.isCommandEnabled("request");
    boolean isCustomCurrenciesEnabled = Configuration.isCustomCurrenciesEnabled();
    String[] currencies = isCustomCurrenciesEnabled ? getCurrencies().toArray(new String[0]) : null;
    
    List<WriteModel<Document>> updates = new ArrayList<>(AccountManager.getAllAccounts().size());
    
    for (Account account : AccountManager.getAllAccounts()) {
      if (!account.requiresUpdate())
        continue;
      account.update();
      Document update = new Document("$set",
        new Document("USERNAME", account.getUsername())
          .append("BALANCE", account.getBalance())
      );
      
      if (isPayEnabled) update.append("$set", new Document("PAYABLE", account.isPayable()));
      if (isRequestEnabled) update.append("$set", new Document("REQUESTABLE", account.isRequestable()));
      if (isCustomCurrenciesEnabled)
        for (String currency : currencies)
          update.append("$set", new Document(currency, account.getCustomBalance(currency)));
      
      updates.add(new UpdateOneModel<>(new Document("UUID", account.getUniqueId()), update));
    }
    
    if (!updates.isEmpty()) {
      playerDataCollection.bulkWrite(updates);
    }
  }
  
  
  @Override
  public synchronized @NotNull Map<UUID, Account> getAllAccounts() {
    Map<UUID, Account> accounts = new HashMap<>();
    playerDataCollection.find().forEach(document -> {
      UUID uuid = document.get("UUID", UUID.class);
      Account account = new Account(uuid).setUsername(document.getString("USERNAME")).setBalance(document.getDouble("BALANCE"));
      if (Configuration.isCommandEnabled("pay"))
        account.setPayable(document.getBoolean("PAYABLE"));
      if (Configuration.isCommandEnabled("request"))
        account.setRequestable(document.getBoolean("REQUESTABLE"));
      if (Configuration.isCustomCurrenciesEnabled())
        for (String currency : currencies)
          account.setCustomBalance(currency, document.getDouble(currency));
      accounts.put(uuid, account);
    });
    return accounts;
  }
  
  @Override
  public boolean addCurrency(@NotNull String currency) {
    if (currencyCollection == null) {
      new Debug.QualityError("currencies collection database not found.").log();
      return false;
    }
    currencyCollection.insertOne(new Document("CURRENCY", currency));
    super.currencies.add(currency);
    return true;
  }
  
  @Override
  public boolean removeCurrency(@NotNull String currency) {
    if (currencyCollection == null) {
      new Debug.QualityError("currencies collection not found.").log();
      return false;
    }
    wipeEntry(currency);
    currencyCollection.findOneAndDelete(new Document("CURRENCY", currency));
    super.currencies.remove(currency);
    return true;
  }
  
  private void toggleCurrencyCollection() {
    boolean collectionExists = false;
    List<String> currencies = new ArrayList<>();
    for (String collectionName : data.listCollectionNames())
      if (collectionName.equals("CURRENCIES")) {
        collectionExists = true;
        MongoCollection<Document> collection = data.getCollection("CURRENCIES");
        collection.find().forEach(document -> currencies.add((String) document.get("CURRENCY")));
        break;
      }
    if (Configuration.isCustomCurrenciesEnabled() && !collectionExists) {
      super.currencies.addAll(currencies);
      data.createCollection("CURRENCIES");
      currencyCollection = data.getCollection("CURRENCIES");
    } else if (!Configuration.isCustomCurrenciesEnabled() && collectionExists) {
      currencies.forEach(super::wipeEntry);
      data.getCollection("CURRENCIES").drop();
      currencyCollection = null;
    }
  }
  
  private void togglePayable() {
    if (Configuration.isCommandEnabled("pay"))
      setDefaultValue("PAYABLE", true);
    else
      wipeEntry("PAYABLE");
  }
  
  private void toggleRequestable() {
    if (Configuration.isCommandEnabled("request"))
      setDefaultValue("REQUESTABLE", false);
    else
      wipeEntry("REQUESTABLE");
  }
  
  
}
