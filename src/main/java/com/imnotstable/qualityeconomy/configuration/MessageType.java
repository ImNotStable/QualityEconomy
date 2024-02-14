package com.imnotstable.qualityeconomy.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
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
  PAY_TOGGLE_ON("pay.toggle-on"),
  PAY_TOGGLE_OFF("pay.toggle-off"),
  PAY_SEND("pay.send", new String[]{"amount", "receiver"}),
  PAY_RECEIVE("pay.receive", new String[]{"amount", "sender"}),
  REQUEST_TOGGLE_ON("request.toggle-on"),
  REQUEST_TOGGLE_OFF("request.toggle-off"),
  REQUEST_SEND("request.send", new String[]{"amount", "requestee"}),
  REQUEST_RECEIVE("request.receive", new String[]{"amount", "requester"}),
  REQUEST_ACCEPT_SEND("request.accept-send", new String[]{"amount", "requestee"}),
  REQUEST_ACCEPT_RECEIVE("request.accept-receive", new String[]{"amount", "requester"}),
  REQUEST_DENY_SEND("request.deny-send", new String[]{"amount", "requestee"}),
  REQUEST_DENY_RECEIVE("request.deny-receive", new String[]{"amount", "requester"}),
  WITHDRAW_MESSAGE("withdraw.message", new String[]{"amount"}),
  WITHDRAW_CLAIM("withdraw.claim", new String[]{"amount"}),
  WITHDRAW_BANKNOTE_DISPLAYNAME("withdraw.banknote-item.displayname", new String[]{"amount", "player"}),
  WITHDRAW_BANKNOTE_LORE("withdraw.banknote-item.lore", new String[]{"amount", "player"}),
  PLAYER_NOT_FOUND("errors.player-not-found"),
  PLAYER_NOT_ONLINE("errors.player-not-online"),
  CURRENCY_NOT_FOUND("errors.currency-not-found"),
  SELF_NOT_ENOUGH_MONEY("errors.not-enough-money.self"),
  OTHER_NOT_ENOUGH_MONEY("errors.not-enough-money.other"),
  NOT_ACCEPTING_PAYMENTS("errors.not-accepting-payments"),
  NOT_ACCEPTING_REQUESTS("errors.not-accepting-requests");
  
  private final String value;
  private final String[] tags;
  
  MessageType(String value) {
    this(value, new String[0]);
  }
  
}