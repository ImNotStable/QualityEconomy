package com.imnotstable.qualityeconomy.util.storage;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.util.Debug;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class EasyYaml extends EasyCurrencies {
  
  protected final File file = new File(QualityEconomy.getInstance().getDataFolder(), "playerdata.yml");
  protected YamlConfiguration yaml;
  
  protected void setAccount(Account account) {
    UUID uuid = account.getUniqueId();
    yaml.set(uuid + ".USERNAME", account.getUsername());
    yaml.set(uuid + ".BALANCE", account.getBalance());
    if (Configuration.isCommandEnabled("pay"))
      yaml.set(uuid + ".PAYABLE", account.isPayable());
    if (Configuration.isCommandEnabled("request"))
      yaml.set(uuid + ".REQUESTABLE", account.isRequestable());
    if (Configuration.areCustomCurrenciesEnabled())
      account.getCustomBalances().forEach((currency, balance) -> yaml.set(uuid + "." + currency, balance));
  }
  
  protected void save() {
    try {
      yaml.save(file);
    } catch (IOException exception) {
      new Debug.QualityError("Failed to save playerdata.yml", exception).log();
    }
  }
  
}
