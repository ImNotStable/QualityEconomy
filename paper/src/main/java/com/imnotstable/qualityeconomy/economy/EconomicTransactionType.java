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
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
public enum EconomicTransactionType {
  
  BALANCE_RESET(BalanceRemoveEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      Currency currency = transaction.getCurrency();
      QualityEconomyAPI.setBalance(target.getUniqueId(), currency.getName(), 0);
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), currency.getMessage(MessageType.ADMIN_RESET),
          "player", target.getUsername());
    }, (transaction) -> String.format("%s's balance was reset by %s", transaction.getEconomyPlayers()[0].getUsername(), transaction.getSender().getName())),
  BALANCE_SET(BalanceSetEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      Currency currency = transaction.getCurrency();
      QualityEconomyAPI.setBalance(target.getUniqueId(), currency.getName(), transaction.getAmount());
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), currency.getMessage(MessageType.ADMIN_SET),
          "balance", currency.getFormattedAmount(transaction.getAmount()),
          "player", target.getUsername());
    }, (transaction) -> String.format("%s's balance was set to $%s by %s", transaction.getEconomyPlayers()[0].getUsername(), transaction.getCurrency().getFormattedAmount(transaction.getAmount()), transaction.getSender().getName())),
  BALANCE_ADD(BalanceAddEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      Currency currency = transaction.getCurrency();
      QualityEconomyAPI.addBalance(target.getUniqueId(), currency.getName(), transaction.getAmount());
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), currency.getMessage(MessageType.ADMIN_ADD),
          "balance", currency.getFormattedAmount(transaction.getAmount()),
          "player", target.getUsername());
    }, (transaction) -> String.format("$%s was added to %s's balance by %s", transaction.getCurrency().getFormattedAmount(transaction.getAmount()), transaction.getEconomyPlayers()[0].getUsername(), transaction.getSender().getName())),
  BALANCE_REMOVE(BalanceRemoveEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      Currency currency = transaction.getCurrency();
      QualityEconomyAPI.removeBalance(target.getUniqueId(), currency.getName(), transaction.getAmount());
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), currency.getMessage(MessageType.ADMIN_REMOVE),
          "balance", currency.getFormattedAmount(transaction.getAmount()),
          "player", target.getUsername());
    }, (transaction) -> String.format("$%s was removed from %s's balance by %s", transaction.getCurrency().getFormattedAmount(transaction.getAmount()), transaction.getEconomyPlayers()[0].getUsername(), transaction.getSender().getName())),
  BALANCE_TRANSFER(2, BalanceTransferEvent::new,
    transaction -> {
      Player sender = transaction.getEconomyPlayers()[0].getOfflineplayer().getPlayer();
      EconomyPlayer target = transaction.getEconomyPlayers()[1];
      Currency currency = transaction.getCurrency();
      if (!transaction.isSilent())
        Messages.sendParsedMessage(sender, currency.getMessage(MessageType.TRANSFER_SEND),
          "amount", currency.getFormattedAmount(transaction.getAmount()),
          "receiver", target.getUsername()
        );
      if (!transaction.isSilent() && target.getOfflineplayer().isOnline())
        Messages.sendParsedMessage(target.getOfflineplayer().getPlayer(), currency.getMessage(MessageType.TRANSFER_RECEIVE),
          "amount", currency.getFormattedAmount(transaction.getAmount()),
          "sender", sender.getName());
      QualityEconomyAPI.transferBalance(sender.getUniqueId(), target.getUniqueId(), currency.getName(), transaction.getAmount());
    }, (transaction) -> String.format("$%s was transferred from %s to %s", transaction.getCurrency().getFormattedAmount(transaction.getAmount()), transaction.getEconomyPlayers()[0].getUsername(), transaction.getEconomyPlayers()[1].getUsername()));
  
  @Getter
  private final int playerRequirement;
  private final boolean isAsync;
  private final Function<EconomicTransaction, EconomyEvent> event;
  private final Consumer<EconomicTransaction> executor;
  private final Function<EconomicTransaction, String> logMessage;
  
  EconomicTransactionType(int playerRequirement, Function<EconomicTransaction, EconomyEvent> event, Consumer<EconomicTransaction> executor, Function<EconomicTransaction, String> logMessage) {
    this.playerRequirement = playerRequirement;
    this.isAsync = true;
    this.event = event;
    this.executor = executor;
    this.logMessage = logMessage;
  }
  
  EconomicTransactionType(Function<EconomicTransaction, EconomyEvent> event, Consumer<EconomicTransaction> executor, Function<EconomicTransaction, String> logMessage) {
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
  
  public String getLogMessage(EconomicTransaction transaction) {
    return transaction.getType().logMessage.apply(transaction);
  }
  
}
