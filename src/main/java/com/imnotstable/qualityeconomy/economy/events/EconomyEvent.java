package com.imnotstable.qualityeconomy.economy.events;

import com.imnotstable.qualityeconomy.economy.EconomicTransaction;
import com.imnotstable.qualityeconomy.economy.EconomicTransactionType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public class EconomyEvent extends Event implements Cancellable {
  
  private final EconomicTransaction transaction;
  private boolean cancelled;
  
  protected EconomyEvent(EconomicTransaction transaction, EconomicTransactionType requiredType) {
    super();
    this.transaction = transaction;
    if (!transaction.getType().equals(requiredType))
      throw new IllegalArgumentException("Transaction Type must be " + requiredType.name() + " found " + transaction.getType().name());
  }
  
  @Override
  public @NotNull HandlerList getHandlers() {
    throw new UnsupportedOperationException();
  }
  
}
