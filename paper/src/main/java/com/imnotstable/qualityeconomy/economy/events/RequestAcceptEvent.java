package com.imnotstable.qualityeconomy.economy.events;

import com.imnotstable.qualityeconomy.economy.EconomicTransaction;
import com.imnotstable.qualityeconomy.economy.EconomicTransactionType;
import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RequestAcceptEvent extends EconomyEvent {
  
  @Getter
  private static final HandlerList handlerList = new HandlerList();
  
  public RequestAcceptEvent(EconomicTransaction transaction) {
    super(transaction, EconomicTransactionType.REQUEST_ACCEPT);
  }
  
  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
  
}
