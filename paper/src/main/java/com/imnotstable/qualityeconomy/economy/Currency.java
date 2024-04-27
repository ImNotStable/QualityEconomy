package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.config.MessageType;
import com.imnotstable.qualityeconomy.config.Messages;
import com.imnotstable.qualityeconomy.util.Number;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class Currency {
  
  @Getter
  private final String name;
  @Getter
  private final double defaultBalance;
  @Getter
  private final int decimalPlaces;
  @Getter
  private final String viewCommand;
  @Getter
  private final String[] viewAliases;
  @Getter
  private final String adminCommand;
  @Getter
  private final String[] adminAliases;
  @Getter
  private final String transferCommand;
  @Getter
  private final String[] transferAliases;
  @Getter
  private final String leaderboardCommand;
  @Getter
  private final String[] leaderboardAliases;
  @Getter
  private final String symbol;
  @Getter
  private final int symbolPosition;
  @Getter
  private final String singular;
  @Getter
  private final String plural;
  private final Map<MessageType, String> messages;
  
  private Currency(@NotNull String name, double defaultBalance, int decimalPlaces,
                   @NotNull String @NotNull [] viewCommands,
                   @NotNull String @NotNull [] adminCommands,
                   @NotNull String @NotNull [] transferCommands,
                   @NotNull String @NotNull [] leaderboardCommands,
                   String symbol, String symbolPosition, String singular, String plural,
                   Map<MessageType, String> messages) {
    this.name = name;
    this.defaultBalance = defaultBalance;
    this.decimalPlaces = decimalPlaces;
    if (viewCommands.length > 0)
      this.viewCommand = viewCommands[0];
    else
      this.viewCommand = null;
    if (viewCommands.length > 1) {
      this.viewAliases = new String[viewCommands.length - 1];
      System.arraycopy(viewCommands, 1, viewAliases, 0, viewAliases.length);
    } else
      this.viewAliases = new String[0];
    
    if (adminCommands.length > 0)
      this.adminCommand = adminCommands[0];
    else
      this.adminCommand = null;
    if (adminCommands.length > 1) {
      this.adminAliases = new String[adminCommands.length - 1];
      System.arraycopy(adminCommands, 1, adminAliases, 0, adminAliases.length);
    } else
      this.adminAliases = new String[0];
    
    if (transferCommands.length > 0)
      this.transferCommand = transferCommands[0];
    else
      this.transferCommand = null;
    if (transferCommands.length > 1) {
      this.transferAliases = new String[transferCommands.length - 1];
      System.arraycopy(transferCommands, 1, transferAliases, 0, transferAliases.length);
    } else
      this.transferAliases = new String[0];
    
    if (leaderboardCommands.length > 0)
      this.leaderboardCommand = leaderboardCommands[0];
    else
      this.leaderboardCommand = null;
    if (leaderboardCommands.length > 1) {
      this.leaderboardAliases = new String[leaderboardCommands.length - 1];
      System.arraycopy(leaderboardCommands, 1, leaderboardAliases, 0, leaderboardAliases.length);
    } else
      this.leaderboardAliases = new String[0];
    
    this.symbol = symbol;
    if (symbolPosition.equalsIgnoreCase("after"))
      this.symbolPosition = 1;
    else
      this.symbolPosition = -1;
    this.singular = singular;
    this.plural = plural;
    this.messages = messages;
  }
  
  public double getBalance(UUID uniqueId) {
    return QualityEconomyAPI.getBalance(uniqueId, name);
  }
  
  public String getFormattedBalance(UUID uniqueId) {
    return getFormattedAmount(getBalance(uniqueId));
  }
  
  public String getFormattedAmount(double amount) {
    String formattedAmount = Number.format(amount, Number.FormatType.COMMAS);
    if (symbol == null)
      return formattedAmount;
    if (symbolPosition == 1)
      return formattedAmount + symbol;
    return symbol + formattedAmount;
  }
  
  public String getMessage(MessageType type) {
    if (!type.isCurrencyDependent())
      throw new IllegalArgumentException("Message type " + type + " is not currency dependent");
    if (!messages.containsKey(type))
      return Messages.getMessage(type);
    return messages.get(type);
  }
  
  public static Currency of(String name, double startingBalance, int decimalPlaces,
                            String[] viewCommands, String[] adminCommands, String[] transferCommands, String[] leaderboardCommands,
                            String symbol, String symbolPosition, String singular, String plural,
                            Map<MessageType, String> messages) {
    return new Currency(name, startingBalance, decimalPlaces, viewCommands, adminCommands, transferCommands, leaderboardCommands, symbol, symbolPosition, singular, plural, messages);
  }
  
}
