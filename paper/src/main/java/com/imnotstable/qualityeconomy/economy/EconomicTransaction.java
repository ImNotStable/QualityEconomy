package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.QualityException;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public class EconomicTransaction {
  
  private final @NotNull EconomicTransactionType type;
  private final @NotNull CommandSender sender;
  private final @NotNull EconomyPlayer[] economyPlayers;
  private final double amount;
  private final @NotNull Currency currency;
  private boolean cancelled = false;
  private boolean silent = false;
  
  private EconomicTransaction(@NotNull EconomicTransactionType type, @NotNull CommandSender sender, @NotNull EconomyPlayer[] economyPlayers, @NotNull Currency currency, double amount) {
    this.type = type;
    this.sender = sender;
    this.economyPlayers = economyPlayers;
    this.currency = currency;
    this.amount = amount;
  }
  
  public static EconomicTransaction startNewTransaction(@NotNull EconomicTransactionType type, @NotNull CommandSender sender, @NotNull Currency currency, double amount, @NotNull EconomyPlayer... players) throws QualityException {
    if (type.getPlayerRequirement() != players.length)
      throw new QualityException("Economic Transaction failed to meet player requirement");
    return new EconomicTransaction(type, sender, players, currency, amount);
  }
  
  public void execute() {
    if (QualityEconomy.getQualityConfig().CUSTOM_EVENTS)
      cancelled = type.callEvent(this);
    type.execute(this);
  }
  
}
