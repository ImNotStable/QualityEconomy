package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.config.MessageType;
import com.imnotstable.qualityeconomy.config.Messages;
import com.imnotstable.qualityeconomy.economy.Currency;
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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CurrencyCommand {
  
  private final View view;
  private final Admin admin;
  
  public CurrencyCommand(@NotNull Currency currency) {
    this.view = new View(currency);
    this.admin = new Admin(currency);
  }
  
  public void register() {
    view.register();
    admin.register();
  }
  
  public void unregister() {
    view.unregister();
    admin.unregister();
  }
  
  private static class View extends BaseCommand {
    
    private final CommandTree command;
    private final Currency currency;
    
    private View(@NotNull Currency currency) {
      this.currency = currency;
      if (currency.getViewCommand() == null)
        command = null;
      else
        command = new CommandTree(currency.getViewCommand())
          .withPermission("qualityeconomy." + currency.getName().toLowerCase())
          .then(CommandUtils.TargetArgument(false)
            .executes(this::viewOtherBalance))
          .executesPlayer(this::viewOwnBalance);
    }
    
    private void viewOtherBalance(CommandSender sender, CommandArguments args) {
      OfflinePlayer target = (OfflinePlayer) args.get("target");
      Messages.sendParsedMessage(sender, MessageType.BALANCE_OTHER_BALANCE,
        "balance", Number.format(QualityEconomyAPI.getBalance(target.getUniqueId(), currency.getName()), Number.FormatType.COMMAS), "player", target.getName());
    }
    
    private void viewOwnBalance(Player sender, CommandArguments args) {
      Messages.sendParsedMessage(sender, MessageType.BALANCE_OWN_BALANCE,
        "balance", Number.format(QualityEconomyAPI.getBalance(sender.getUniqueId(), currency.getName()), Number.FormatType.COMMAS));
    }
    
    @Override
    public void register() {
      super.register(command, command != null);
    }
    
    @Override
    public void unregister() {
      super.unregister(command);
    }
    
  }
  
  private static class Admin extends BaseCommand {
    
    private final CommandTree command;
    private final Currency currency;
    
    private Admin(@NotNull Currency currency) {
      this.currency = currency;
      if (currency.getAdminCommand() == null)
        command = null;
      else
        command = new CommandTree(currency.getAdminCommand())
          .withPermission("qualityeconomy.admin")
          .withAliases(currency.getAdminAliases())
          .then(CommandUtils.TargetArgument(false)
            .then(new LiteralArgument("reset").executes(this::resetBalance))
            .then(new LiteralArgument("set").then(CommandUtils.AmountArgument().executes(this::setBalance)))
            .then(new LiteralArgument("add").then(CommandUtils.AmountArgument().executes(this::addBalance)))
            .then(new LiteralArgument("remove").then(CommandUtils.AmountArgument().executes(this::removeBalance))));
    }
    
    @SneakyThrows
    private void resetBalance(CommandSender sender, CommandArguments args) {
      OfflinePlayer target = (OfflinePlayer) args.get("target");
      EconomicTransaction.startNewTransaction(EconomicTransactionType.BALANCE_RESET, sender, currency, 0, EconomyPlayer.of(target)).execute();
    }
    
    @SneakyThrows
    private void setBalance(CommandSender sender, CommandArguments args) {
      OfflinePlayer target = (OfflinePlayer) args.get("target");
      double balance = (double) args.get("amount");
      EconomicTransaction.startNewTransaction(EconomicTransactionType.BALANCE_SET, sender, currency, balance, EconomyPlayer.of(target)).execute();
    }
    
    @SneakyThrows
    private void addBalance(CommandSender sender, CommandArguments args) {
      OfflinePlayer target = (OfflinePlayer) args.get("target");
      double balance = (double) args.get("amount");
      EconomicTransaction.startNewTransaction(EconomicTransactionType.BALANCE_ADD, sender, currency, balance, EconomyPlayer.of(target)).execute();
    }
    
    @SneakyThrows
    private void removeBalance(CommandSender sender, CommandArguments args) {
      OfflinePlayer target = (OfflinePlayer) args.get("target");
      double balance = (double) args.get("amount");
      EconomicTransaction.startNewTransaction(EconomicTransactionType.BALANCE_REMOVE, sender, currency, balance, EconomyPlayer.of(target)).execute();
    }
    
    @Override
    public void register() {
      super.register(command, command != null);
    }
    
    @Override
    public void unregister() {
      super.unregister(command);
    }
    
  }
  
  private static class Transfer extends BaseCommand {
    
    private final CommandTree command;
    private final Currency currency;
    
    private Transfer(@NotNull Currency currency) {
      this.currency = currency;
      if (currency.getTransferCommand() == null)
        command = null;
      else
        command = new CommandTree("pay")
          .withPermission("qualityeconomy.pay")
          .then(new LiteralArgument("toggle")
            .executesPlayer(this::togglePay))
          .then(CommandUtils.TargetArgument(false)
            .then(CommandUtils.AmountArgument()
              .executesPlayer(this::pay)));
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
      EconomicTransaction.startNewTransaction(EconomicTransactionType.BALANCE_TRANSFER, sender, currency, amount, EconomyPlayer.of(sender), EconomyPlayer.of(target)).execute();
    }
    
    public void register() {
      super.register(command);
    }
    
    public void unregister() {
      super.unregister(command);
    }
    
  }
  
}
