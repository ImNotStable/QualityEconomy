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
  BALANCE_OWN_BALANCE("balance.own-balance"),
  BALANCE_OTHER_BALANCE("balance.other-balance"),
  ECONOMY_SET("economy.set"),
  ECONOMY_ADD("economy.add"),
  ECONOMY_REMOVE("economy.remove"),
  ECONOMY_RESET("economy.reset"),
  PAY_TOGGLE_ON("pay.toggle-on"),
  PAY_TOGGLE_OFF("pay.toggle-off"),
  PAY_SEND("pay.send"),
  PAY_RECEIVE("pay.receive"),
  REQUEST_TOGGLE_ON("request.toggle-on"),
  REQUEST_TOGGLE_OFF("request.toggle-off"),
  REQUEST_SEND("request.send"),
  REQUEST_RECEIVE("request.receive"),
  REQUEST_ACCEPT_SEND("request.accept-send"),
  REQUEST_ACCEPT_RECEIVE("request.accept-receive"),
  REQUEST_DENY_SEND("request.deny-send"),
  REQUEST_DENY_RECEIVE("request.deny-receive"),
  WITHDRAW_MESSAGE("withdraw.message"),
  WITHDRAW_CLAIM("withdraw.claim"),
  WITHDRAW_BANKNOTE_DISPLAYNAME("withdraw.banknote-item.displayname"),
  WITHDRAW_BANKNOTE_LORE("withdraw.banknote-item.lore"),
  PLAYER_NOT_FOUND("errors.player-not-found"),
  PLAYER_NOT_ONLINE("errors.player-not-online"),
  CURRENCY_NOT_FOUND("errors.currency-not-found"),
  SELF_NOT_ENOUGH_MONEY("errors.not-enough-money.self"),
  OTHER_NOT_ENOUGH_MONEY("errors.not-enough-money.other"),
  NOT_ACCEPTING_PAYMENTS("errors.not-accepting-payments"),
  NOT_ACCEPTING_REQUESTS("errors.not-accepting-requests"),
  INVALID_NUMBER("errors.invalid-number");
  
  private final String value;
  
}