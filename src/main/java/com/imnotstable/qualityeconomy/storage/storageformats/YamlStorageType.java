package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.storage.EasyYaml;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class YamlStorageType extends EasyYaml implements StorageType {
  
  @Override
  public boolean initStorageProcesses() {
    if (yaml != null) return false;
    try {
      if (!file.exists()) {
        if (!file.createNewFile())
          return false;
        yaml = new YamlConfiguration();
      } else
        yaml = YamlConfiguration.loadConfiguration(file);
    } catch (IOException exception) {
      new Debug.QualityError("Failed to initiate storage processes", exception).log();
      return false;
    }
    return true;
  }
  
  @Override
  public void endStorageProcesses() {
    if (yaml == null) return;
    if (file.exists())
      save();
    yaml = null;
  }
  
  @Override
  public void wipeDatabase() {
    file.delete();
    endStorageProcesses();
    initStorageProcesses();
  }
  
  @Override
  public void createAccount(Account account) {
    setAccount(account);
    save();
  }
  
  @Override
  public void createAccounts(Collection<Account> accounts) {
    accounts.forEach(this::createAccount);
    save();
  }
  
  @Override
  public void updateAccounts(Collection<Account> accounts) {
    accounts.forEach(this::createAccount);
    save();
  }
  
  @Override
  public Map<UUID, Account> getAllAccounts() {
    Map<UUID, Account> accounts = new HashMap<>();
    yaml.getKeys(false).forEach(uuid -> {
      Account account = new Account(UUID.fromString(uuid));
      account.setUsername(yaml.getString(uuid + ".USERNAME"));
      account.setBalance(yaml.getDouble(uuid + ".BALANCE"));
      if (Configuration.isCommandEnabled("pay"))
        account.setPayable(yaml.getBoolean(uuid + ".PAYABLE"));
      if (Configuration.isCommandEnabled("request"))
        account.setRequestable(yaml.getBoolean(uuid + ".REQUESTABLE"));
      if (Configuration.areCustomCurrenciesEnabled())
        for (String currency : getCurrencies())
          account.setCustomBalance(currency, yaml.getDouble(uuid + "." + currency));
      accounts.put(account.getUUID(), account);
    });
    return accounts;
  }
  
  @Override
  public void addCurrency(String currency) {
    currency = super.addCurrencyAttempt(currency);
    if (currency == null)
      return;
    List<String> currencies = yaml.getStringList("custom-currencies");
    currencies.add(currency);
    yaml.set("custom-currencies", currencies);
    super.addCurrencySuccess(currency);
    save();
  }
  
  @Override
  public void removeCurrency(String currency) {
    currency = super.removeCurrencyAttempt(currency);
    if (currency == null)
      return;
    List<String> currencies = yaml.getStringList("custom-currencies");
    currencies.remove(currency);
    yaml.set("custom-currencies", currencies);
    super.removeCurrencySuccess(currency);
    save();
  }
  
}
