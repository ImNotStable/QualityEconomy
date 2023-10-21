package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.Account;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.util.Number;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PayCommand implements TabExecutor {

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (sender instanceof Player player) {
      if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
        UUID uuid = player.getUniqueId();
        Account account = AccountManager.getAccount(uuid);
        boolean toggle = !account.getPayable();
        AccountManager.updateAccount(account.setPayable(toggle));
        if (toggle) {
          sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("pay.toggle-on")));
        } else {
          sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("pay.toggle-off")));
        }
        return true;
      } else if (args.length == 2) {

        OfflinePlayer receiver = Bukkit.getOfflinePlayer(args[0]);
        if (!AccountManager.accountExists(receiver.getUniqueId())) {
          sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
          return true;
        }

        if (!AccountManager.getAccount(receiver.getUniqueId()).getPayable()) {
          sender.sendMessage(Component.text("This player isn't accepting payments.", NamedTextColor.RED));
          return true;
        }

        double amount = 0;
        try {
          amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException ignored) {}
        if (amount < 0.01) {
          sender.sendMessage(Component.text("Invalid amount.", NamedTextColor.RED));
          return true;
        }

        Account account = AccountManager.getAccount(player.getUniqueId());
        if (account.getBalance() < amount) {
          player.sendMessage(Component.text("You don't have enough money.", NamedTextColor.RED));
          return true;
        }

        player.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("pay.send"),
          TagResolver.resolver("amount", Tag.selfClosingInserting(Component.text(Number.formatCommas(amount)))),
          TagResolver.resolver("target", Tag.selfClosingInserting(Component.text(receiver.getName() != null ? receiver.getName() : receiver.getUniqueId().toString())))));

        Account receiverAccount = AccountManager.getAccount(receiver.getUniqueId());
        AccountManager.updateAccount(account.setBalance(account.getBalance() - amount));
        if (receiver.isOnline())
          receiver.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("pay.receive"),
            TagResolver.resolver("amount", Tag.selfClosingInserting(Component.text(Number.formatCommas(amount)))),
            TagResolver.resolver("sender", Tag.selfClosingInserting(Component.text(player.getName())))));
        AccountManager.updateAccount(receiverAccount.setBalance(receiverAccount.getBalance() + amount));

        return true;
      }
      sender.sendMessage(Component.text("Incorrect usage.", NamedTextColor.RED));
      return true;
    }
    sender.sendMessage(Component.text("You cannot do this.", NamedTextColor.RED));
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    List<String> completions = new ArrayList<>();
    if (args.length == 1) {
      completions.add("toggle");
      Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
    }
    else if (args.length == 2) {
      completions.add("<amount>");
    }
    return completions;
  }

}
