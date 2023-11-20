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
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand {
  
  private static boolean isRegistered = false;
  
  public static void register() {
    if (isRegistered || !Configuration.isBalanceCommandEnabled())
      return;
    new CommandTree("balance")
      .withAliases("bal")
      .then(new OfflinePlayerArgument("target")
        .replaceSuggestions(ArgumentSuggestions.strings(Misc::getOfflinePlayerSuggestion))
        .executes(BalanceCommand::viewOtherBalance))
      .executesPlayer(BalanceCommand::viewOwnBalance)
      .register();
    isRegistered = true;
  }
  
  public static void unregister() {
    if (!isRegistered)
      return;
    CommandAPI.unregister("balance", true);
    isRegistered = false;
  }
  
  private static void viewOtherBalance(CommandSender sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (!QualityEconomyAPI.hasAccount(target.getUniqueId())) {
      sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
      return;
    }
    sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage(MessageType.BALANCE_OTHER_BALANCE),
      TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(target.getName()))),
      TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(QualityEconomyAPI.getBalance(target.getUniqueId())))))));
  }
  
  private static void viewOwnBalance(Player sender, CommandArguments args) {
    sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage(MessageType.BALANCE_OWN_BALANCE),
      TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(QualityEconomyAPI.getBalance(sender.getUniqueId())))))));
  }
  
}
