package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.config.MessageType;
import com.imnotstable.qualityeconomy.config.Messages;
import com.imnotstable.qualityeconomy.economy.EconomicTransaction;
import com.imnotstable.qualityeconomy.economy.EconomicTransactionType;
import com.imnotstable.qualityeconomy.economy.EconomyPlayer;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.SneakyThrows;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PayCommand extends BaseCommand {
  
  private final CommandTree command = new CommandTree("pay")
    .then(new LiteralArgument("toggle")
      .executesPlayer(this::togglePay))
    .then(CommandUtils.TargetArgument(false)
      .then(CommandUtils.AmountArgument()
        .executesPlayer(this::pay)));
  
  public void register() {
    super.register(command, QualityEconomy.getQualityConfig().COMMANDS_PAY);
  }
  
  public void unregister() {
    super.unregister(command);
  }
  
  private void togglePay(Player sender, CommandArguments args) {
    boolean toggle = !QualityEconomyAPI.isPayable(sender.getUniqueId());
    QualityEconomyAPI.setPayable(sender.getUniqueId(), toggle);
    if (toggle) {
      Messages.sendParsedMessage(sender, MessageType.PAY_TOGGLE_ON);
    } else {
      Messages.sendParsedMessage(sender, MessageType.PAY_TOGGLE_OFF);
    }
  }
  
  @SneakyThrows
  private void pay(Player sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.isPayable(target.getUniqueId()), MessageType.NOT_ACCEPTING_PAYMENTS, sender))
      return;
    double amount = (double) args.get("amount");
    if (CommandUtils.requirement(QualityEconomyAPI.hasBalance(sender.getUniqueId(), amount), MessageType.SELF_NOT_ENOUGH_MONEY, sender))
      return;
    if (CommandUtils.requirement(amount >= Number.getMinimumValue(), MessageType.INVALID_NUMBER, sender))
      return;
    EconomicTransaction.startNewTransaction(EconomicTransactionType.BALANCE_TRANSFER, sender, amount, EconomyPlayer.of(sender), EconomyPlayer.of(target)).execute();
  }
  
}
