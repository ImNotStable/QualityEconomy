package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.commands.WithdrawCommand;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.util.Number;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.Consumer;

@AllArgsConstructor
@Getter
public enum EconomicTransactionType {
  
  ECONOMY_RESET(1, transaction -> {
    EconomyPlayer target = transaction.getEconomyPlayers()[0];
    QualityEconomyAPI.setBalance(target.getUniqueId(), 0);
    if (!transaction.isSilent())
      Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_RESET,
        target.getUsername());
  }),
  ECONOMY_SET(1, transaction -> {
    EconomyPlayer target = transaction.getEconomyPlayers()[0];
    QualityEconomyAPI.setBalance(target.getUniqueId(), transaction.getAmount());
    if (!transaction.isSilent())
      Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_SET,
        Number.format(transaction.getAmount(), Number.FormatType.COMMAS), target.getUsername());
  }),
  ECONOMY_ADD(1, transaction -> {
    EconomyPlayer target = transaction.getEconomyPlayers()[0];
    QualityEconomyAPI.addBalance(target.getUniqueId(), transaction.getAmount());
    if (!transaction.isSilent())
      Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_ADD,
        Number.format(transaction.getAmount(), Number.FormatType.COMMAS), target.getUsername());
  }),
  ECONOMY_REMOVE(1, transaction -> {
    EconomyPlayer target = transaction.getEconomyPlayers()[0];
    QualityEconomyAPI.removeBalance(target.getUniqueId(), transaction.getAmount());
    if (!transaction.isSilent())
      Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_REMOVE,
        Number.format(transaction.getAmount(), Number.FormatType.COMMAS), target.getUsername());
  }),
  CUSTOM_ECONOMY_RESET(1, transaction -> {
    EconomyPlayer target = transaction.getEconomyPlayers()[0];
    QualityEconomyAPI.setCustomBalance(target.getUniqueId(), transaction.getCurrency(), 0);
    if (!transaction.isSilent())
      Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_RESET,
        target.getUsername());
  }),
  CUSTOM_ECONOMY_SET(1, transaction -> {
    EconomyPlayer target = transaction.getEconomyPlayers()[0];
    QualityEconomyAPI.setCustomBalance(target.getUniqueId(), transaction.getCurrency(), transaction.getAmount());
    if (!transaction.isSilent())
      Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_SET,
        Number.format(transaction.getAmount(), Number.FormatType.COMMAS), target.getUsername());
  }),
  CUSTOM_ECONOMY_ADD(1, transaction -> {
    EconomyPlayer target = transaction.getEconomyPlayers()[0];
    QualityEconomyAPI.addCustomBalance(target.getUniqueId(), transaction.getCurrency(), transaction.getAmount());
    if (transaction.isSilent())
      Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_ADD,
        Number.format(transaction.getAmount(), Number.FormatType.COMMAS), target.getUsername());
  }),
  CUSTOM_ECONOMY_REMOVE(1, transaction -> {
    EconomyPlayer target = transaction.getEconomyPlayers()[0];
    QualityEconomyAPI.removeCustomBalance(target.getUniqueId(), transaction.getCurrency(), transaction.getAmount());
    if (!transaction.isSilent())
      Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_REMOVE,
        Number.format(transaction.getAmount(), Number.FormatType.COMMAS), target.getUsername());
  }),
  BALANCE_TRANSFER(2, transaction -> {
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
  }),
  REQUEST(2, transaction -> {
    Player requester = transaction.getEconomyPlayers()[0].getOfflineplayer().getPlayer();
    Player requestee = transaction.getEconomyPlayers()[1].getOfflineplayer().getPlayer();
    double amount = transaction.getAmount();
    Messages.sendParsedMessage(requester, MessageType.REQUEST_SEND,
      Number.format(amount, Number.FormatType.COMMAS), requestee.getName());
    Messages.sendParsedMessage(requestee, MessageType.REQUEST_RECEIVE,
      Number.format(amount, Number.FormatType.COMMAS), requester.getName());
    QualityEconomyAPI.createRequest(requester.getUniqueId(), requestee.getUniqueId(), amount);
  }),
  REQUEST_ACCEPT(2, transaction -> {
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
  }),
  REQUEST_DENY(2, transaction -> {
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
  }),
  WITHDRAW(1, transaction -> {
    Player sender = transaction.getEconomyPlayers()[0].getOfflineplayer().getPlayer();
    double amount = transaction.getAmount();
      QualityEconomyAPI.removeBalance(sender.getUniqueId(), amount);
      sender.getInventory().addItem(WithdrawCommand.getBankNote(amount, sender));
      if (!transaction.isSilent())
        Messages.sendParsedMessage(sender, MessageType.WITHDRAW_MESSAGE,
          Number.format(amount, Number.FormatType.COMMAS)
        );
  }),
  WITHDRAW_CLAIM(1, transaction -> {
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
  });
  
  private final int playerRequirement;
  private final Consumer<EconomicTransaction> executor;
  //private final Consumer<EconomicTransaction> eventExecutor;
  
}
