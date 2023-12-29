package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class EconomyCommand implements Command {
  
  @Getter
  private final String name = "economy";
  
  private final CommandTree command = new CommandTree(name)
    .withPermission("qualityeconomy.economy")
    .withAliases("eco")
    .then(new OfflinePlayerArgument("target")
      .replaceSuggestions(CommandUtils.getOnlinePlayerSuggestion())
      .then(new LiteralArgument("reset").executes(this::resetBalance))
      .then(new LiteralArgument("set").then(new DoubleArgument("amount").executes(this::setBalance)))
      .then(new LiteralArgument("add").then(new DoubleArgument("amount").executes(this::addBalance)))
      .then(new LiteralArgument("remove").then(new DoubleArgument("amount").executes(this::removeBalance))));
  private boolean isRegistered = false;
  
  public void register() {
    if (isRegistered || !Configuration.isCommandEnabled("economy"))
      return;
    command.register();
    isRegistered = true;
  }
  
  public void unregister() {
    if (!isRegistered)
      return;
    CommandAPI.unregister(name, true);
    isRegistered = false;
  }
  
  private void resetBalance(CommandSender sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    QualityEconomyAPI.setBalance(target.getUniqueId(), 0);
    Messages.sendParsedMessage(MessageType.ECONOMY_RESET, new String[]{
      target.getName()
    }, sender);
  }
  
  private void setBalance(CommandSender sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    double balance = Number.roundObj(args.get("amount"));
    QualityEconomyAPI.setBalance(target.getUniqueId(), balance);
    Messages.sendParsedMessage(MessageType.ECONOMY_SET, new String[]{
      Number.formatCommas(balance),
      target.getName()
    }, sender);
  }
  
  private void addBalance(CommandSender sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    double balance = Number.roundObj(args.get("amount"));
    QualityEconomyAPI.addBalance(target.getUniqueId(), balance);
    Messages.sendParsedMessage(MessageType.ECONOMY_ADD, new String[]{
      Number.formatCommas(balance),
      target.getName()
    }, sender);
  }
  
  private void removeBalance(CommandSender sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    double balance = Number.roundObj(args.get("amount"));
    QualityEconomyAPI.removeBalance(target.getUniqueId(), balance);
    Messages.sendParsedMessage(MessageType.ECONOMY_REMOVE, new String[]{
      Number.formatCommas(balance),
      target.getName()
    }, sender);
  }
  
}
