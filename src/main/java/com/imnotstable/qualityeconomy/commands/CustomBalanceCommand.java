package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CustomBalanceCommand extends BaseCommand {
  
  private final CommandTree command = new CommandTree("custombalance")
    .withAliases("cbalance", "custombal", "cbal")
    .then(new StringArgument("currency")
      .replaceSuggestions(ArgumentSuggestions.strings(info -> StorageManager.getActiveStorageType().getCurrencies().toArray(new String[0])))
      .then(new OfflinePlayerArgument("target")
        .replaceSuggestions(CommandUtils.getOnlinePlayerSuggestion())
        .executes(this::viewOtherBalance))
      .executesPlayer(this::viewOwnBalance));
  
  public void register() {
    super.register(command, !StorageManager.getActiveStorageType().getCurrencies().isEmpty());
  }
  
  public void unregister() {
    super.unregister(command);
  }
  
  private void viewOtherBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.requirement(StorageManager.getActiveStorageType().getCurrencies().contains(currency), MessageType.CURRENCY_NOT_FOUND, sender))
      return;
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    Messages.sendParsedMessage(sender, MessageType.BALANCE_OTHER_BALANCE,
      Number.format(QualityEconomyAPI.getCustomBalance(target.getUniqueId(), currency), Number.FormatType.COMMAS), target.getName());
  }
  
  private void viewOwnBalance(Player sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.requirement(StorageManager.getActiveStorageType().getCurrencies().contains(currency), MessageType.CURRENCY_NOT_FOUND, sender))
      return;
    Messages.sendParsedMessage(sender, MessageType.BALANCE_OWN_BALANCE,
      Number.format(QualityEconomyAPI.getCustomBalance(sender.getUniqueId(), currency), Number.FormatType.COMMAS));
  }
  
}
