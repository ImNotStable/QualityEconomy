package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

@Getter
public class CustomEconomyCommand implements Command {
  
  private final CommandTree command = new CommandTree("customeconomy")
    .withAliases("ceconomy", "customeco", "ceco")
    .withPermission("qualityeconomy.customeconomy")
    .then(new StringArgument("currency")
      .replaceSuggestions(ArgumentSuggestions.strings(info -> StorageManager.getActiveStorageType().getCurrencies().toArray(new String[0])))
      .then(new OfflinePlayerArgument("target")
        .replaceSuggestions(CommandUtils.getOnlinePlayerSuggestion())
        .then(new LiteralArgument("reset").executes(this::resetBalance))
        .then(new LiteralArgument("set").then(new StringArgument("amount").executes(this::setBalance)))
        .then(new LiteralArgument("add").then(new StringArgument("amount").executes(this::addBalance)))
        .then(new LiteralArgument("remove").then(new StringArgument("amount").executes(this::removeBalance)))));
  private boolean isRegistered = false;
  
  public void register() {
    if (isRegistered || !Configuration.isCommandEnabled("customeconomy") || StorageManager.getActiveStorageType().getCurrencies().isEmpty())
      return;
    command.register();
    isRegistered = true;
  }
  
  public void unregister() {
    if (!isRegistered)
      return;
    CommandAPI.unregister(command.getName(), true);
    isRegistered = false;
  }
  
  private void resetBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.requirement(StorageManager.getActiveStorageType().getCurrencies().contains(currency), MessageType.CURRENCY_NOT_FOUND, sender))
      return;
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    QualityEconomyAPI.setCustomBalance(target.getUniqueId(), currency, 0);
    Messages.sendParsedMessage(sender, MessageType.ECONOMY_RESET,
      target.getName());
  }
  
  private void setBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.requirement(StorageManager.getActiveStorageType().getCurrencies().contains(currency), MessageType.CURRENCY_NOT_FOUND, sender))
      return;
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    double balance;
    try {
      balance = Number.roundObj(CommandUtils.parseNumber((String) args.get("amount")));
    } catch (NumberFormatException exception) {
      Messages.sendParsedMessage(sender, MessageType.INVALID_NUMBER);
      return;
    }
    QualityEconomyAPI.setCustomBalance(target.getUniqueId(), currency, balance);
    Messages.sendParsedMessage(sender, MessageType.ECONOMY_SET,
      Number.format(balance, Number.FormatType.COMMAS), target.getName());
  }
  
  private void addBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.requirement(StorageManager.getActiveStorageType().getCurrencies().contains(currency), MessageType.CURRENCY_NOT_FOUND, sender))
      return;
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    double balance;
    try {
      balance = Number.roundObj(CommandUtils.parseNumber((String) args.get("amount")));
    } catch (NumberFormatException exception) {
      Messages.sendParsedMessage(sender, MessageType.INVALID_NUMBER);
      return;
    }
    QualityEconomyAPI.addCustomBalance(target.getUniqueId(), currency, balance);
    Messages.sendParsedMessage(sender, MessageType.ECONOMY_ADD,
      Number.format(balance, Number.FormatType.COMMAS), target.getName());
  }
  
  private void removeBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.requirement(StorageManager.getActiveStorageType().getCurrencies().contains(currency), MessageType.CURRENCY_NOT_FOUND, sender))
      return;
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    double balance;
    try {
      balance = Number.roundObj(CommandUtils.parseNumber((String) args.get("amount")));
    } catch (NumberFormatException exception) {
      Messages.sendParsedMessage(sender, MessageType.INVALID_NUMBER);
      return;
    }
    QualityEconomyAPI.removeCustomBalance(target.getUniqueId(), currency, balance);
    Messages.sendParsedMessage(sender, MessageType.ECONOMY_REMOVE,
      Number.format(balance, Number.FormatType.COMMAS), target.getName());
  }
  
}
