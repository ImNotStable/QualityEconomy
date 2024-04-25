package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.config.MessageType;
import com.imnotstable.qualityeconomy.config.Messages;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.util.Number;
import com.imnotstable.qualityeconomy.util.debug.Timer;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Comparator;

public class BalanceTopCommand extends BaseCommand {
  
  public static Account[] orderedPlayerList;
  private String serverTotal = "0.0";
  private int maxPage;
  private final CommandTree command = new CommandTree("balancetop")
    .withPermission("qualityeconomy.balancetop")
    .withAliases("baltop")
    .then(new LiteralArgument("update")
      .withPermission("qualityeconomy.balancetop.update")
      .executes((sender, args) -> {
        updateBalanceTop();
      }))
    .then(new IntegerArgument("page", 1)
      .setOptional(true)
      .executes(this::viewBalanceTop));
  private Integer taskID = null;
  
  public void register() {
    if (!super.register(command, QualityEconomy.getQualityConfig().COMMANDS_BALANCETOP))
      return;
    long interval = QualityEconomy.getQualityConfig().BALANCETOP_INTERVAL;
    if (interval != 0)
      taskID = Bukkit.getScheduler().runTaskTimerAsynchronously(QualityEconomy.getInstance(), this::updateBalanceTop, 0L, interval).getTaskId();
  }
  
  public void unregister() {
    if (!super.unregister(command))
      return;
    if (taskID != null) {
      Bukkit.getScheduler().cancelTask(taskID);
      taskID = null;
    }
  }
  
  private void viewBalanceTop(CommandSender sender, CommandArguments args) {
    int page = Math.min((int) args.getOrDefault("page", 1), maxPage);
    int startIndex = (page - 1) * 10;
    int endIndex = Math.min(startIndex + 10, orderedPlayerList.length);
    
    Messages.sendParsedMessage(sender, MessageType.BALANCETOP_TITLE,
      "maxpage", String.valueOf(maxPage),
      "page", String.valueOf(page));
    Messages.sendParsedMessage(sender, MessageType.BALANCETOP_SERVER_TOTAL,
      "servertotal", serverTotal);
    
    if (maxPage != 0)
      for (int i = startIndex; i < endIndex; i++) {
        Account account = orderedPlayerList[i];
        Messages.sendParsedMessage(sender, MessageType.BALANCETOP_BALANCE_VIEW,
          "place", String.valueOf(i + 1),
          "player", account.getUsername(),
          "balance", Number.format(account.getDefaultBalance(), Number.FormatType.COMMAS));
      }
    
    Messages.sendParsedMessage(sender, MessageType.BALANCETOP_NEXT_PAGE,
      "command", args.fullInput().split(" ")[0].substring(1),
      "nextpage", String.valueOf(page + 1));
  }
  
  private void updateBalanceTop() {
    Timer timer = new Timer("updateBalanceTop()");
    
    Collection<Account> accounts = AccountManager.getAllAccounts();
    
    serverTotal = Number.format(accounts.stream()
      .mapToDouble(Account::getDefaultBalance)
      .sum(), Number.FormatType.COMMAS);
    orderedPlayerList = accounts.stream()
      .sorted(Comparator.comparingDouble(Account::getDefaultBalance).reversed())
      .toArray(Account[]::new);
    maxPage = (int) Math.ceil(orderedPlayerList.length / 10.0);
    
    timer.end();
  }
  
}
