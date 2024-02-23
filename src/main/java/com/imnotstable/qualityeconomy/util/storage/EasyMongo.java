package com.imnotstable.qualityeconomy.util.storage;

import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.util.Logger;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EasyMongo extends EasyCurrencies {
  
  protected MongoClient client = null;
  protected MongoDatabase data = null;
  protected MongoCollection<Document> playerdata = null;
  protected MongoCollection<Document> customCurrencies = null;
  
  protected String getConnectionString() {
    String database = Configuration.getDatabaseInfo(0, "qualityeconomy");
    String address = Configuration.getDatabaseInfo(1, "localhost");
    String port = Configuration.getDatabaseInfo(2, "27017");
    String username = Configuration.getDatabaseInfo(3, "");
    String password = Configuration.getDatabaseInfo(4, "");
    StringBuilder connectionStringBuilder = new StringBuilder("mongodb://");
    if (!username.isEmpty() && !password.isEmpty())
      connectionStringBuilder.append(username).append(":").append(password).append("@");
    connectionStringBuilder.append(address).append(":").append(port);
    if (!database.isEmpty())
      connectionStringBuilder.append("/").append(database);
    Logger.log(connectionStringBuilder.toString());
    return connectionStringBuilder.toString();
  }
  
  protected Document createDocument(Account account) {
    Document document = new Document("UUID", account.getUniqueId());
    document.put("USERNAME", account.getUsername());
    document.put("BALANCE", account.getBalance());
    if (Configuration.isCommandEnabled("pay"))
      document.put("PAYABLE", account.isPayable());
    if (Configuration.isCommandEnabled("request"))
      document.put("REQUESTABLE", account.isRequestable());
    if (Configuration.isCustomCurrenciesEnabled())
      for (String currency : currencies)
        document.put(currency, account.getCustomBalance(currency));
    return document;
  }
  
  protected void setDefaultValue(String entry, boolean value) {
    List<WriteModel<Document>> updates = new ArrayList<>();
    for (Document document : playerdata.find().projection(Projections.include("UUID"))) {
      UUID uuid = document.get("UUID", UUID.class);
      if (uuid != null && !document.containsKey(entry)) {
        Document query = new Document("UUID", uuid);
        Document update = new Document("$set", new Document(entry, value));
        updates.add(new UpdateOneModel<>(query, update));
      }
    }
    if (!updates.isEmpty())
      playerdata.bulkWrite(updates);
  }
  
  protected void wipeEntry(String entry) {
    List<WriteModel<Document>> updates = new ArrayList<>();
    for (Document document : playerdata.find().projection(Projections.include("UUID"))) {
      UUID uuid = document.get("UUID", UUID.class);
      if (uuid != null) {
        Document query = new Document("UUID", uuid);
        Document update = new Document("$unset", new Document(entry, ""));
        updates.add(new UpdateOneModel<>(query, update));
      }
    }
    if (!updates.isEmpty())
      playerdata.bulkWrite(updates);
  }
  
}
