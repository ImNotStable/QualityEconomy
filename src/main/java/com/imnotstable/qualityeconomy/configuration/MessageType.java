package com.imnotstable.qualityeconomy.configuration;

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
  WITHDRAW("withdraw.withdraw");
  MessageType(String value) {
    this.value = value;
  }
  private final String value;
  public String getValue() {
    return value;
  }
}