package com.imnotstable.qualityeconomy.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum MessageType {
  BALANCETOP_TITLE("balancetop.title", new String[]{"maxpage", "page"}),
  BALANCETOP_SERVER_TOTAL("balancetop.server-total", new String[]{"servertotal"}),
  BALANCETOP_BALANCE_VIEW("balancetop.balance-view", new String[]{"balance", "place", "player"}),
  BALANCETOP_NEXT_PAGE("balancetop.next-page", new String[]{"command", "nextpage"}),
  BALANCE_OWN_BALANCE("balance.own-balance", new String[]{"balance"}),
  BALANCE_OTHER_BALANCE("balance.other-balance", new String[]{"balance", "player"}),
  ECONOMY_SET("economy.set", new String[]{"balance", "player"}),
  ECONOMY_ADD("economy.add", new String[]{"balance", "player"}),
  ECONOMY_REMOVE("economy.remove", new String[]{"balance", "player"}),
  ECONOMY_RESET("economy.reset", new String[]{"player"}),
  PAY_TOGGLE_ON("pay.toggle-on", new String[]{}),
  PAY_TOGGLE_OFF("pay.toggle-off", new String[]{}),
  PAY_SEND("pay.send", new String[]{"amount", "receiver"}),
  PAY_RECEIVE("pay.receive", new String[]{"amount", "sender"}),
  REQUEST_TOGGLE_ON("request.toggle-on", new String[]{}),
  REQUEST_TOGGLE_OFF("request.toggle-off", new String[]{}),
  REQUEST_SEND("request.send", new String[]{"amount", "requestee"}),
  REQUEST_RECEIVE("request.receive", new String[]{"amount", "requester"}),
  REQUEST_ACCEPT_SEND("request.accept-send", new String[]{"amount", "requestee"}),
  REQUEST_ACCEPT_RECEIVE("request.accept-receive", new String[]{"amount", "requester"}),
  REQUEST_DENY_SEND("request.deny-send", new String[]{"amount", "requestee"}),
  REQUEST_DENY_RECEIVE("request.deny-receive", new String[]{"amount", "requester"}),
  WITHDRAW("withdraw.withdraw", new String[]{"amount"}),
  PLAYER_NOT_FOUND("errors.player-not-found", new String[0]),
  PLAYER_NOT_ONLINE("errors.player-not-online", new String[0]),
  CURRENCY_NOT_FOUND("errors.currency-not-found", new String[0]),
  SELF_NOT_ENOUGH_MONEY("errors.not-enough-money.self", new String[0]),
  OTHER_NOT_ENOUGH_MONEY("errors.not-enough-money.other", new String[0]),
  NOT_ACCEPTING_PAYMENTS("errors.not-accepting-payments", new String[0]),
  NOT_ACCEPTING_REQUESTS("errors.not-accepting-requests", new String[0]);
  
  @Getter
  private final String value;
  @Getter
  private final String[] tags;
  
}