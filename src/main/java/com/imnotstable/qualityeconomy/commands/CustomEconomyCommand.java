package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.economy.EconomicTransaction;
import com.imnotstable.qualityeconomy.economy.EconomicTransactionType;
import com.imnotstable.qualityeconomy.economy.EconomyPlayer;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

@Getter
public class CustomEconomyCommand extends BaseCommand {
  
  private final CommandTree command = new CommandTree("customeconomy")
    .withAliases("ceconomy", "customeco", "ceco")
    .withPermission("qualityeconomy.customeconomy")
    .then(new StringArgument("currency")
      .replaceSuggestions(ArgumentSuggestions.strings(info -> StorageManager.getActiveStorageType().getCurrencies().toArray(new String[0])))
      .then(new OfflinePlayerArgument("target")
        .replaceSuggestions(CommandUtils.getOnlinePlayerSuggestion())
        .then(new LiteralArgument("reset").executes(this::resetBalance))
        .then(new LiteralArgument("set").then(CommandUtils.CurrencyAmountArgument().executes(this::setBalance)))
        .then(new LiteralArgument("add").then(CommandUtils.CurrencyAmountArgument().executes(this::addBalance)))
        .then(new LiteralArgument("remove").then(CommandUtils.CurrencyAmountArgument().executes(this::removeBalance)))));
  
  @SuppressWarnings("SimplifiableConditionalExpression")
  public void register() {
    super.register(command, Configuration.isCustomCurrenciesEnabled() ? !StorageManager.getActiveStorageType().getCurrencies().isEmpty() : false);
  }
  
  public void unregister() {
    super.unregister(command);
  }
  
  @SneakyThrows
  private void resetBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.requirement(StorageManager.getActiveStorageType().getCurrencies().contains(currency), MessageType.CURRENCY_NOT_FOUND, sender))
      return;
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    EconomicTransaction.startNewTransaction(EconomicTransactionType.CUSTOM_BALANCE_RESET, sender, currency, 0, EconomyPlayer.of(target)).execute();
  }
  
  @SneakyThrows
  private void setBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.requirement(StorageManager.getActiveStorageType().getCurrencies().contains(currency), MessageType.CURRENCY_NOT_FOUND, sender))
      return;
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    double balance = (double) args.get("amount");
    EconomicTransaction.startNewTransaction(EconomicTransactionType.CUSTOM_BALANCE_SET, sender, currency, balance, EconomyPlayer.of(target)).execute();
  }
  
  @SneakyThrows
  private void addBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.requirement(StorageManager.getActiveStorageType().getCurrencies().contains(currency), MessageType.CURRENCY_NOT_FOUND, sender))
      return;
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    double balance = (double) args.get("amount");
    EconomicTransaction.startNewTransaction(EconomicTransactionType.CUSTOM_BALANCE_ADD, sender, currency, balance, EconomyPlayer.of(target)).execute();
  }
  
  @SneakyThrows
  private void removeBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.requirement(StorageManager.getActiveStorageType().getCurrencies().contains(currency), MessageType.CURRENCY_NOT_FOUND, sender))
      return;
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    double balance = (double) args.get("amount");
    EconomicTransaction.startNewTransaction(EconomicTransactionType.CUSTOM_BALANCE_REMOVE, sender, currency, balance, EconomyPlayer.of(target)).execute();
  }
  
}
