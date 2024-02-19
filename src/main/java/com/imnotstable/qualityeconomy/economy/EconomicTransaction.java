package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.util.QualityException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Setter
@Getter
public class EconomicTransaction {
  
  private final @NotNull EconomicTransactionType type;
  private final @Nullable CommandSender sender;
  private final @NotNull EconomyPlayer[] economyPlayers;
  private final double amount;
  private final @Nullable String currency;
  private boolean cancelled = false;
  private boolean silent = false;
  
  private EconomicTransaction(@NotNull EconomicTransactionType type, @Nullable CommandSender sender, @NotNull EconomyPlayer[] economyPlayers, @Nullable String currency, double amount) {
    this.type = type;
    this.sender = sender;
    this.economyPlayers = economyPlayers;
    this.currency = currency;
    this.amount = amount;
  }
  
  public static EconomicTransaction startNewTransaction(EconomicTransactionType type, CommandSender sender, String currency, double amount, EconomyPlayer... players) throws QualityException {
    if (type.getPlayerRequirement() != players.length)
      throw new QualityException("Economic Transaction failed to meet player requirement");
    return new EconomicTransaction(type, sender, players, currency, amount);
  }
  
  public static EconomicTransaction startNewTransaction(EconomicTransactionType type, CommandSender sender, double amount, EconomyPlayer... players) throws QualityException {
    return startNewTransaction(type, sender, null, amount, players);
  }
  
  public static EconomicTransaction startNewTransaction(EconomicTransactionType type, String currency, double amount, EconomyPlayer... players) throws QualityException {
    return startNewTransaction(type, null, currency, amount, players);
  }
  
  public static EconomicTransaction startNewTransaction(EconomicTransactionType type, double amount, EconomyPlayer... players) throws QualityException {
    return startNewTransaction(type, null, null, amount, players);
  }
  
  public void execute() { // This will be used to call the custom events that are planned.
    if (cancelled)
      return;
    type.getExecutor().accept(this);
  }
  
}
