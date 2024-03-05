package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.economy.EconomicTransaction;
import com.imnotstable.qualityeconomy.economy.EconomicTransactionType;
import com.imnotstable.qualityeconomy.economy.EconomyPlayer;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.SneakyThrows;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class EconomyCommand extends BaseCommand {
  
  private final CommandTree command = new CommandTree("economy")
    .withPermission("qualityeconomy.economy")
    .withAliases("eco")
    .then(CommandUtils.TargetArgument(false)
      .then(new LiteralArgument("reset").executes(this::resetBalance))
      .then(new LiteralArgument("set").then(CommandUtils.AmountArgument().executes(this::setBalance)))
      .then(new LiteralArgument("add").then(CommandUtils.AmountArgument().executes(this::addBalance)))
      .then(new LiteralArgument("remove").then(CommandUtils.AmountArgument().executes(this::removeBalance))));
  
  public void register() {
    super.register(command);
  }
  
  public void unregister() {
    super.unregister(command);
  }
  
  @SneakyThrows
  private void resetBalance(CommandSender sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    EconomicTransaction.startNewTransaction(EconomicTransactionType.BALANCE_RESET, sender, 0, EconomyPlayer.of(target)).execute();
  }
  
  @SneakyThrows
  private void setBalance(CommandSender sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    double balance = (double) args.get("amount");
    EconomicTransaction.startNewTransaction(EconomicTransactionType.BALANCE_SET, sender, balance, EconomyPlayer.of(target)).execute();
  }
  
  @SneakyThrows
  private void addBalance(CommandSender sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    double balance = (double) args.get("amount");
    EconomicTransaction.startNewTransaction(EconomicTransactionType.BALANCE_ADD, sender, balance, EconomyPlayer.of(target)).execute();
  }
  
  @SneakyThrows
  private void removeBalance(CommandSender sender, CommandArguments args) {
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    double balance = (double) args.get("amount");
    EconomicTransaction.startNewTransaction(EconomicTransactionType.BALANCE_REMOVE, sender, balance, EconomyPlayer.of(target)).execute();
  }
  
}
