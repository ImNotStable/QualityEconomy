package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.config.MessageType;
import com.imnotstable.qualityeconomy.config.Messages;
import com.imnotstable.qualityeconomy.economy.Account;
import com.imnotstable.qualityeconomy.economy.Currency;
import com.imnotstable.qualityeconomy.economy.EconomicTransaction;
import com.imnotstable.qualityeconomy.economy.EconomicTransactionType;
import com.imnotstable.qualityeconomy.economy.EconomyPlayer;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import com.imnotstable.qualityeconomy.util.debug.Timer;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;

public class CurrencyCommand {
  
  private final View view;
  private final Admin admin;
  private final Transfer transfer;
  private final Leaderboard leaderboard;
  
  public CurrencyCommand(@NotNull Currency currency) {
    this.view = new View(currency);
    this.admin = new Admin(currency);
    this.transfer = new Transfer(currency);
    this.leaderboard = new Leaderboard(currency);
  }
  
  public Account getLeaderboardAccount(int index) {
    if (Leaderboard.orderedPlayerList == null)
      throw new IllegalStateException("Leaderboard was not initialized");
    if (index < 0 || index >= Leaderboard.orderedPlayerList.length)
      return null;
    return Leaderboard.orderedPlayerList[index];
  }
  
  public void register() {
    view.register();
    admin.register();
    transfer.register();
    leaderboard.register();
  }
  
  public void unregister() {
    view.unregister();
    admin.unregister();
    transfer.unregister();
    leaderboard.unregister();
  }
  
  private static class View extends BaseCommand {
    
    private final CommandTree command;
    private final Currency currency;
    
    private View(@NotNull Currency currency) {
      this.currency = currency;
      Currency.Command viewCommand = currency.getViewCommand();
      if (viewCommand.command() != null)
        command = new CommandTree(viewCommand.command())
          .withAliases(viewCommand.aliases())
          .withPermission(viewCommand.permission())
          .then(CommandUtils.TargetArgument(currency, MessageType.VIEW_PLAYER_NOT_FOUND)
            .executes(this::viewOtherBalance))
          .executesPlayer(this::viewOwnBalance);
      else
        command = null;
    }
    
    private void viewOtherBalance(CommandSender sender, CommandArguments args) {
      OfflinePlayer target = (OfflinePlayer) args.get("target");
      Messages.sendParsedMessage(sender, currency.getMessage(MessageType.VIEW_OTHER),
        "balance", currency.getFormattedBalance(target.getUniqueId()), "player", target.getName());
    }
    
    private void viewOwnBalance(Player sender, CommandArguments args) {
      Messages.sendParsedMessage(sender, currency.getMessage(MessageType.VIEW_OWN),
        "balance", currency.getFormattedBalance(sender.getUniqueId()));
    }
    
    @Override
    public void register() {
      super.register(command);
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
      Currency.Command adminCommand = currency.getAdminCommand();
      if (adminCommand.command() != null)
        command = new CommandTree(adminCommand.command())
          .withAliases(adminCommand.aliases())
          .withPermission(adminCommand.permission())
          .then(CommandUtils.TargetArgument(currency, MessageType.ADMIN_PLAYER_NOT_FOUND)
            .then(new LiteralArgument("reset").executes(this::resetBalance))
            .then(new LiteralArgument("set").then(CommandUtils.AmountArgument(currency, MessageType.ADMIN_INVALID_NUMBER).executes(this::setBalance)))
            .then(new LiteralArgument("add").then(CommandUtils.AmountArgument(currency, MessageType.ADMIN_INVALID_NUMBER).executes(this::addBalance)))
            .then(new LiteralArgument("remove").then(CommandUtils.AmountArgument(currency, MessageType.ADMIN_INVALID_NUMBER).executes(this::removeBalance))));
      else
        command = null;
    }
    
    private void resetBalance(CommandSender sender, CommandArguments args) {
      OfflinePlayer target = (OfflinePlayer) args.get("target");
      EconomicTransaction.startNewTransaction(EconomicTransactionType.BALANCE_RESET, sender, currency, 0, EconomyPlayer.of(target)).execute();
    }
    
    private void setBalance(CommandSender sender, CommandArguments args) {
      OfflinePlayer target = (OfflinePlayer) args.get("target");
      double balance = (double) args.get("amount");
      EconomicTransaction.startNewTransaction(EconomicTransactionType.BALANCE_SET, sender, currency, balance, EconomyPlayer.of(target)).execute();
    }
    
    private void addBalance(CommandSender sender, CommandArguments args) {
      OfflinePlayer target = (OfflinePlayer) args.get("target");
      double balance = (double) args.get("amount");
      EconomicTransaction.startNewTransaction(EconomicTransactionType.BALANCE_ADD, sender, currency, balance, EconomyPlayer.of(target)).execute();
    }
    
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
      Currency.Command transferCommand = currency.getTransferCommand();
      if (transferCommand.command() != null)
        command = new CommandTree(transferCommand.command())
          .withAliases(transferCommand.aliases())
          .withPermission(transferCommand.permission())
          .then(new LiteralArgument("toggle")
            .executesPlayer(this::togglePay))
          .then(CommandUtils.TargetArgument(currency, MessageType.TRANSFER_PLAYER_NOT_FOUND)
            .then(CommandUtils.AmountArgument(currency, MessageType.TRANSFER_INVALID_NUMBER)
              .executesPlayer(this::pay)));
      else
        command = null;
    }
    
    private void togglePay(Player sender, CommandArguments args) {
      boolean toggle = !QualityEconomyAPI.isPayable(sender.getUniqueId());
      QualityEconomyAPI.setPayable(sender.getUniqueId(), toggle);
      if (toggle) {
        Messages.sendParsedMessage(sender, currency.getMessage(MessageType.TRANSFER_TOGGLE_ON));
      } else {
        Messages.sendParsedMessage(sender, currency.getMessage(MessageType.TRANSFER_TOGGLE_OFF));
      }
    }
    
    private void pay(Player sender, CommandArguments args) {
      OfflinePlayer target = (OfflinePlayer) args.get("target");
      if (CommandUtils.requirement(QualityEconomyAPI.isPayable(target.getUniqueId()), currency, MessageType.TRANSFER_NOT_ACCEPTING_PAYMENTS, sender))
        return;
      double amount = (double) args.get("amount");
      if (CommandUtils.requirement(QualityEconomyAPI.hasBalance(sender.getUniqueId(), currency.getName(), amount), currency, MessageType.TRANSFER_NOT_ENOUGH_MONEY, sender))
        return;
      if (CommandUtils.requirement(amount >= currency.getMinimumValue(), currency, MessageType.TRANSFER_INVALID_NUMBER, sender))
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
  
  private static class Leaderboard extends BaseCommand {
    
    public static Account[] orderedPlayerList;
    private final CommandTree command;
    private final Currency currency;
    private String serverTotal = "0.0";
    private int maxPage;
    private Integer taskID = null;
    
    public Leaderboard(Currency currency) {
      this.currency = currency;
      Currency.Command leaderboardCommand = currency.getLeaderboardCommand();
      if (leaderboardCommand.command() != null)
        command = new CommandTree(leaderboardCommand.command())
          .withAliases(leaderboardCommand.aliases())
          .withPermission(leaderboardCommand.permission())
          .then(new LiteralArgument("update")
            .withPermission(currency.getAdminCommand().permission())
            .executes((sender, args) -> {
              updateBalanceTop();
            }))
          .then(new IntegerArgument("page", 1)
            .setOptional(true)
            .executes(this::viewBalanceTop));
      else
        command = null;
    }
    
    private void viewBalanceTop(CommandSender sender, CommandArguments args) {
      int page = Math.min((int) args.getOrDefault("page", 1), maxPage);
      int startIndex = (page - 1) * 10;
      int endIndex = Math.min(startIndex + 10, orderedPlayerList.length);
      
      Messages.sendParsedMessage(sender, currency.getMessage(MessageType.LEADERBOARD_TITLE),
        "maxpage", String.valueOf(maxPage),
        "page", String.valueOf(page));
      Messages.sendParsedMessage(sender, currency.getMessage(MessageType.LEADERBOARD_SERVER_TOTAL),
        "servertotal", serverTotal);
      
      if (maxPage != 0)
        for (int i = startIndex; i < endIndex; i++) {
          Account account = orderedPlayerList[i];
          Messages.sendParsedMessage(sender, currency.getMessage(MessageType.LEADERBOARD_BALANCE_VIEW),
            "place", String.valueOf(i + 1),
            "player", account.getUsername(),
            "balance", currency.getFormattedBalance(account.getUniqueId()));
        }
      
      Messages.sendParsedMessage(sender, currency.getMessage(MessageType.LEADERBOARD_NEXT_PAGE),
        "command", args.fullInput().split(" ")[0],
        "nextpage", String.valueOf(page + 1));
    }
    
    private void updateBalanceTop() {
      Timer timer = new Timer("updateBalanceTop() {" + currency.getName() + "}");
      
      Collection<Account> accounts = AccountManager.getAllAccounts();
      
      serverTotal = currency.getFormattedAmount(accounts.stream()
        .mapToDouble(account -> account.getBalance(currency.getName()))
        .sum());
      orderedPlayerList = accounts.stream()
        .sorted(Comparator.comparingDouble((Account account) -> account.getBalance(currency.getName())).reversed())
        .toArray(Account[]::new);
      maxPage = (int) Math.ceil(orderedPlayerList.length / 10.0);
      timer.end();
    }
    
    public void register() {
      if (!super.register(command))
        return;
      taskID = Bukkit.getScheduler().runTaskTimerAsynchronously(QualityEconomy.getInstance(), this::updateBalanceTop, 0L, currency.getLeaderboardRefreshInterval()).getTaskId();
    }
    
    public void unregister() {
      if (!super.unregister(command))
        return;
      if (taskID == null)
        return;
      Bukkit.getScheduler().cancelTask(taskID);
      taskID = null;
    }
  }
  
}
