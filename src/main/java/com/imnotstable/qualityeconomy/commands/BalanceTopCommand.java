package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class BalanceTopCommand implements Command {
  
  public static List<Account> orderedPlayerList = new ArrayList<>();
  @Getter
  private final String name = "balancetop";
  private boolean isRegistered = false;
  private double serverTotal = 0;
  private int maxPage;
  private final CommandTree command = new CommandTree(name)
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
    if (isRegistered || !Configuration.isCommandEnabled("balancetop"))
      return;
    command.register();
    if (Configuration.getBalancetopInterval() != 0)
      taskID = Bukkit.getScheduler().runTaskTimerAsynchronously(QualityEconomy.getInstance(), this::updateBalanceTop, 0L, Configuration.getBalancetopInterval()).getTaskId();
    isRegistered = true;
  }
  
  public void unregister() {
    if (!isRegistered)
      return;
    CommandAPI.unregister(name, true);
    if (taskID != null) {
      Bukkit.getScheduler().cancelTask(taskID);
      taskID = null;
    }
    isRegistered = false;
  }
  
  private void viewBalanceTop(CommandSender sender, CommandArguments args) {
    int page = Math.min((int) args.getOrDefault("page", 1), maxPage);
    int startIndex = (page - 1) * 10;
    int endIndex = Math.min(startIndex + 10, orderedPlayerList.size());
    
    Messages.sendParsedMessage(MessageType.BALANCETOP_TITLE, new String[]{
      String.valueOf(maxPage),
      String.valueOf(page)
    }, sender);
    Messages.sendParsedMessage(MessageType.BALANCETOP_SERVER_TOTAL, new String[]{
      String.valueOf(serverTotal)
    }, sender);
    
    if (!orderedPlayerList.isEmpty())
      for (int i = startIndex; i < endIndex; i++) {
        Account account = orderedPlayerList.get(i);
        Messages.sendParsedMessage(MessageType.BALANCETOP_BALANCE_VIEW, new String[]{
          Number.formatCommas(account.getBalance()),
          String.valueOf(i + 1),
          account.getUsername()
        }, sender);
      }
    
    Messages.sendParsedMessage(MessageType.BALANCETOP_NEXT_PAGE, new String[]{
      args.fullInput().split(" ")[0].substring(1),
      String.valueOf(page + 1)
    }, sender);
  }
  
  private void updateBalanceTop() {
    Debug.Timer timer = new Debug.Timer("updateBalanceTop()");
    
    Collection<Account> accounts = AccountManager.getAllAccounts();
    
    serverTotal = accounts.stream()
      .mapToDouble(Account::getBalance)
      .sum();
    
    orderedPlayerList = accounts.stream()
      .sorted(Comparator.comparingDouble(Account::getBalance).reversed())
      .toList();
    
    maxPage = (int) Math.ceil(orderedPlayerList.size() / 10.0);
    timer.end();
  }
  
}
