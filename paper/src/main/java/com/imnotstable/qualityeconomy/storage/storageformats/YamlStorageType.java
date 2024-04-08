package com.imnotstable.qualityeconomy.storage.storageformats;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import com.imnotstable.qualityeconomy.util.storage.EasyYaml;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

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
      } else {
        yaml = YamlConfiguration.loadConfiguration(file);
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
  public void createAccount(@NotNull Account account) {
    setAccount(account);
    save();
  }
  
  @Override
  public void createAccounts(@NotNull Collection<Account> accounts) {
    accounts.forEach(this::setAccount);
    save();
  }
  
  @Override
  public void saveAllAccounts() {
    AccountManager.getAllAccounts().stream()
      .filter(Account::requiresUpdate)
      .forEach(account -> setAccount(account.update()));
    save();
  }
  
  @Override
  public @NotNull Map<UUID, Account> getAllAccounts() {
    Map<UUID, Account> accounts = new HashMap<>();
    for (String uuid : getAllUniqueIds()) {
      Account account = new Account(UUID.fromString(uuid));
      account.setUsername(yaml.getString(uuid + ".USERNAME"));
      account.setBalance(yaml.getDouble(uuid + ".BALANCE"));
      if (QualityEconomy.getQualityConfig().COMMANDS_PAY)
        account.setPayable(yaml.getBoolean(uuid + ".PAYABLE"));
      if (QualityEconomy.getQualityConfig().COMMANDS_REQUEST)
        account.setRequestable(yaml.getBoolean(uuid + ".REQUESTABLE"));
      if (QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES)
        for (String currency : getCurrencies())
          account.setCustomBalance(currency, yaml.getDouble(uuid + "." + currency));
      accounts.put(account.getUniqueId(), account);
    }
    return accounts;
  }
  
  @Override
  public boolean addCurrency(@NotNull String currency) {
    List<String> currencies = yaml.getStringList("custom-currencies");
    yaml.set("custom-currencies", currencies);
    for (String uuid : getAllUniqueIds())
      yaml.set(uuid + "." + currency, 0);
    save();
    super.currencies.add(currency);
    return true;
  }
  
  @Override
  public boolean removeCurrency(@NotNull String currency) {
    List<String> currencies = yaml.getStringList("custom-currencies");
    yaml.set("custom-currencies", currencies);
    for (String uuid : getAllUniqueIds())
      yaml.set(uuid + "." + currency, null);
    save();
    super.currencies.remove(currency);
    return true;
  }
  
}
