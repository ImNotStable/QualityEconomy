package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.config.MessageType;
import com.imnotstable.qualityeconomy.config.Messages;
import com.imnotstable.qualityeconomy.economy.events.BalanceAddEvent;
import com.imnotstable.qualityeconomy.economy.events.BalanceRemoveEvent;
import com.imnotstable.qualityeconomy.economy.events.BalanceSetEvent;
import com.imnotstable.qualityeconomy.economy.events.BalanceTransferEvent;
import com.imnotstable.qualityeconomy.economy.events.EconomyEvent;
import com.imnotstable.qualityeconomy.util.Number;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor
public enum EconomicTransactionType {
  
  BALANCE_RESET(() -> true, BalanceRemoveEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.setBalance(target.getUniqueId(), 0);
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_RESET,
          "player", target.getUsername());
    }, (transaction) -> String.format("%s's balance was reset by %s", transaction.getEconomyPlayers()[0].getUsername(), transaction.getSender().getName())),
  BALANCE_SET(() -> true, BalanceSetEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.setBalance(target.getUniqueId(), transaction.getAmount());
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_SET,
          "balance", Number.format(transaction.getAmount(), Number.FormatType.COMMAS),
          "player", target.getUsername());
    }, (transaction) -> String.format("%s's balance was set to $%s by %s", transaction.getEconomyPlayers()[0].getUsername(), Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getSender().getName())),
  BALANCE_ADD(() -> true, BalanceAddEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.addBalance(target.getUniqueId(), transaction.getAmount());
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_ADD,
          "balance", Number.format(transaction.getAmount(), Number.FormatType.COMMAS),
          "player", target.getUsername());
    }, (transaction) -> String.format("$%s was added to %s's balance by %s", Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[0].getUsername(), transaction.getSender().getName())),
  BALANCE_REMOVE(() -> true, BalanceRemoveEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.removeBalance(target.getUniqueId(), transaction.getAmount());
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_REMOVE,
          "balance", Number.format(transaction.getAmount(), Number.FormatType.COMMAS),
          "player", target.getUsername());
    }, (transaction) -> String.format("$%s was removed from %s's balance by %s", Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[0].getUsername(), transaction.getSender().getName())),
  BALANCE_TRANSFER(() -> true, 2, BalanceTransferEvent::new,
    transaction -> {
      Player sender = transaction.getEconomyPlayers()[0].getOfflineplayer().getPlayer();
      EconomyPlayer target = transaction.getEconomyPlayers()[1];
      if (!transaction.isSilent())
        Messages.sendParsedMessage(sender, MessageType.PAY_SEND,
          "amount", Number.format(transaction.getAmount(), Number.FormatType.COMMAS),
          "receiver", target.getUsername()
        );
      if (!transaction.isSilent() && target.getOfflineplayer().isOnline())
        Messages.sendParsedMessage(target.getOfflineplayer().getPlayer(), MessageType.PAY_RECEIVE,
          "amount", Number.format(transaction.getAmount(), Number.FormatType.COMMAS),
          "sender", sender.getName());
      QualityEconomyAPI.transferBalance(sender.getUniqueId(), target.getUniqueId(), transaction.getAmount());
    }, (transaction) -> String.format("$%s was transferred from %s to %s", Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[0].getUsername(), transaction.getEconomyPlayers()[1].getUsername()));
  
  @Getter
  private final Supplier<Boolean> configurationRequirement;
  @Getter
  private final int playerRequirement;
  private final boolean isAsync;
  private final Function<EconomicTransaction, EconomyEvent> event;
  private final Consumer<EconomicTransaction> executor;
  @Getter
  private final Function<EconomicTransaction, String> logMessage;
  
  EconomicTransactionType(Supplier<Boolean> configurationRequirement, int playerRequirement, Function<EconomicTransaction, EconomyEvent> event, Consumer<EconomicTransaction> executor, Function<EconomicTransaction, String> logMessage) {
    this.configurationRequirement = configurationRequirement;
    this.playerRequirement = playerRequirement;
    this.isAsync = true;
    this.event = event;
    this.executor = executor;
    this.logMessage = logMessage;
  }
  
  EconomicTransactionType(Supplier<Boolean> configurationRequirement, boolean isAsync, Function<EconomicTransaction, EconomyEvent> event, Consumer<EconomicTransaction> executor, Function<EconomicTransaction, String> logMessage) {
    this.configurationRequirement = configurationRequirement;
    this.playerRequirement = 1;
    this.isAsync = isAsync;
    this.event = event;
    this.executor = executor;
    this.logMessage = logMessage;
  }
  
  EconomicTransactionType(Supplier<Boolean> configurationRequirement, Function<EconomicTransaction, EconomyEvent> event, Consumer<EconomicTransaction> executor, Function<EconomicTransaction, String> logMessage) {
    this.configurationRequirement = configurationRequirement;
    this.playerRequirement = 1;
    this.isAsync = true;
    this.event = event;
    this.executor = executor;
    this.logMessage = logMessage;
  }
  
  public boolean callEvent(EconomicTransaction transaction) {
    return !event.apply(transaction).callEvent();
  }
  
  public void execute(EconomicTransaction transaction) {
    if (isAsync)
      CompletableFuture.runAsync(() -> {
        if (!transaction.isCancelled())
          executor.accept(transaction);
        
        if (QualityEconomy.getQualityConfig().TRANSACTION_LOGGING)
          TransactionLogger.log(transaction);
      });
    else {
      if (!transaction.isCancelled())
        executor.accept(transaction);
      
      if (QualityEconomy.getQualityConfig().TRANSACTION_LOGGING)
        TransactionLogger.log(transaction);
    }
  }
  
}
