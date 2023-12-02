package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import com.imnotstable.qualityeconomy.util.Misc;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CustomBalanceCommand extends AbstractCommand {
  
  private final @Getter String name = "custombalance";
  
  private final CommandTree command = new CommandTree(name)
    .withAliases("cbalance", "custombal", "cbal")
    .then(new StringArgument("currency")
      .replaceSuggestions(ArgumentSuggestions.strings(info -> CustomCurrencies.getCustomCurrencies().toArray(new String[0])))
      .then(new OfflinePlayerArgument("target")
        .replaceSuggestions(ArgumentSuggestions.strings(Misc::getOfflinePlayerSuggestion))
        .executes(this::viewOtherBalance))
      .executesPlayer(this::viewOwnBalance));
  private boolean isRegistered = false;
  
  public void register() {
    if (isRegistered || !Configuration.isCustomBalanceCommandEnabled() || CustomCurrencies.getCustomCurrencies().isEmpty())
      return;
    command.register();
    isRegistered = true;
  }
  
  public void unregister() {
    if (!isRegistered)
      return;
    CommandAPI.unregister(name, true);
    isRegistered = false;
  }
  
  private void viewOtherBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.currencyDoesNotExist(currency, sender))
      return;
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.playerDoesNotExist(target.getUniqueId(), sender))
      return;
    Messages.sendParsedMessage(MessageType.BALANCE_OTHER_BALANCE, new String[]{
      Number.formatCommas(QualityEconomyAPI.getCustomBalance(target.getUniqueId(), currency)),
      target.getName()
    }, sender);
  }
  
  private void viewOwnBalance(Player sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.currencyDoesNotExist(currency, sender))
      return;
    Messages.sendParsedMessage(MessageType.BALANCE_OWN_BALANCE, new String[]{
      Number.formatCommas(QualityEconomyAPI.getCustomBalance(sender.getUniqueId(), currency))
    }, sender);
  }
  
}
