package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class BalanceCommand {
  
  public static void loadBalanceCommand() {
    new CommandTree("balance")
      .withAliases("bal")
      .then(new OfflinePlayerArgument("target")
        .replaceSuggestions(ArgumentSuggestions.strings((x) -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList().toArray(new String[0])))
        .executes((sender, args) -> {
          OfflinePlayer target = (OfflinePlayer) args.get("target");
          if (!AccountManager.accountExists(target.getUniqueId())) {
            sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
            return;
          }
          sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("balance.other-balance"),
            TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(target.getName()))),
            TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(AccountManager.getAccount(target.getUniqueId()).getBalance()))))));
        }))
      .executesPlayer((sender, args) -> {
        sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("balance.own-balance"),
          TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(AccountManager.getAccount(sender.getUniqueId()).getBalance()))))));
      })
      .register();
  }
  
}
