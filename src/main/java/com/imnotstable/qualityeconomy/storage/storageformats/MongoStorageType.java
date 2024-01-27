package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.storage.EasyMongo;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bukkit.scheduler.BukkitRunnable;

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
    playerdata = data.getCollection("PLAYERDATA");
    new BukkitRunnable() {
      @Override
      public void run() {
        toggleCurrencyCollection();
        togglePayable();
        toggleRequestable();
      }
    }.runTaskAsynchronously(QualityEconomy.getInstance());
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
  public synchronized void createAccount(Account account) {
    Document document = createDocument(account);
    if (!playerdata.insertOne(document).wasAcknowledged())
      new Debug.QualityError("Failed to create account (" + account.getUUID() + ")").log();
  }
  
  @Override
  public synchronized void createAccounts(Collection<Account> accounts) {
    List<Document> documents = new ArrayList<>();
    accounts.forEach(account -> documents.add(createDocument(account)));
    if (!playerdata.insertMany(documents).wasAcknowledged())
      new Debug.QualityError("Failed to create accounts").log();
  }
  
  @Override
  public synchronized void updateAccounts(Collection<Account> accounts) {
    List<WriteModel<Document>> updates = new ArrayList<>();
    
    for (Account account : accounts) {
      Document query = new Document("UUID", account.getUUID());
      Document update = new Document();
      update.append("$set", new Document("USERNAME", account.getUsername()));
      update.append("$set", new Document("BALANCE", account.getBalance()));
      if (Configuration.isCommandEnabled("pay"))
        update.get("$set", Document.class).append("PAYABLE", account.isPayable());
      if (Configuration.isCommandEnabled("request"))
        update.get("$set", Document.class).append("REQUESTABLE", account.isRequestable());
      if (Configuration.areCustomCurrenciesEnabled())
        for (String currency : currencies)
          update.get("$set", Document.class).append(currency, account.getCustomBalance(currency));
      updates.add(new UpdateOneModel<>(query, update));
    }
    
    if (!updates.isEmpty())
      playerdata.bulkWrite(updates);
  }

  
  @Override
  public synchronized Map<UUID, Account> getAllAccounts() {
    Map<UUID, Account> accounts = new HashMap<>();
    playerdata.find().forEach(document -> {
      UUID uuid = document.get("UUID", UUID.class);
      Account account = new Account(uuid).setUsername(document.getString("USERNAME")).setBalance(document.getDouble("BALANCE"));
      if (Configuration.isCommandEnabled("pay"))
        account.setPayable(document.getBoolean("PAYABLE"));
      if (Configuration.isCommandEnabled("request"))
        account.setRequestable(document.getBoolean("REQUESTABLE"));
      if (Configuration.areCustomCurrenciesEnabled())
        for (String currency : currencies)
          account.setCustomBalance(currency, document.getDouble(currency));
      accounts.put(uuid, account);
    });
    return accounts;
  }
  
  @Override
  public void addCurrency(String currency) {
    currency = super.addCurrencyAttempt(currency);
    if (currency == null)
      return;
    if (customCurrencies == null) {
      new Debug.QualityError("currencies database not found.").log();
      return;
    }
    customCurrencies.insertOne(new Document("CURRENCY", currency));
  }
  
  @Override
  public void removeCurrency(String currency) {
    currency = super.removeCurrencyAttempt(currency);
    if (currency == null)
      return;
    if (customCurrencies == null) {
      new Debug.QualityError("currencies database not found.").log();
      return;
    }
    wipeEntry(currency);
    customCurrencies.findOneAndDelete(new Document("CURRENCY", currency));
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
    if (Configuration.areCustomCurrenciesEnabled() && !collectionExists) {
      super.currencies.addAll(currencies);
      data.createCollection("CURRENCIES");
      customCurrencies = data.getCollection("CURRENCIES");
    } else if (!Configuration.areCustomCurrenciesEnabled() && collectionExists) {
      currencies.forEach(super::wipeEntry);
      data.getCollection("CURRENCIES").drop();
      customCurrencies = null;
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
      wipeEntry("REQUETABLE");
  }

  
}
