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

public class PayCommand {
  
  public static void loadCommand() {
    new CommandTree("pay")
      .then(new LiteralArgument("toggle")
        .executesPlayer((sender, args) -> {
          Account account = AccountManager.getAccount(sender.getUniqueId());
          boolean toggle = !account.getPayable();
          AccountManager.updateAccount(account.setPayable(toggle));
          if (toggle) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("pay.toggle-on")));
          } else {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("pay.toggle-off")));
          }
        }))
      .then(new OfflinePlayerArgument("target")
        .includeSuggestions(ArgumentSuggestions.strings((x) -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList().toArray(new String[0])))
        .then(new DoubleArgument("amount", 0.01)
          .executesPlayer((sender, args) -> {
            OfflinePlayer target = (OfflinePlayer) args.get("target");
            
            if (!AccountManager.accountExists(target.getUniqueId())) {
              sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
              return;
            }
            if (!AccountManager.getAccount(target.getUniqueId()).getPayable()) {
              sender.sendMessage(Component.text("This player is not accepting payments", NamedTextColor.RED));
              return;
            }
            double amount = (double) args.get("amount");
            Account account = AccountManager.getAccount(sender.getUniqueId());
            if (account.getBalance() < amount) {
              sender.sendMessage(Component.text("You do not have enough money", NamedTextColor.RED));
              return;
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("pay.send"),
              TagResolver.resolver("amount", Tag.selfClosingInserting(Component.text(Number.formatCommas(amount)))),
              TagResolver.resolver("target", Tag.selfClosingInserting(Component.text(args.getRaw("target"))))));
            Account receiverAccount = AccountManager.getAccount(target.getUniqueId());
            AccountManager.updateAccount(account.setBalance(account.getBalance() - amount));
            if (target.isOnline())
              target.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("pay.receive"),
                TagResolver.resolver("amount", Tag.selfClosingInserting(Component.text(Number.formatCommas(amount)))),
                TagResolver.resolver("sender", Tag.selfClosingInserting(Component.text(sender.getName())))));
            AccountManager.updateAccount(receiverAccount.setBalance(receiverAccount.getBalance() + amount));
          })))
      .register();
  }
  
  public static void unloadCommand() {
    CommandAPI.unregister("pay", true);
  }
  
}
