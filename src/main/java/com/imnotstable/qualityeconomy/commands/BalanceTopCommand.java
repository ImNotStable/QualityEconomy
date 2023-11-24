package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.util.Number;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class BalanceTopCommand {
  
  private static boolean isRegistered = false;
  private static final CommandTree command = new CommandTree("balancetop")
    .withAliases("baltop")
    .then(new LiteralArgument("update")
      .withPermission("qualityeconomy.balancetop.update")
      .executes((sender, args) -> {updateBalanceTop();}))
    .then(new IntegerArgument("page", 1)
      .setOptional(true)
      .executes(BalanceTopCommand::viewBalanceTop));
  
  public static List<Account> orderedPlayerList = new ArrayList<>();
  private static double serverTotal = 0;
  private static int maxPage;
  private static int taskID;
  
  public static void register() {
    if (isRegistered || !Configuration.isBalancetopCommandEnabled())
      return;
    command.register();
    taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(QualityEconomy.getInstance(), BalanceTopCommand::updateBalanceTop, 0L, Configuration.getBalancetopInterval());
    isRegistered = true;
  }
  
  public static void unregister() {
    if (!isRegistered)
      return;
    CommandAPI.unregister("balancetop", true);
    Bukkit.getScheduler().cancelTask(taskID);
    isRegistered = false;
  }
  
  private static void viewBalanceTop(CommandSender sender, CommandArguments args) {
    int page = (int)  args.getOrDefault("page", 1);
    int startIndex = (page - 1) * 10;
    int endIndex = Math.min(startIndex + 10, orderedPlayerList.size());
    
    Messages.sendParsedMessage(MessageType.BALANCETOP_TITLE, new String[]{
      String.valueOf(maxPage),
      String.valueOf(page)
    }, sender);
    Messages.sendParsedMessage(MessageType.BALANCETOP_SERVER_TOTAL, new String[]{
      String.valueOf(serverTotal)
    }, sender);
    
    for (int i = startIndex; i < endIndex; i++) {
      Account account = orderedPlayerList.get(i);
      Messages.sendParsedMessage(MessageType.BALANCETOP_BALANCE_VIEW, new String[]{
        Number.formatCommas(account.getBalance()),
        String.valueOf(i + 1),
        account.getName()
      }, sender);
    }
    Messages.sendParsedMessage(MessageType.BALANCETOP_NEXT_PAGE, new String[]{
      args.fullInput().split(" ")[0].substring(1),
      String.valueOf(page + 1)
    }, sender);
  }
  
  public static void updateBalanceTop() {
    TestToolkit.Timer timer = new TestToolkit.Timer("Reloading balancetop...");
    
    Collection<Account> accounts = AccountManager.getAllAccounts();
    
    serverTotal = accounts.stream()
      .mapToDouble(Account::getBalance)
      .sum();
    
    orderedPlayerList = accounts.stream()
      .sorted(Comparator.comparing(Account::getBalance).reversed())
      .toList();
    
    maxPage = (int) Math.ceil(orderedPlayerList.size() / 10.0);
    timer.end("Reloaded balancetop");
  }
  
}
