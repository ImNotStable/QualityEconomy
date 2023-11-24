package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.util.Misc;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class EconomyCommand {
  
  private static boolean isRegistered = false;
  private static final CommandTree command = new CommandTree("economy")
    .withPermission("qualityeconomy.economy")
    .withAliases("eco")
    .then(new OfflinePlayerArgument("target")
      .replaceSuggestions(ArgumentSuggestions.strings(Misc::getOfflinePlayerSuggestion))
      .then(new LiteralArgument("reset").executes(EconomyCommand::resetBalance))
      .then(new LiteralArgument("set").then(new DoubleArgument("amount").executes(EconomyCommand::setBalance)))
      .then(new LiteralArgument("add").then(new DoubleArgument("amount").executes(EconomyCommand::addBalance)))
      .then(new LiteralArgument("remove").then(new DoubleArgument("amount").executes(EconomyCommand::removeBalance))));
  
  public static void register() {
    if (isRegistered || !Configuration.isEconomyCommandEnabled())
      return;
    command.register();
    isRegistered = true;
  }
  
  public static void unregister() {
    if (!isRegistered)
      return;
    CommandAPI.unregister("economy", true);
    isRegistered = false;
  }
  
  private static void resetBalance(CommandSender sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (!QualityEconomyAPI.hasAccount(target.getUniqueId())) {
      sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
      return;
    }
    QualityEconomyAPI.setBalance(target.getUniqueId(), 0);
    Messages.sendParsedMessage(MessageType.ECONOMY_RESET, new String[]{
      target.getName()
    }, sender);
  }
  
  private static void setBalance(CommandSender sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (!QualityEconomyAPI.hasAccount(target.getUniqueId())) {
      sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
      return;
    }
    double balance = Number.roundObj(args.get("amount"));
    QualityEconomyAPI.setBalance(target.getUniqueId(), balance);
    Messages.sendParsedMessage(MessageType.ECONOMY_SET, new String[]{
      Number.formatCommas(balance),
      target.getName()
    }, sender);
  }
  
  private static void addBalance(CommandSender sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (!QualityEconomyAPI.hasAccount(target.getUniqueId())) {
      sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
      return;
    }
    double balance = Number.roundObj(args.get("amount"));
    QualityEconomyAPI.addBalance(target.getUniqueId(), balance);
    Messages.sendParsedMessage(MessageType.ECONOMY_ADD, new String[]{
      Number.formatCommas(balance),
      target.getName()
    }, sender);
  }
  
  private static void removeBalance(CommandSender sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (!QualityEconomyAPI.hasAccount(target.getUniqueId())) {
      sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
      return;
    }
    double balance = Number.roundObj(args.get("amount"));
    QualityEconomyAPI.removeBalance(target.getUniqueId(), balance);
    Messages.sendParsedMessage(MessageType.ECONOMY_REMOVE, new String[]{
      Number.formatCommas(balance),
      target.getName()
    }, sender);
  }
  
}
