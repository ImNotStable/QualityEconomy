package com.imnotstable.qualityeconomy.util.storage;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EasyMongo extends EasyCurrencies {
  
  protected MongoClient client = null;
  protected MongoDatabase data = null;
  protected MongoCollection<Document> playerDataCollection = null;
  protected MongoCollection<Document> currencyCollection = null;
  
  protected String getConnectionString() {
    Map<String, String> databaseInfo = QualityEconomy.getQualityConfig().DATABASE_INFORMATION;
    String database = databaseInfo.getOrDefault("database", "qualityeconomy");
    String address = databaseInfo.getOrDefault("address", "localhost");
    String port = databaseInfo.getOrDefault("port", "27017");
    String username = databaseInfo.getOrDefault("username", "");
    String password = databaseInfo.getOrDefault("password", "");
    StringBuilder connectionStringBuilder = new StringBuilder("mongodb://");
    if (!username.isEmpty() && !password.isEmpty())
      connectionStringBuilder.append(username).append(":").append(password).append("@");
    connectionStringBuilder.append(address).append(":").append(port);
    if (!database.isEmpty())
      connectionStringBuilder.append("/").append(database);
    return connectionStringBuilder.toString();
  }
  
  protected Document createDocument(Account account) {
    Document document = new Document("UUID", account.getUniqueId());
    document.put("USERNAME", account.getUsername());
    document.put("BALANCE", account.getBalance());
    if (QualityEconomy.getQualityConfig().COMMANDS_PAY)
      document.put("PAYABLE", account.isPayable());
    if (QualityEconomy.getQualityConfig().COMMANDS_REQUEST)
      document.put("REQUESTABLE", account.isRequestable());
    if (QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES)
      for (String currency : currencies)
        document.put(currency, account.getCustomBalance(currency));
    return document;
  }
  
  protected void setDefaultValue(String entry, boolean value) {
    List<WriteModel<Document>> updates = new ArrayList<>();
    for (Document document : playerDataCollection.find().projection(Projections.include("UUID"))) {
      UUID uuid = document.get("UUID", UUID.class);
      if (uuid != null && !document.containsKey(entry)) {
        Document query = new Document("UUID", uuid);
        Document update = new Document("$set", new Document(entry, value));
        updates.add(new UpdateOneModel<>(query, update));
      }
    }
    if (!updates.isEmpty())
      playerDataCollection.bulkWrite(updates);
  }
  
  protected void wipeEntry(String entry) {
    List<WriteModel<Document>> updates = new ArrayList<>();
    for (Document document : playerDataCollection.find().projection(Projections.include("UUID"))) {
      UUID uuid = document.get("UUID", UUID.class);
      if (uuid != null) {
        Document query = new Document("UUID", uuid);
        Document update = new Document("$unset", new Document(entry, ""));
        updates.add(new UpdateOneModel<>(query, update));
      }
    }
    if (!updates.isEmpty())
      playerDataCollection.bulkWrite(updates);
  }
  
}
