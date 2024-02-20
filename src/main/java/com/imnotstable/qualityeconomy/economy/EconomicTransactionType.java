package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.commands.WithdrawCommand;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.economy.events.BalanceAddEvent;
import com.imnotstable.qualityeconomy.economy.events.BalanceRemoveEvent;
import com.imnotstable.qualityeconomy.economy.events.BalanceSetEvent;
import com.imnotstable.qualityeconomy.economy.events.BalanceTransferEvent;
import com.imnotstable.qualityeconomy.economy.events.CustomBalanceAddEvent;
import com.imnotstable.qualityeconomy.economy.events.CustomBalanceRemoveEvent;
import com.imnotstable.qualityeconomy.economy.events.CustomBalanceResetEvent;
import com.imnotstable.qualityeconomy.economy.events.CustomBalanceSetEvent;
import com.imnotstable.qualityeconomy.economy.events.EconomyEvent;
import com.imnotstable.qualityeconomy.economy.events.RequestAcceptEvent;
import com.imnotstable.qualityeconomy.economy.events.RequestDenyEvent;
import com.imnotstable.qualityeconomy.economy.events.RequestEvent;
import com.imnotstable.qualityeconomy.economy.events.WithdrawClaimEvent;
import com.imnotstable.qualityeconomy.economy.events.WithdrawEvent;
import com.imnotstable.qualityeconomy.util.Number;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
@Getter
public enum EconomicTransactionType {
  
  BALANCE_RESET(1,
    BalanceRemoveEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.setBalance(target.getUniqueId(), 0);
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_RESET,
          target.getUsername());
    }, (transaction) -> String.format("%s's balance was reset by %s", transaction.getEconomyPlayers()[0].getUsername(), transaction.getSender().getName())),
  BALANCE_SET(1,
    BalanceSetEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.setBalance(target.getUniqueId(), transaction.getAmount());
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_SET,
          Number.format(transaction.getAmount(), Number.FormatType.COMMAS), target.getUsername());
    }, (transaction) -> String.format("%s's balance was set to $%s by %s", transaction.getEconomyPlayers()[0].getUsername(), Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getSender().getName())),
  BALANCE_ADD(1,
    BalanceAddEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.addBalance(target.getUniqueId(), transaction.getAmount());
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_ADD,
          Number.format(transaction.getAmount(), Number.FormatType.COMMAS), target.getUsername());
    }, (transaction) -> String.format("$%s was added to %s's balance by %s", Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[0].getUsername(), transaction.getSender().getName())),
  BALANCE_REMOVE(1,
    BalanceRemoveEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.removeBalance(target.getUniqueId(), transaction.getAmount());
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_REMOVE,
          Number.format(transaction.getAmount(), Number.FormatType.COMMAS), target.getUsername());
    }, (transaction) -> String.format("$%s was removed from %s's balance by %s", Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[0].getUsername(), transaction.getSender().getName())),
  CUSTOM_BALANCE_RESET(1,
    CustomBalanceResetEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.setCustomBalance(target.getUniqueId(), transaction.getCurrency(), 0);
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_RESET,
          target.getUsername());
    }, (transaction) -> String.format("%s's custom balance (%s) was reset by %s", transaction.getEconomyPlayers()[0].getUsername(), transaction.getCurrency(), transaction.getSender().getName())),
  CUSTOM_BALANCE_SET(1,
    CustomBalanceSetEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.setCustomBalance(target.getUniqueId(), transaction.getCurrency(), transaction.getAmount());
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_SET,
          Number.format(transaction.getAmount(), Number.FormatType.COMMAS), target.getUsername());
    }, (transaction) -> String.format("%s's custom balance (%s) was set to $%s by %s", transaction.getEconomyPlayers()[0].getUsername(), transaction.getCurrency(), Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getSender().getName())),
  CUSTOM_BALANCE_ADD(1,
    CustomBalanceAddEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.addCustomBalance(target.getUniqueId(), transaction.getCurrency(), transaction.getAmount());
      if (transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_ADD,
          Number.format(transaction.getAmount(), Number.FormatType.COMMAS), target.getUsername());
    }, (transaction) -> String.format("$%s was added to %s's custom balance (%s) by %s", Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[0].getUsername(), transaction.getCurrency(), transaction.getSender().getName())),
  CUSTOM_BALANCE_REMOVE(1,
    CustomBalanceRemoveEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.removeCustomBalance(target.getUniqueId(), transaction.getCurrency(), transaction.getAmount());
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_REMOVE,
          Number.format(transaction.getAmount(), Number.FormatType.COMMAS), target.getUsername());
    }, (transaction) -> String.format("$%s was removed from %s's custom balance (%s) by %s", Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[0].getUsername(), transaction.getCurrency(), transaction.getSender().getName())),
  BALANCE_TRANSFER(2,
    BalanceTransferEvent::new,
    transaction -> {
      Player sender = transaction.getEconomyPlayers()[0].getOfflineplayer().getPlayer();
      EconomyPlayer target = transaction.getEconomyPlayers()[1];
      if (!transaction.isSilent())
        Messages.sendParsedMessage(sender, MessageType.PAY_SEND,
          Number.format(transaction.getAmount(), Number.FormatType.COMMAS),
          target.getUsername()
        );
      if (!transaction.isSilent() && target.getOfflineplayer().isOnline())
        Messages.sendParsedMessage(target.getOfflineplayer().getPlayer(), MessageType.PAY_RECEIVE,
          Number.format(transaction.getAmount(), Number.FormatType.COMMAS), sender.getName());
      QualityEconomyAPI.transferBalance(sender.getUniqueId(), target.getUniqueId(), transaction.getAmount());
    }, (transaction) -> String.format("$%s was transferred from %s to %s", Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[0].getUsername(), transaction.getEconomyPlayers()[1].getUsername())),
  REQUEST(2,
    RequestEvent::new,
    transaction -> {
      Player requester = transaction.getEconomyPlayers()[0].getOfflineplayer().getPlayer();
      Player requestee = transaction.getEconomyPlayers()[1].getOfflineplayer().getPlayer();
      double amount = transaction.getAmount();
      Messages.sendParsedMessage(requester, MessageType.REQUEST_SEND,
        Number.format(amount, Number.FormatType.COMMAS), requestee.getName());
      Messages.sendParsedMessage(requestee, MessageType.REQUEST_RECEIVE,
        Number.format(amount, Number.FormatType.COMMAS), requester.getName());
      QualityEconomyAPI.createRequest(requester.getUniqueId(), requestee.getUniqueId(), amount);
    }, (transaction) -> String.format("%s requested $%s from %s", transaction.getEconomyPlayers()[0].getUsername(), Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[1].getUsername())),
  REQUEST_ACCEPT(2,
    RequestAcceptEvent::new,
    transaction -> {
      Player requester = transaction.getEconomyPlayers()[0].getOfflineplayer().getPlayer();
      Player requestee = transaction.getEconomyPlayers()[1].getOfflineplayer().getPlayer();
      double amount = transaction.getAmount();
      if (!transaction.isSilent())
        Messages.sendParsedMessage(requestee, MessageType.REQUEST_ACCEPT_SEND,
          Number.format(amount, Number.FormatType.COMMAS),
          requester.getName()
        );
      if (!transaction.isSilent() && requester.isOnline())
        Messages.sendParsedMessage(requestee, MessageType.REQUEST_ACCEPT_RECEIVE,
          Number.format(amount, Number.FormatType.COMMAS), requestee.getName());
      QualityEconomyAPI.answerRequest(requester.getUniqueId(), requestee.getUniqueId(), true);
    }, (transaction) -> String.format("%s accepted %s's request for $%s", transaction.getEconomyPlayers()[1].getUsername(), transaction.getEconomyPlayers()[0].getUsername(), Number.format(transaction.getAmount(), Number.FormatType.COMMAS))),
  REQUEST_DENY(2,
    RequestDenyEvent::new,
    transaction -> {
      Player requester = transaction.getEconomyPlayers()[0].getOfflineplayer().getPlayer();
      Player requestee = transaction.getEconomyPlayers()[1].getOfflineplayer().getPlayer();
      double amount = transaction.getAmount();
      if (!transaction.isSilent())
        Messages.sendParsedMessage(requestee, MessageType.REQUEST_DENY_SEND,
          Number.format(amount, Number.FormatType.COMMAS), requester.getName());
      if (!transaction.isSilent() && requester.isOnline())
        Messages.sendParsedMessage(requestee, MessageType.REQUEST_DENY_RECEIVE,
          Number.format(amount, Number.FormatType.COMMAS), requestee.getName());
      QualityEconomyAPI.answerRequest(requester.getUniqueId(), requestee.getUniqueId(), false);
    }, (transaction) -> String.format("%s denied %s's request for $%s", transaction.getEconomyPlayers()[1].getUsername(), transaction.getEconomyPlayers()[0].getUsername(), Number.format(transaction.getAmount(), Number.FormatType.COMMAS))),
  WITHDRAW(1,
    WithdrawEvent::new,
    transaction -> {
      Player sender = transaction.getEconomyPlayers()[0].getOfflineplayer().getPlayer();
      double amount = transaction.getAmount();
      QualityEconomyAPI.removeBalance(sender.getUniqueId(), amount);
      sender.getInventory().addItem(WithdrawCommand.getBankNote(amount, sender));
      if (!transaction.isSilent())
        Messages.sendParsedMessage(sender, MessageType.WITHDRAW_MESSAGE,
          Number.format(amount, Number.FormatType.COMMAS)
        );
    }, (transaction) -> String.format("$%s was withdrawn by %s", Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[0].getUsername())),
  WITHDRAW_CLAIM(1,
    WithdrawClaimEvent::new,
    transaction -> {
      Player player = transaction.getEconomyPlayers()[0].getOfflineplayer().getPlayer();
      ItemStack itemStack = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
      PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
      if (!container.has(WithdrawCommand.getAmountKey()) || !container.has(WithdrawCommand.getOwnerKey()))
        return;
      double amount = container.get(WithdrawCommand.getAmountKey(), PersistentDataType.DOUBLE);
      if (transaction.getAmount() > 0 && transaction.getAmount() != amount) {
        return;
      }
      QualityEconomyAPI.addBalance(player.getUniqueId(), amount);
      itemStack.subtract();
      if (!transaction.isSilent())
        Messages.sendParsedMessage(player, MessageType.WITHDRAW_CLAIM,
          Number.format(amount, Number.FormatType.COMMAS), container.get(WithdrawCommand.getOwnerKey(), PersistentDataType.STRING));
    }, (transaction) -> String.format("$%s was claimed by %s from %s", Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[0].getUsername(), transaction.getEconomyPlayers()[1].getUsername()));
  
  private final int playerRequirement;
  private final Function<EconomicTransaction, EconomyEvent> event;
  private final Consumer<EconomicTransaction> executor;
  private final Function<EconomicTransaction, String> logMessage;
  
  public boolean callEvent(EconomicTransaction transaction) {
    EconomyEvent event = this.event.apply(transaction);
    Bukkit.getPluginManager().callEvent(event);
    return event.isCancelled();
  }
  
}
