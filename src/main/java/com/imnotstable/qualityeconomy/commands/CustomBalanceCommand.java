package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CustomBalanceCommand {
  
  private static boolean isRegistered = false;
  private static final CommandTree command = new CommandTree("custombalance")
    .withAliases("cbalance", "custombal", "cbal")
    .then(new StringArgument("currency")
      .replaceSuggestions(ArgumentSuggestions.strings(info -> CustomCurrencies.getCustomCurrencies().toArray(new String[0])))
      .then(new OfflinePlayerArgument("target")
        .replaceSuggestions(ArgumentSuggestions.strings(Misc::getOfflinePlayerSuggestion))
        .executes(CustomBalanceCommand::viewOtherBalance))
      .executesPlayer(CustomBalanceCommand::viewOwnBalance));
  
  public static void register() {
    if (isRegistered || !Configuration.isCustomBalanceCommandEnabled() || CustomCurrencies.getCustomCurrencies().isEmpty())
      return;
    command.register();
    isRegistered = true;
  }
  
  public static void unregister() {
    if (!isRegistered)
      return;
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
    if (!QualityEconomyAPI.hasAccount(target.getUniqueId())) {
      sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
      return;
    }
    Messages.sendParsedMessage(MessageType.BALANCE_OTHER_BALANCE, new String[]{
        Number.formatCommas(QualityEconomyAPI.getCustomBalance(target.getUniqueId(), currency)),
      target.getName()
    }, sender);
  }
  
  private static void viewOwnBalance(Player sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (!CustomCurrencies.getCustomCurrencies().contains(currency)) {
      sender.sendMessage(Component.text("That currency does not exist", NamedTextColor.RED));
      return;
    }
    Messages.sendParsedMessage(MessageType.BALANCE_OWN_BALANCE, new String[]{
      Number.formatCommas(QualityEconomyAPI.getCustomBalance(sender.getUniqueId(), currency))
    }, sender);
  }
  
}
