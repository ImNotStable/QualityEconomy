package com.imnotstable.qualityeconomy.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MessageType {
  BALANCETOP_TITLE("balancetop.title"),
  BALANCETOP_SERVER_TOTAL("balancetop.server-total"),
  BALANCETOP_BALANCE_VIEW("balancetop.balance-view"),
  BALANCETOP_NEXT_PAGE("balancetop.next-page"),
  BALANCE_OWN_BALANCE("balance.own-balance", true),
  BALANCE_OTHER_BALANCE("balance.other-balance", true),
  ECONOMY_SET("economy.set", true),
  ECONOMY_ADD("economy.add", true),
  ECONOMY_REMOVE("economy.remove", true),
  ECONOMY_RESET("economy.reset", true),
  PAY_TOGGLE_ON("pay.toggle-on", true),
  PAY_TOGGLE_OFF("pay.toggle-off", true),
  PAY_SEND("pay.send", true),
  PAY_RECEIVE("pay.receive", true),
  PLAYER_NOT_FOUND("errors.player-not-found"),
  PLAYER_NOT_ONLINE("errors.player-not-online"),
  CURRENCY_NOT_FOUND("errors.currency-not-found"),
  SELF_NOT_ENOUGH_MONEY("errors.not-enough-money.self"),
  NOT_ACCEPTING_PAYMENTS("errors.not-accepting-payments"),
  INVALID_NUMBER("errors.invalid-number");
  
  private final String value;
  private final boolean currencyDependent;
  
  MessageType(String value) {
    this(value, false);
  }
  
}