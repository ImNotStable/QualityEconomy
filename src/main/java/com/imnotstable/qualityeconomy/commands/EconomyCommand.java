package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class EconomyCommand extends BaseCommand {
  
  private final CommandTree command = new CommandTree("economy")
    .withPermission("qualityeconomy.economy")
    .withAliases("eco")
    .then(new OfflinePlayerArgument("target")
      .replaceSuggestions(CommandUtils.getOnlinePlayerSuggestion())
      .then(new LiteralArgument("reset").executes(this::resetBalance))
      .then(new LiteralArgument("set").then(new StringArgument("amount").executes(this::setBalance)))
      .then(new LiteralArgument("add").then(new StringArgument("amount").executes(this::addBalance)))
      .then(new LiteralArgument("remove").then(new StringArgument("amount").executes(this::removeBalance))));
  
  public void register() {
    super.register(command);
  }
  
  public void unregister() {
    super.unregister(command);
  }
  
  private void resetBalance(CommandSender sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    QualityEconomyAPI.setBalance(target.getUniqueId(), 0);
    Messages.sendParsedMessage(sender, MessageType.ECONOMY_RESET,
      target.getName());
  }
  
  private void setBalance(CommandSender sender, CommandArguments args) {
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
    QualityEconomyAPI.setBalance(target.getUniqueId(), balance);
    Messages.sendParsedMessage(sender, MessageType.ECONOMY_SET,
      Number.format(balance, Number.FormatType.COMMAS), target.getName());
  }
  
  private void addBalance(CommandSender sender, CommandArguments args) {
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
    QualityEconomyAPI.addBalance(target.getUniqueId(), balance);
    Messages.sendParsedMessage(sender, MessageType.ECONOMY_ADD,
      Number.format(balance, Number.FormatType.COMMAS), target.getName());
  }
  
  private void removeBalance(CommandSender sender, CommandArguments args) {
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
    QualityEconomyAPI.removeBalance(target.getUniqueId(), balance);
    Messages.sendParsedMessage(sender, MessageType.ECONOMY_REMOVE,
      Number.format(balance, Number.FormatType.COMMAS), target.getName());
  }
  
}
