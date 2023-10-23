package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.Account;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class EconomyCommand {
  
  public static void loadCommand() {
    new CommandTree("economy")
      .withAliases("eco")
      .withPermission("qualityeconomy.economy")
      .then(new OfflinePlayerArgument("target")
        .replaceSuggestions(ArgumentSuggestions.strings((x) -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList().toArray(new String[0])))
        .then(new LiteralArgument("set")
          .then(new DoubleArgument("amount")
            .executes((sender, args) -> {
              OfflinePlayer target = (OfflinePlayer) args.get("target");
              if (!AccountManager.accountExists(target.getUniqueId())) {
                sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
                return;
              }
              double balance = (double) args.get("amount");
              AccountManager.updateAccount(AccountManager.getAccount(target.getUniqueId()).setBalance(balance));
              sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("economy.set"),
                TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(target.getName()))),
                TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(balance))))));
            })))
        .then(new LiteralArgument("reset")
          .executes((sender, args) -> {
            OfflinePlayer target = (OfflinePlayer) args.get("target");
            if (!AccountManager.accountExists(target.getUniqueId())) {
              sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
              return;
            }
            AccountManager.updateAccount(AccountManager.getAccount(target.getUniqueId()).setBalance(0));
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("economy.reset"),
              TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(target.getName()))),
              TagResolver.resolver("", Tag.selfClosingInserting(Component.text("")))));
          }))
        .then(new LiteralArgument("add")
          .then(new DoubleArgument("amount")
            .executes((sender, args) -> {
              OfflinePlayer target = (OfflinePlayer) args.get("target");
              if (!AccountManager.accountExists(target.getUniqueId())) {
                sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
                return;
              }
              Account account = AccountManager.getAccount(target.getUniqueId());
              double balance = (double) args.get("amount");
              AccountManager.updateAccount(account.setBalance(account.getBalance() + balance));
              sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("economy.add"),
                TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(balance)))),
                TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(target.getName())))));
            })))
        .then(new LiteralArgument("remove")
          .then(new DoubleArgument("amount")
            .executes((sender, args) -> {
              OfflinePlayer target = (OfflinePlayer) args.get("target");
              if (!AccountManager.accountExists(target.getUniqueId())) {
                sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
                return;
              }
              Account account = AccountManager.getAccount(target.getUniqueId());
              double balance = (double) args.get("amount");
              AccountManager.updateAccount(account.setBalance(account.getBalance() - balance));
              sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("economy.remove"),
                TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(balance)))),
                TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(target.getName())))));
            }))))
      .register();
  }
  
  public static void unloadCommand() {
    CommandAPI.unregister("economy", true);
  }
  
}
