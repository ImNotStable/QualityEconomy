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
import org.bukkit.entity.Player;

public class PayCommand {
  
  private static boolean isRegistered = false;
  private static final CommandTree command = new CommandTree("pay")
    .then(new LiteralArgument("toggle")
      .executesPlayer(PayCommand::togglePay))
    .then(new OfflinePlayerArgument("target")
      .replaceSuggestions(ArgumentSuggestions.strings(Misc::getOfflinePlayerSuggestion))
      .then(new DoubleArgument("amount", Number.getMinimumValue())
        .executesPlayer(PayCommand::pay)));
  
  public static void register() {
    if (isRegistered || !Configuration.isPayCommandEnabled())
      return;
    command.register();
    isRegistered = true;
  }
  
  public static void unregister() {
    if (!isRegistered)
      return;
    CommandAPI.unregister("pay", true);
    isRegistered = false;
  }
  
  private static void togglePay(Player sender, CommandArguments args) {
    boolean toggle = !QualityEconomyAPI.isPayable(sender.getUniqueId());
    QualityEconomyAPI.setPayable(sender.getUniqueId(), toggle);
    if (toggle) {
      sender.sendMessage(Messages.getMessage(MessageType.PAY_TOGGLE_ON));
    } else {
      sender.sendMessage(Messages.getMessage(MessageType.PAY_TOGGLE_OFF));
    }
  }
  
  private static void pay(Player sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    
    if (!QualityEconomyAPI.hasAccount(target.getUniqueId())) {
      sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
      return;
    }
    if (!QualityEconomyAPI.isPayable(target.getUniqueId())) {
      sender.sendMessage(Component.text("This player is not accepting payments", NamedTextColor.RED));
      return;
    }
    double amount = Number.roundObj(args.get("amount"));
    if (!QualityEconomyAPI.hasBalance(sender.getUniqueId(), amount)) {
      sender.sendMessage(Component.text("You do not have enough money", NamedTextColor.RED));
      return;
    }
    Messages.sendParsedMessage(MessageType.PAY_SEND, new String[]{
      Number.formatCommas(amount),
      target.getName()
    }, sender);
    if (target.isOnline())
      Messages.sendParsedMessage(MessageType.PAY_RECEIVE, new String[]{
        Number.formatCommas(amount),
        sender.getName()
      }, sender);
    QualityEconomyAPI.transferBalance(sender.getUniqueId(), target.getUniqueId(), amount);
  }
  
}
