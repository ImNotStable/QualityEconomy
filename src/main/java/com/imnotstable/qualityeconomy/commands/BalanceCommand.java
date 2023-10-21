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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BalanceCommand implements CommandExecutor {

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (sender instanceof Player player) {
      if (args.length == 1) {
        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[0]);
        if (target == null) {
          sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
          return true;
        }
        Component component = MiniMessage.miniMessage().deserialize(Messages.getMessage("balance.other-balance"),
            TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(target.getName()))),
            TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(AccountManager.getAccount(target.getUniqueId()).getBalance())))));
        sender.sendMessage(component);
        return true;
      }
      Component component =
        MiniMessage.miniMessage().deserialize(Messages.getMessage("balance.own-balance"),
          TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(AccountManager.getAccount(player.getUniqueId()).getBalance())))));
      sender.sendMessage(component);
      return true;
    }
    sender.sendMessage(Component.text("You cannot do this.", NamedTextColor.RED));
    return true;
  }

}
