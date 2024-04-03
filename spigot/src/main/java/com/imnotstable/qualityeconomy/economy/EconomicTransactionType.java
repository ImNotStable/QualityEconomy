package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.commands.RequestCommand;
import com.imnotstable.qualityeconomy.commands.WithdrawCommand;
import com.imnotstable.qualityeconomy.config.MessageType;
import com.imnotstable.qualityeconomy.config.Messages;
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
import com.imnotstable.qualityeconomy.util.Misc;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandAPI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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
    }, (transaction) -> String.format("$%s was transferred from %s to %s", Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[0].getUsername(), transaction.getEconomyPlayers()[1].getUsername())),
  CUSTOM_BALANCE_RESET(() -> QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES, CustomBalanceResetEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.setCustomBalance(target.getUniqueId(), transaction.getCurrency(), 0);
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_RESET,
          "player", target.getUsername());
    }, (transaction) -> String.format("%s's custom balance (%s) was reset by %s", transaction.getEconomyPlayers()[0].getUsername(), transaction.getCurrency(), transaction.getSender().getName())),
  CUSTOM_BALANCE_SET(() -> QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES, CustomBalanceSetEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.setCustomBalance(target.getUniqueId(), transaction.getCurrency(), transaction.getAmount());
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_SET,
          "balance", Number.format(transaction.getAmount(), Number.FormatType.COMMAS),
          "player", target.getUsername());
    }, (transaction) -> String.format("%s's custom balance (%s) was set to $%s by %s", transaction.getEconomyPlayers()[0].getUsername(), transaction.getCurrency(), Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getSender().getName())),
  CUSTOM_BALANCE_ADD(() -> QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES, CustomBalanceAddEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.addCustomBalance(target.getUniqueId(), transaction.getCurrency(), transaction.getAmount());
      if (transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_ADD,
          "balance", Number.format(transaction.getAmount(), Number.FormatType.COMMAS),
          "player", target.getUsername());
    }, (transaction) -> String.format("$%s was added to %s's custom balance (%s) by %s", Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[0].getUsername(), transaction.getCurrency(), transaction.getSender().getName())),
  CUSTOM_BALANCE_REMOVE(() -> QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES, CustomBalanceRemoveEvent::new,
    transaction -> {
      EconomyPlayer target = transaction.getEconomyPlayers()[0];
      QualityEconomyAPI.removeCustomBalance(target.getUniqueId(), transaction.getCurrency(), transaction.getAmount());
      if (!transaction.isSilent())
        Messages.sendParsedMessage(transaction.getSender(), MessageType.ECONOMY_REMOVE,
          "balance", Number.format(transaction.getAmount(), Number.FormatType.COMMAS),
          "player", target.getUsername());
    }, (transaction) -> String.format("$%s was removed from %s's custom balance (%s) by %s", Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[0].getUsername(), transaction.getCurrency(), transaction.getSender().getName())),
  REQUEST(() -> QualityEconomy.getQualityConfig().COMMANDS_REQUEST, 2, RequestEvent::new,
    transaction -> {
      Player requester = transaction.getEconomyPlayers()[0].getOfflineplayer().getPlayer();
      Player requestee = transaction.getEconomyPlayers()[1].getOfflineplayer().getPlayer();
      double amount = transaction.getAmount();
      if (!transaction.isSilent()) {
        Messages.sendParsedMessage(requester, MessageType.REQUEST_SEND,
          "amount", Number.format(amount, Number.FormatType.COMMAS),
          "requestee", requestee.getName());
        Messages.sendParsedMessage(requestee, MessageType.REQUEST_RECEIVE,
          "amount", Number.format(amount, Number.FormatType.COMMAS),
          "requester", requester.getName());
      }
      QualityEconomyAPI.createRequest(requester.getUniqueId(), requestee.getUniqueId(), amount);
      CommandAPI.updateRequirements(requestee);
      Bukkit.getScheduler().runTaskLaterAsynchronously(QualityEconomy.getInstance(), () -> {
        RequestCommand.getRequests().get(requestee.getUniqueId()).remove(requester.getUniqueId(), amount);
        CommandAPI.updateRequirements(requestee);
      }, 1200);
    }, (transaction) -> String.format("%s requested $%s from %s", transaction.getEconomyPlayers()[0].getUsername(), Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[1].getUsername())),
  REQUEST_ACCEPT(() -> QualityEconomy.getQualityConfig().COMMANDS_REQUEST, 2, RequestAcceptEvent::new,
    transaction -> {
      OfflinePlayer requester = transaction.getEconomyPlayers()[0].getOfflineplayer();
      Player requestee = transaction.getEconomyPlayers()[1].getOfflineplayer().getPlayer();
      double amount = transaction.getAmount();
      if (!transaction.isSilent()) {
        Messages.sendParsedMessage(requestee, MessageType.REQUEST_ACCEPT_RECEIVE,
          "amount", Number.format(amount, Number.FormatType.COMMAS),
          "requester", requester.getName());
        if (!requester.isOnline())
          Messages.sendParsedMessage(requester.getPlayer(), MessageType.REQUEST_ACCEPT_SEND,
            "amount", Number.format(amount, Number.FormatType.COMMAS),
            "requestee", requestee.getName());
      }
      QualityEconomyAPI.acceptRequest(requester.getUniqueId(), requestee.getUniqueId());
      CommandAPI.updateRequirements(requestee);
    }, (transaction) -> String.format("%s accepted %s's request for $%s", transaction.getEconomyPlayers()[1].getUsername(), transaction.getEconomyPlayers()[0].getUsername(), Number.format(transaction.getAmount(), Number.FormatType.COMMAS))),
  REQUEST_DENY(() -> QualityEconomy.getQualityConfig().COMMANDS_REQUEST, 2, RequestDenyEvent::new,
    transaction -> {
      OfflinePlayer requester = transaction.getEconomyPlayers()[0].getOfflineplayer();
      Player requestee = transaction.getEconomyPlayers()[1].getOfflineplayer().getPlayer();
      double amount = transaction.getAmount();
      if (!transaction.isSilent())
        Messages.sendParsedMessage(requestee, MessageType.REQUEST_DENY_RECEIVE,
          "amount", Number.format(amount, Number.FormatType.COMMAS),
          "requester", requester.getName());
      if (!transaction.isSilent() && requester.isOnline())
        Messages.sendParsedMessage(requester.getPlayer(), MessageType.REQUEST_DENY_SEND,
          "amount", Number.format(amount, Number.FormatType.COMMAS),
          "requestee", requestee.getName());
      QualityEconomyAPI.denyRequest(requester.getUniqueId(), requestee.getUniqueId());
      CommandAPI.updateRequirements(requestee);
    }, (transaction) -> String.format("%s denied %s's request for $%s", transaction.getEconomyPlayers()[1].getUsername(), transaction.getEconomyPlayers()[0].getUsername(), Number.format(transaction.getAmount(), Number.FormatType.COMMAS))),
  WITHDRAW(() -> QualityEconomy.getQualityConfig().BANKNOTES, false, WithdrawEvent::new,
    transaction -> {
      Player sender = transaction.getEconomyPlayers()[0].getOfflineplayer().getPlayer();
      double amount = transaction.getAmount();
      QualityEconomyAPI.removeBalance(sender.getUniqueId(), amount);
      sender.getInventory().addItem(WithdrawCommand.getBankNote(amount, sender));
      if (!transaction.isSilent())
        Messages.sendParsedMessage(sender, MessageType.WITHDRAW_MESSAGE,
          "amount", Number.format(amount, Number.FormatType.COMMAS));
    }, (transaction) -> String.format("$%s was withdrawn by %s", Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[0].getUsername())),
  WITHDRAW_CLAIM(() -> QualityEconomy.getQualityConfig().BANKNOTES, false, WithdrawClaimEvent::new,
    transaction -> {
      Player player = transaction.getEconomyPlayers()[0].getOfflineplayer().getPlayer();
      ItemStack itemStack = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
      PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
      if (!container.has(WithdrawCommand.getAmountKey(), PersistentDataType.DOUBLE) || !container.has(WithdrawCommand.getOwnerKey(), PersistentDataType.STRING))
        return;
      double amount = container.get(WithdrawCommand.getAmountKey(), PersistentDataType.DOUBLE);
      if (transaction.getAmount() > 0 && transaction.getAmount() != amount) {
        return;
      }
      QualityEconomyAPI.addBalance(player.getUniqueId(), amount);
      itemStack.setAmount(itemStack.getAmount() - 1);
      if (!transaction.isSilent())
        Messages.sendParsedMessage(player, MessageType.WITHDRAW_CLAIM,
          "amount", Number.format(amount, Number.FormatType.COMMAS),
          "player", container.get(WithdrawCommand.getOwnerKey(), PersistentDataType.STRING));
    }, (transaction) -> String.format("$%s was claimed by %s", Number.format(transaction.getAmount(), Number.FormatType.COMMAS), transaction.getEconomyPlayers()[0].getUsername()));
  
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
    EconomyEvent event = this.event.apply(transaction);
    Bukkit.getPluginManager().callEvent(event);
    return !event.isCancelled();
  }
  
  public void execute(EconomicTransaction transaction) {
    if (isAsync)
      Misc.runAsync(() -> {
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
