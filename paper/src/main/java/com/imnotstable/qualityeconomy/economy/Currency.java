package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.config.MessageType;
import com.imnotstable.qualityeconomy.config.Messages;
import com.imnotstable.qualityeconomy.util.Number;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

@Getter
public class Currency {
  
  private final String name;
  private final String viewCommand;
  private final String[] viewAliases;
  private final String adminCommand;
  private final String[] adminAliases;
  private final String transferCommand;
  private final String[] transferAliases;
  private final String leaderboardCommand;
  private final String[] leaderboardAliases;
  private final double defaultBalance;
  private final int decimalPlaces;
  private final String singular;
  private final String plural;
  private final String symbol;
  private final int symbolPosition;
  private final boolean customEvents;
  private final boolean transactionLogging;
  private final Map<MessageType, String> messages;
  
  private Currency(@NotNull String name,
                   @NotNull String @NotNull [] viewCommands,
                   @NotNull String @NotNull [] adminCommands,
                   @NotNull String @NotNull [] transferCommands,
                   @NotNull String @NotNull [] leaderboardCommands,
                   double defaultBalance, int decimalPlaces,
                   String singular, String plural,
                   String symbol, String symbolPosition,
                   boolean customEvents, boolean transactionLogging,
                   Map<MessageType, String> messages) {
    this.name = name;
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
    
    this.defaultBalance = defaultBalance;
    this.decimalPlaces = decimalPlaces;
    this.singular = singular;
    this.plural = plural;
    this.symbol = symbol;
    if (symbolPosition.equalsIgnoreCase("after"))
      this.symbolPosition = 1;
    else
      this.symbolPosition = -1;
    this.customEvents = customEvents;
    this.transactionLogging = transactionLogging;
    this.messages = messages;
  }
  
  public static Currency of(String name,
                            String[] viewCommands, String[] adminCommands, String[] transferCommands, String[] leaderboardCommands,
                            double startingBalance, int decimalPlaces,
                            String singular, String plural,
                            String symbol, String symbolPosition,
                            boolean customEvents, boolean transactionLogging,
                            Map<MessageType, String> messages) {
    return new Currency(name, viewCommands, adminCommands, transferCommands, leaderboardCommands, startingBalance, decimalPlaces, singular, plural, symbol, symbolPosition, customEvents, transactionLogging, messages);
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
  
  public double round(double n) {
    if (decimalPlaces == -1)
      return n;
    double multiplier = Math.pow(10, decimalPlaces);
    return Math.floor(n * multiplier) / multiplier;
  }
  
  public double getMinimumValue() {
    if (decimalPlaces <= 0)
      return 0.0;
    return Math.pow(10, -decimalPlaces);
  }
  
  public String getMessage(MessageType type) {
    if (!type.isCurrencyDependent())
      throw new IllegalArgumentException("Message type " + type + " is not currency dependent");
    if (!messages.containsKey(type))
      return Messages.getMessage(type);
    return messages.get(type);
  }
  
}
