package com.imnotstable.qualityeconomy.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MessageType {
  
  VIEW_OWN("view-command.own", true),
  VIEW_OTHER("view-command.other", true),
  VIEW_PLAYER_NOT_FOUND("errors.player-not-found", true),
  
  ADMIN_SET("admin-command.set", true),
  ADMIN_ADD("admin-command.add", true),
  ADMIN_REMOVE("admin-command.remove", true),
  ADMIN_RESET("admin-command.reset", true),
  ADMIN_PLAYER_NOT_FOUND("errors.player-not-found", true),
  ADMIN_INVALID_NUMBER("errors.invalid-number", true),
  
  TRANSFER_TOGGLE_ON("transfer-command.toggle-on", true),
  TRANSFER_TOGGLE_OFF("transfer-command.toggle-off", true),
  TRANSFER_SEND("transfer-command.send", true),
  TRANSFER_RECEIVE("transfer-command.receive", true),
  TRANSFER_PLAYER_NOT_FOUND("errors.player-not-found", true),
  TRANSFER_NOT_ACCEPTING_PAYMENTS("errors.not-accepting-payments", true),
  TRANSFER_NOT_ENOUGH_MONEY("errors.not-enough-money.self", true),
  TRANSFER_INVALID_NUMBER("errors.invalid-number", true),
  
  LEADERBOARD_TITLE("leaderboard-command.title", true),
  LEADERBOARD_SERVER_TOTAL("leaderboard-command.server-total", true),
  LEADERBOARD_BALANCE_VIEW("leaderboard-command.balance-view", true),
  LEADERBOARD_NEXT_PAGE("leaderboard-command.next-page", true);
  
  
  private final String value;
  private final boolean currencyDependent;
  
  MessageType(String value) {
    this(value, false);
  }
  
}