package com.imnotstable.qualityeconomy.config;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.commands.CurrencyCommand;
import com.imnotstable.qualityeconomy.economy.Account;
import com.imnotstable.qualityeconomy.economy.Currency;
import com.imnotstable.qualityeconomy.util.QualityException;
import com.imnotstable.qualityeconomy.util.debug.Logger;
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
  private final Map<String, CurrencyCommand> currencyCommands = new HashMap<>();
  
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
      try {
        currencies.put(currencyName, Currency.of(section.getConfigurationSection(currencyName)));
      } catch (QualityException exception) {
        Logger.logError("Failed to load currency " + currencyName, exception, false);
      }
    }
    loadCommands();
  }
  
  public Account getLeaderboardAccount(Currency currency, int position) {
    return currencyCommands.get(currency.getName()).getLeaderboardAccount(position);
  }
  
  private void loadCommands() {
    currencies.values().forEach(currency -> currencyCommands.put(currency.getName(), new CurrencyCommand(currency)));
    currencyCommands.values().forEach(CurrencyCommand::register);
  }
  
  private void unloadCommands() {
    currencyCommands.values().forEach(CurrencyCommand::unregister);
    currencyCommands.clear();
  }
  
  public Set<Currency> getCurrencies() {
    return new HashSet<>(currencies.values());
  }
  
  public @NotNull Optional<@Nullable Currency> getCurrency(@NotNull String name) {
    return Optional.ofNullable(currencies.get(name));
  }
  
  public double getStartingBalance(@NotNull String name) {
    return currencies.get(name).getStartingBalance();
  }
  
}
