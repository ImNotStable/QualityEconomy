package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.config.MessageType;
import com.imnotstable.qualityeconomy.config.Messages;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand extends BaseCommand {
  
  private final CommandTree command = new CommandTree("balance")
    .withPermission("qualityeconomy.balance")
    .withAliases("bal")
    .then(CommandUtils.TargetArgument(false)
      .executes(this::viewOtherBalance))
    .executesPlayer(this::viewOwnBalance);
  
  public void register() {
    super.register(command, QualityEconomy.getQualityConfig().COMMANDS_BALANCE);
  }
  
  public void unregister() {
    super.unregister(command);
  }
  
  private void viewOtherBalance(CommandSender sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    Messages.sendParsedMessage(sender, MessageType.BALANCE_OTHER_BALANCE,
      "balance", Number.format(QualityEconomyAPI.getBalance(target.getUniqueId()), Number.FormatType.COMMAS),
      "player", target.getName());
  }
  
  private void viewOwnBalance(Player sender, CommandArguments args) {
    Messages.sendParsedMessage(sender, MessageType.BALANCE_OWN_BALANCE,
      "balance", Number.format(QualityEconomyAPI.getBalance(sender.getUniqueId()), Number.FormatType.COMMAS));
  }
  
}
