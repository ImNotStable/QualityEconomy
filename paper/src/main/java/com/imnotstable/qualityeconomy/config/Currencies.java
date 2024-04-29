package com.imnotstable.qualityeconomy.config;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.commands.CurrencyCommand;
import com.imnotstable.qualityeconomy.economy.Account;
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
      ConfigurationSection currencySection = section.getConfigurationSection(currencyName);
      if (currencySection == null)
        continue;
      double startingBalance = currencySection.getDouble("starting-balance", 0.0);
      int decimalPlaces = currencySection.getInt("decimal-places", 0);
      String[] viewCommands = currencySection.getStringList("view-commands").toArray(new String[0]);
      String[] adminCommands = currencySection.getStringList("admin-commands").toArray(new String[0]);
      String[] transferCommands = currencySection.getStringList("transfer-commands").toArray(new String[0]);
      String[] leaderboardCommands = currencySection.getStringList("leaderboard-commands").toArray(new String[0]);
      String symbol = currencySection.getString("symbol", "");
      String symbolPosition = currencySection.getString("symbol-position", "before");
      String singular = currencySection.getString("singular", "");
      String plural = currencySection.getString("plural", "");
      MessageType[] messageTypes = MessageType.values();
      Map<MessageType, String> messages = new HashMap<>();
      for (MessageType messageType : messageTypes) {
        if (!messageType.isCurrencyDependent())
          continue;
        String message = currencySection.getString("messages." + messageType.getKey());
        if (message != null)
          messages.put(messageType, message);
      }
      currencies.put(currencyName, Currency.of(currencyName, startingBalance, decimalPlaces, viewCommands, adminCommands, transferCommands, leaderboardCommands, symbol, symbolPosition, singular, plural, messages));
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
  
  public double getDefaultBalance(@NotNull String name) {
    return currencies.get(name).getDefaultBalance();
  }
  
}
