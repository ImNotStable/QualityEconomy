package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.Account;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.util.Number;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class BalanceTopCommand implements TabExecutor {

  public static List<Account> orderedPlayerList = new ArrayList<>();
  private static double serverTotal = 0;
  private static int maxPage;

  public static void initScheduler() {
    Bukkit.getScheduler().scheduleSyncRepeatingTask(QualityEconomy.getInstance(), BalanceTopCommand::updateBalanceTop, 0L, 300L);
  }

  public static void updateBalanceTop() {
    TestToolkit.Timer timer = new TestToolkit.Timer("Reloading balancetop...");

    Collection<Account> accounts = AccountManager.getAllAccounts();

    serverTotal = accounts.parallelStream()
        .mapToDouble(Account::getBalance)
        .sum();

    orderedPlayerList = accounts.parallelStream()
        .sorted(Comparator.comparing(Account::getBalance).reversed())
        .toList();

    maxPage = (int) Math.ceil(orderedPlayerList.size() / 10.0);
    timer.end("Reloaded balancetop");
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    int page = 1;

    if (args.length == 1) {
      if (args[0].equals("update") && sender.hasPermission("command.balancetop.update")) {
        updateBalanceTop();
        return true;
      }
      page = parsePageNumber(args[0]);
    }

    int startIndex = (page - 1) * 10;
    int endIndex = Math.min(startIndex + 10, orderedPlayerList.size());

    Component titleMessage = MiniMessage.miniMessage().deserialize(Messages.getMessage("balancetop.title"),
        TagResolver.resolver("page", Tag.selfClosingInserting(Component.text(page))),
        TagResolver.resolver("maxpage", Tag.selfClosingInserting(Component.text(maxPage))));
    Component serverTotalMessage = MiniMessage.miniMessage().deserialize(Messages.getMessage("balancetop.server-total"),
        TagResolver.resolver("servertotal", Tag.selfClosingInserting(Component.text(Number.formatCommas(serverTotal)))));
    Component nextPageMessage = MiniMessage.miniMessage().deserialize(Messages.getMessage("balancetop.next-page"),
      TagResolver.resolver("command", Tag.selfClosingInserting(Component.text(label))),
      TagResolver.resolver("nextpage", Tag.selfClosingInserting(Component.text((page + 1)))));

    sender.sendMessage(titleMessage);
    sender.sendMessage(serverTotalMessage);

    for (int i = startIndex; i < endIndex; i++) {
      Account account = orderedPlayerList.get(i);
      Component balanceMessage = MiniMessage.miniMessage().deserialize(Messages.getMessage("balancetop.balance-view"),
        TagResolver.resolver("place", Tag.selfClosingInserting(Component.text(i + 1))),
        TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(account.getName()))),
        TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(account.getBalance())))));
      sender.sendMessage(balanceMessage);
    }
    sender.sendMessage(nextPageMessage);
    return true;
  }

  private int parsePageNumber(String page) {
    try {
      return Math.max(Math.min(Integer.parseInt(page), maxPage), 1);
    } catch (NumberFormatException ignored) {
      return 1;
    }
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    List<String> completions = new ArrayList<>();
    completions.add("page");
    if (sender.hasPermission("qualityeconomy.admin")) {
      completions.add("update");
    }
    return completions;
  }
}
