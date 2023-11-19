package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;
import com.imnotstable.qualityeconomy.util.Misc;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CustomBalanceCommand {
  
  private static boolean isRegistered = false;
  
  public static void register() {
    if (isRegistered)
      return;
    new CommandTree("custombalance")
      .withAliases("cbalance", "custombal", "cbal")
      .then(new StringArgument("currency")
        .replaceSuggestions(ArgumentSuggestions.strings(info -> CustomCurrencies.getCustomCurrencies().toArray(new String[0])))
        .then(new OfflinePlayerArgument("target")
          .replaceSuggestions(ArgumentSuggestions.strings(Misc::getOfflinePlayerSuggestion))
          .executes(CustomBalanceCommand::viewOtherBalance))
        .executesPlayer(CustomBalanceCommand::viewOwnBalance))
      .register();
    isRegistered = true;
  }
  
  public static void unregister() {
    CommandAPI.unregister("custombalance", true);
    isRegistered = false;
  }
  
  private static void viewOtherBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (!CustomCurrencies.getCustomCurrencies().contains(currency)) {
      sender.sendMessage(Component.text("That currency does not exist", NamedTextColor.RED));
      return;
    }
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (!AccountManager.accountExists(target.getUniqueId())) {
      sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
      return;
    }
    sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage(MessageType.BALANCE_OTHER_BALANCE),
      TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(target.getName()))),
      TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(AccountManager.getAccount(target.getUniqueId()).getCustomBalance(currency)))))));
  }
  
  private static void viewOwnBalance(Player sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (!CustomCurrencies.getCustomCurrencies().contains(currency)) {
      sender.sendMessage(Component.text("That currency does not exist", NamedTextColor.RED));
      return;
    }
    sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage(MessageType.BALANCE_OWN_BALANCE),
      TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(AccountManager.getAccount(sender.getUniqueId()).getCustomBalance(currency)))))));
  }
  
}
