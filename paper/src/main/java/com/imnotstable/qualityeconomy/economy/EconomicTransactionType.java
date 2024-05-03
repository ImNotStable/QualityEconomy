package com.imnotstable.qualityeconomy.economy;

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
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@AllArgsConstructor
public enum EconomicTransactionType {
  
  BALANCE_RESET(BalanceRemoveEvent::new,
    (transaction) -> String.format("%s's balance was reset by %s", transaction.getEconomyPlayers()[0].getUsername(), transaction.getSender().getName())),
  BALANCE_SET(BalanceSetEvent::new,
    (transaction) -> String.format("%s's balance was set to $%s by %s", transaction.getEconomyPlayers()[0].getUsername(), transaction.getCurrency().getFormattedAmount(transaction.getAmount()), transaction.getSender().getName())),
  BALANCE_ADD(BalanceAddEvent::new,
    (transaction) -> String.format("$%s was added to %s's balance by %s", transaction.getCurrency().getFormattedAmount(transaction.getAmount()), transaction.getEconomyPlayers()[0].getUsername(), transaction.getSender().getName())),
  BALANCE_REMOVE(BalanceRemoveEvent::new,
    (transaction) -> String.format("$%s was removed from %s's balance by %s", transaction.getCurrency().getFormattedAmount(transaction.getAmount()), transaction.getEconomyPlayers()[0].getUsername(), transaction.getSender().getName())),
  BALANCE_TRANSFER(2, BalanceTransferEvent::new,
    (transaction) -> String.format("$%s was transferred from %s to %s", transaction.getCurrency().getFormattedAmount(transaction.getAmount()), transaction.getEconomyPlayers()[0].getUsername(), transaction.getEconomyPlayers()[1].getUsername()));
  
  @Getter
  private final int playerRequirement;
  private final Function<EconomicTransaction, EconomyEvent> event;
  private final Function<EconomicTransaction, String> logMessage;
  
  EconomicTransactionType(Function<EconomicTransaction, EconomyEvent> event, Function<EconomicTransaction, String> logMessage) {
    this.playerRequirement = 1;
    this.event = event;
    this.logMessage = logMessage;
  }
  
  public boolean callEvent(EconomicTransaction transaction) {
    return !event.apply(transaction).callEvent();
  }
  
  public CompletableFuture<Void> execute(EconomicTransaction transaction) {
    return CompletableFuture.runAsync(() -> executeTransaction(transaction));
  }
  
  private void executeTransaction(@NotNull EconomicTransaction transaction) {
    if (!transaction.isCancelled())
      switch (this) {
        case BALANCE_RESET -> {
          EconomyPlayer target = transaction.getEconomyPlayers()[0];
          Currency currency = transaction.getCurrency();
          QualityEconomyAPI.setBalance(target.getUniqueId(), currency.getName(), 0);
          if (!transaction.isSilent())
            Messages.sendParsedMessage(transaction.getSender(), currency.getMessage(MessageType.ADMIN_RESET),
              "player", target.getUsername());
        }
        case BALANCE_SET -> {
          EconomyPlayer target = transaction.getEconomyPlayers()[0];
          Currency currency = transaction.getCurrency();
          QualityEconomyAPI.setBalance(target.getUniqueId(), currency.getName(), transaction.getAmount());
          if (!transaction.isSilent())
            Messages.sendParsedMessage(transaction.getSender(), currency.getMessage(MessageType.ADMIN_SET),
              "balance", currency.getFormattedAmount(transaction.getAmount()),
              "player", target.getUsername());
        }
        case BALANCE_ADD -> {
          EconomyPlayer target = transaction.getEconomyPlayers()[0];
          Currency currency = transaction.getCurrency();
          QualityEconomyAPI.addBalance(target.getUniqueId(), currency.getName(), transaction.getAmount());
          if (!transaction.isSilent())
            Messages.sendParsedMessage(transaction.getSender(), currency.getMessage(MessageType.ADMIN_ADD),
              "balance", currency.getFormattedAmount(transaction.getAmount()),
              "player", target.getUsername());
        }
        case BALANCE_REMOVE -> {
          EconomyPlayer target = transaction.getEconomyPlayers()[0];
          Currency currency = transaction.getCurrency();
          QualityEconomyAPI.removeBalance(target.getUniqueId(), currency.getName(), transaction.getAmount());
          if (!transaction.isSilent())
            Messages.sendParsedMessage(transaction.getSender(), currency.getMessage(MessageType.ADMIN_REMOVE),
              "balance", currency.getFormattedAmount(transaction.getAmount()),
              "player", target.getUsername());
        }
        case BALANCE_TRANSFER -> {
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
        }
      }
    
    if (transaction.getCurrency().isTransactionLogging())
      TransactionLogger.log(transaction);
  }
  
  public String getLogMessage(EconomicTransaction transaction) {
    return transaction.getType().logMessage.apply(transaction);
  }
  
}
