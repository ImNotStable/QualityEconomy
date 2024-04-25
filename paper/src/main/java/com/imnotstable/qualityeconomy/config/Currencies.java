package com.imnotstable.qualityeconomy.config;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.commands.CurrencyCommand;
import com.imnotstable.qualityeconomy.economy.Currency;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class Currencies extends BaseConfig {
  
  private final Map<String, Currency> currencies = new HashMap<>();
  private final Set<CurrencyCommand> currencyCommands = new HashSet<>();
  
  public Currencies(@NotNull QualityEconomy plugin) {
    super(plugin, "currencies.yml");
    load();
  }
  
  public void load() {
    super.load(false);
    currencies.clear();
    unloadCommands();
    ConfigurationSection section = config.getConfigurationSection("currencies");
    for (String currencyName : section.getKeys(false)) {
      ConfigurationSection currencySection = section.getConfigurationSection(currencyName);
      if (currencySection == null)
        continue;
      double startingBalance = currencySection.getDouble("starting-balance");
      String[] viewCommands = currencySection.getStringList("view-commands").toArray(new String[0]);
      String[] adminCommands = currencySection.getStringList("admin-commands").toArray(new String[0]);
      String[] transferCommands = currencySection.getStringList("transfer-commands").toArray(new String[0]);
      String singular = currencySection.getString("singular");
      String plural = currencySection.getString("plural");
      currencies.put(currencyName, Currency.of(currencyName, startingBalance, viewCommands, adminCommands, transferCommands, singular, plural));
    }
    loadCommands();
  }
  
  private void loadCommands() {
    currencies.values().forEach(currency -> currencyCommands.add(new CurrencyCommand(currency)));
    currencyCommands.forEach(CurrencyCommand::register);
  }
  
  private void unloadCommands() {
    currencyCommands.forEach(CurrencyCommand::unregister);
    currencyCommands.clear();
  }
  
  public Set<Currency> getCurrencies() {
    return new HashSet<>(currencies.values());
  }
  
  public @NotNull Optional<@Nullable Currency> getCurrency(@NotNull String name) {
    return Optional.ofNullable(currencies.get(name));
  }
  
  public double getDefaultBalance(@NotNull String name) {
    return currencies.get(name).getDefaultBalance();
  }
  
}
