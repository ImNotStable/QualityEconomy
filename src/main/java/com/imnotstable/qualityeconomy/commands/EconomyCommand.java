package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.configuration.Messages;
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
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EconomyCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (args.length > 1) {
      OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(args[0]);
      if (player == null) {
        sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
        return true;
      }
      double num = 0;
      if (!args[1].equals("reset")) {
        if (args.length < 3) {
          sender.sendMessage(Component.text("Invalid usage.", NamedTextColor.RED));
          return true;
        }
        try {
          num = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
          sender.sendMessage(Component.text("Invalid number.", NamedTextColor.RED));
          return true;
        }
      }
      UUID uuid = player.getUniqueId();
      Component message;
      double balance = AccountManager.getAccount(uuid).getBalance();
      switch (args[1]) {
        case "reset" -> {
          balance = 0;
          message = MiniMessage.miniMessage().deserialize(Messages.getMessage("economy.reset"),
            TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(player.getName()))),
            TagResolver.resolver("", Tag.selfClosingInserting(Component.text(""))),
            TagResolver.resolver("", Tag.selfClosingInserting(Component.text(""))),
            TagResolver.resolver("", Tag.selfClosingInserting(Component.text(""))));
        }
        case "set" -> {
          balance = num;
          message = MiniMessage.miniMessage().deserialize(Messages.getMessage("economy.set"),
            TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(player.getName()))),
            TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(balance)))));
        }
        case "add" -> {
          balance += num;
          message = MiniMessage.miniMessage().deserialize(Messages.getMessage("economy.add"),
            TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(balance)))),
            TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(player.getName()))));
        }
        case "remove" -> {
          balance -= num;
          message = MiniMessage.miniMessage().deserialize(Messages.getMessage("economy.remove"),
            TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(balance)))),
            TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(player.getName()))));
        }
        default -> {
          sender.sendMessage(Component.text("Incorrect Usage.", NamedTextColor.RED));
          return true;
        }
      }
      AccountManager.updateAccount(AccountManager.getAccount(uuid).setBalance(balance));
      sender.sendMessage(message);
    }
    return true;
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
    List<String> completions = new ArrayList<>();
    if (args.length == 1) {
      for (Player player : Bukkit.getOnlinePlayers()) {
        completions.add(player.getName());
      }
    } else if (args.length == 2) {
      completions.add("reset");
      completions.add("set");
      completions.add("remove");
      completions.add("add");
    } else if (args.length == 3) {
      if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("add")) {
        completions.add("<amount>");
      }
    }
    return completions;
  }

}
