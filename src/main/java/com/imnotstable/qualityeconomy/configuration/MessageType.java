package com.imnotstable.qualityeconomy.configuration;

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
  WITHDRAW("withdraw.withdraw", new String[]{"amount"});
  MessageType(String value, String[] tags) {
    this.value = value;
    this.tags = tags;
  }
  private final String value;
  private final String[] tags;
  public String getValue() {
    return value;
  }
  public String[] getTags() {
    return tags;
  }
}