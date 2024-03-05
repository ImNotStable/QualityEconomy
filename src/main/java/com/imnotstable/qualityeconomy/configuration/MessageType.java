package com.imnotstable.qualityeconomy.configuration;

import lombok.Getter;

@Getter
public enum MessageType {
  BALANCETOP_TITLE("balancetop.title", "maxpage", "page"),
  BALANCETOP_SERVER_TOTAL("balancetop.server-total","servertotal"),
  BALANCETOP_BALANCE_VIEW("balancetop.balance-view","balance", "place", "player"),
  BALANCETOP_NEXT_PAGE("balancetop.next-page","command", "nextpage"),
  BALANCE_OWN_BALANCE("balance.own-balance","balance"),
  BALANCE_OTHER_BALANCE("balance.other-balance","balance", "player"),
  ECONOMY_SET("economy.set","balance", "player"),
  ECONOMY_ADD("economy.add","balance", "player"),
  ECONOMY_REMOVE("economy.remove","balance", "player"),
  ECONOMY_RESET("economy.reset","player"),
  PAY_TOGGLE_ON("pay.toggle-on"),
  PAY_TOGGLE_OFF("pay.toggle-off"),
  PAY_SEND("pay.send","amount", "receiver"),
  PAY_RECEIVE("pay.receive","amount", "sender"),
  REQUEST_TOGGLE_ON("request.toggle-on"),
  REQUEST_TOGGLE_OFF("request.toggle-off"),
  REQUEST_SEND("request.send","amount", "requestee"),
  REQUEST_RECEIVE("request.receive","amount", "requester"),
  REQUEST_ACCEPT_SEND("request.accept-send","amount", "requestee"),
  REQUEST_ACCEPT_RECEIVE("request.accept-receive","amount", "requester"),
  REQUEST_DENY_SEND("request.deny-send","amount", "requestee"),
  REQUEST_DENY_RECEIVE("request.deny-receive","amount", "requester"),
  WITHDRAW_MESSAGE("withdraw.message","amount"),
  WITHDRAW_CLAIM("withdraw.claim","amount", "player"),
  WITHDRAW_BANKNOTE_DISPLAYNAME("withdraw.banknote-item.displayname","amount", "player"),
  WITHDRAW_BANKNOTE_LORE("withdraw.banknote-item.lore","amount", "player"),
  PLAYER_NOT_FOUND("errors.player-not-found", "player"),
  PLAYER_NOT_ONLINE("errors.player-not-online", "player"),
  CURRENCY_NOT_FOUND("errors.currency-not-found", "currency"),
  SELF_NOT_ENOUGH_MONEY("errors.not-enough-money.self"),
  OTHER_NOT_ENOUGH_MONEY("errors.not-enough-money.other"),
  NOT_ACCEPTING_PAYMENTS("errors.not-accepting-payments"),
  NOT_ACCEPTING_REQUESTS("errors.not-accepting-requests"),
  INVALID_NUMBER("errors.invalid-number", "amount");
  
  private final String value;
  private final String[] tags;
  
  MessageType(String value, String... tags) {
    this.value = value;
    this.tags = tags;
  }
  
  MessageType(String value) {
    this(value, new String[0]);
  }
  
}