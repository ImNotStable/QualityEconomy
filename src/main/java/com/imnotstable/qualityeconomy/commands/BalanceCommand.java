package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements Command {
  
  @Getter
  private final String name = "balance";
  
  private final CommandTree command = new CommandTree(name)
    .withAliases("bal")
    .then(new OfflinePlayerArgument("target")
      .replaceSuggestions(CommandUtils.getOnlinePlayerSuggestion())
      .executes(this::viewOtherBalance))
    .executesPlayer(this::viewOwnBalance);
  private boolean isRegistered = false;
  
  public void register() {
    if (isRegistered || !Configuration.isCommandEnabled("balance"))
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
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    Messages.sendParsedMessage(MessageType.BALANCE_OTHER_BALANCE, new String[]{
      Number.formatCommas(QualityEconomyAPI.getBalance(target.getUniqueId())),
      target.getName()
    }, sender);
  }
  
  private void viewOwnBalance(Player sender, CommandArguments args) {
    Messages.sendParsedMessage(MessageType.BALANCE_OWN_BALANCE, new String[]{
      Number.formatCommas(QualityEconomyAPI.getBalance(sender.getUniqueId()))
    }, sender);
  }
  
}
