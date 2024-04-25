package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.config.MessageType;
import com.imnotstable.qualityeconomy.config.Messages;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CustomBalanceCommand extends BaseCommand {
  
  private final CommandTree command = new CommandTree("custombalance")
    .withPermission("qualityeconomy.custombalance")
    .withAliases("cbalance", "custombal", "cbal")
    .then(CommandUtils.CurrencyArgument()
      .then(CommandUtils.TargetArgument(false)
        .executes(this::viewOtherBalance))
      .executesPlayer(this::viewOwnBalance));
  
  @SuppressWarnings("SimplifiableConditionalExpression")
  public void register() {
    super.register(command, (QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES ? !StorageManager.getActiveStorageType().getCurrencies().isEmpty() : false) && QualityEconomy.getQualityConfig().COMMANDS_CUSTOMBALANCE);
  }
  
  public void unregister() {
    super.unregister(command);
  }
  
  private void viewOtherBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    Messages.sendParsedMessage(sender, MessageType.BALANCE_OTHER_BALANCE,
      Number.format(QualityEconomyAPI.getBalance(target.getUniqueId(), currency), Number.FormatType.COMMAS), target.getName());
  }
  
  private void viewOwnBalance(Player sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    Messages.sendParsedMessage(sender, MessageType.BALANCE_OWN_BALANCE,
      Number.format(QualityEconomyAPI.getBalance(sender.getUniqueId(), currency), Number.FormatType.COMMAS));
  }
  
}
