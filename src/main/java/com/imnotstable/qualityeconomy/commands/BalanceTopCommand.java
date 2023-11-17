package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.Account;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.util.Number;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class BalanceTopCommand {
  
  public static List<Account> orderedPlayerList = new ArrayList<>();
  private static double serverTotal = 0;
  private static int maxPage;
  private static int taskID;
  
  public static void loadCommand() {
    taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(QualityEconomy.getInstance(), BalanceTopCommand::updateBalanceTop, 0L, Configuration.getBalancetopInterval());
    new CommandTree("balancetop")
      .withAliases("baltop")
      .then(new LiteralArgument("update")
        .withPermission("qualityeconomy.balancetop.update")
        .executes((sender, args) -> {
          updateBalanceTop();
        }))
      .then(new IntegerArgument("page", 1)
        .setOptional(true)
        .executes((sender, args) -> {
          int page = (int) args.getOrDefault("page", 1);
          int startIndex = (page - 1) * 10;
          int endIndex = Math.min(startIndex + 10, orderedPlayerList.size());
          
          Component titleMessage = MiniMessage.miniMessage().deserialize(Messages.getMessage(MessageType.BALANCETOP_TITLE),
            TagResolver.resolver("page", Tag.selfClosingInserting(Component.text(page))),
            TagResolver.resolver("maxpage", Tag.selfClosingInserting(Component.text(maxPage))));
          Component serverTotalMessage = MiniMessage.miniMessage().deserialize(Messages.getMessage(MessageType.BALANCETOP_SERVER_TOTAL),
            TagResolver.resolver("servertotal", Tag.selfClosingInserting(Component.text(Number.formatCommas(serverTotal)))));
          Component nextPageMessage = MiniMessage.miniMessage().deserialize(Messages.getMessage(MessageType.BALANCETOP_NEXT_PAGE),
            TagResolver.resolver("command", Tag.selfClosingInserting(Component.text("balancetop"))),
            TagResolver.resolver("nextpage", Tag.selfClosingInserting(Component.text((page + 1)))));
          
          sender.sendMessage(titleMessage);
          sender.sendMessage(serverTotalMessage);
          
          for (int i = startIndex; i < endIndex; i++) {
            Account account = orderedPlayerList.get(i);
            Component balanceMessage = MiniMessage.miniMessage().deserialize(Messages.getMessage(MessageType.BALANCETOP_BALANCE_VIEW),
              TagResolver.resolver("place", Tag.selfClosingInserting(Component.text(i + 1))),
              TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(account.getName()))),
              TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(account.getBalance())))));
            sender.sendMessage(balanceMessage);
          }
          sender.sendMessage(nextPageMessage);
        }))
      .register();
  }
  
  public static void unloadCommand() {
    Bukkit.getScheduler().cancelTask(taskID);
    CommandAPI.unregister("balancetop", true);
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
