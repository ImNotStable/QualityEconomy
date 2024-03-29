package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.economy.EconomicTransaction;
import com.imnotstable.qualityeconomy.economy.EconomicTransactionType;
import com.imnotstable.qualityeconomy.economy.EconomyPlayer;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

@Getter
public class CustomEconomyCommand extends BaseCommand {
  
  private final CommandTree command = new CommandTree("customeconomy")
    .withPermission("qualityeconomy.customeconomy")
    .withAliases("ceconomy", "customeco", "ceco")
    .withPermission("qualityeconomy.customeconomy")
    .then(CommandUtils.CurrencyArgument()
      .then(CommandUtils.TargetArgument(false)
        .then(new LiteralArgument("reset").executes(this::resetBalance))
        .then(new LiteralArgument("set").then(CommandUtils.AmountArgument().executes(this::setBalance)))
        .then(new LiteralArgument("add").then(CommandUtils.AmountArgument().executes(this::addBalance)))
        .then(new LiteralArgument("remove").then(CommandUtils.AmountArgument().executes(this::removeBalance)))));
  
  @SuppressWarnings("SimplifiableConditionalExpression")
  public void register() {
    super.register(command, (QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES ? !StorageManager.getActiveStorageType().getCurrencies().isEmpty() : false) && QualityEconomy.getQualityConfig().COMMANDS_CUSTOMECONOMY);
  }
  
  public void unregister() {
    super.unregister(command);
  }
  
  @SneakyThrows
  private void resetBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    EconomicTransaction.startNewTransaction(EconomicTransactionType.CUSTOM_BALANCE_RESET, sender, currency, 0, EconomyPlayer.of(target)).execute();
  }
  
  @SneakyThrows
  private void setBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    double balance = (double) args.get("amount");
    EconomicTransaction.startNewTransaction(EconomicTransactionType.CUSTOM_BALANCE_SET, sender, currency, balance, EconomyPlayer.of(target)).execute();
  }
  
  @SneakyThrows
  private void addBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    double balance = (double) args.get("amount");
    EconomicTransaction.startNewTransaction(EconomicTransactionType.CUSTOM_BALANCE_ADD, sender, currency, balance, EconomyPlayer.of(target)).execute();
  }
  
  @SneakyThrows
  private void removeBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    double balance = (double) args.get("amount");
    EconomicTransaction.startNewTransaction(EconomicTransactionType.CUSTOM_BALANCE_REMOVE, sender, currency, balance, EconomyPlayer.of(target)).execute();
  }
  
}
