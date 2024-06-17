package com.imnotstable.qualityeconomy.economy;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.config.MessageType;
import com.imnotstable.qualityeconomy.config.Messages;
import com.imnotstable.qualityeconomy.util.CurrencyFormatter;
import com.imnotstable.qualityeconomy.util.QualityException;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Internal
@SuppressWarnings("unused")
@Getter
public class Currency {
  
  private final String name;
  private final Command viewCommand;
  private final Command adminCommand;
  private final Command transferCommand;
  private final Command leaderboardCommand;
  private final int leaderboardRefreshInterval;
  private final double startingBalance;
  private final String singular;
  private final String plural;
  private final CurrencyFormatter.FormatType formatType;
  private final int decimalPlaces;
  private final String symbol;
  private final int symbolPosition;
  private final boolean customEvents;
  private final boolean transactionLogging;
  private final Map<MessageType, String> messages;
  
  private Currency(@NotNull String name,
                   @NotNull Command viewCommand,
                   @NotNull Command adminCommand,
                   @NotNull Command transferCommand,
                   @NotNull Command leaderboardCommand, int leaderboardRefreshInterval,
                   double startingBalance,
                   String singular, String plural,
                   CurrencyFormatter.FormatType formatType, int decimalPlaces, String symbol, String symbolPosition,
                   boolean customEvents, boolean transactionLogging,
                   Map<MessageType, String> messages) {
    this.name = name;
    
    this.viewCommand = viewCommand;
    this.adminCommand = adminCommand;
    this.transferCommand = transferCommand;
    this.leaderboardCommand = leaderboardCommand;
    this.leaderboardRefreshInterval = leaderboardRefreshInterval;
    
    this.startingBalance = startingBalance;
    this.singular = singular;
    this.plural = plural;
    this.formatType = formatType;
    this.decimalPlaces = decimalPlaces;
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
                            Command viewCommands, Command adminCommands, Command transferCommands, Command leaderboardCommands,
                            int leaderboardRefreshInterval,
                            double startingBalance,
                            String singular, String plural,
                            CurrencyFormatter.FormatType formatType, int decimalPlaces, String symbol, String symbolPosition,
                            boolean customEvents, boolean transactionLogging,
                            Map<MessageType, String> messages) {
    return new Currency(name, viewCommands, adminCommands, transferCommands, leaderboardCommands, leaderboardRefreshInterval, startingBalance, singular, plural, formatType, decimalPlaces, symbol, symbolPosition, customEvents, transactionLogging, messages);
  }
  
  public static Currency of(ConfigurationSection section) throws QualityException {
    if (section == null)
      throw new QualityException("Currency section is null");
    Command viewCommand = Command.fromYaml(section.getConfigurationSection("view-command"));
    Command adminCommand = Command.fromYaml(section.getConfigurationSection("admin-command"));
    Command transferCommand = Command.fromYaml(section.getConfigurationSection("transfer-command"));
    Command leaderboardCommand = Command.fromYaml(section.getConfigurationSection("leaderboard-command"));
    int leaderboardRefreshInterval = section.getInt("leaderboard-refresh-interval", 5) * 20;
    if (leaderboardRefreshInterval < 1)
      throw new QualityException("Leaderboard refresh interval must be at least 1 second");
    double startingBalance = section.getDouble("starting-balance", 0.0);
    String singular = section.getString("singular-name");
    if (singular == null)
      throw new QualityException("Singular name is null");
    String plural = section.getString("plural-name");
    if (plural == null)
      throw new QualityException("Plural name is null");
    CurrencyFormatter.FormatType formatType = switch (section.getString("format-type", "NORMAL").toUpperCase()) {
      case "NORMAL" -> CurrencyFormatter.FormatType.NORMAL;
      case "SUFFIX" -> CurrencyFormatter.FormatType.SUFFIX;
      case "UPPERCASE_SUFFIX" -> CurrencyFormatter.FormatType.UPPERCASE_SUFFIX;
      case "COMMAS" -> CurrencyFormatter.FormatType.COMMAS;
      case "QUALITY" -> CurrencyFormatter.FormatType.QUALITY;
      default -> throw new QualityException("Invalid format type");
    };
    int decimalPlaces = section.getInt("decimal-places", 2);
    String symbol = section.getString("symbol");
    if (symbol == null)
      throw new QualityException("Symbol is null");
    String symbolPosition = section.getString("symbol-position", "before");
    boolean customEvents = section.getBoolean("custom-events", false);
    boolean transactionLogging = section.getBoolean("transaction-logging", false);
    MessageType[] messageTypes = MessageType.values();
    Map<MessageType, String> messages = new HashMap<>();
    for (MessageType messageType : messageTypes) {
      if (!messageType.isCurrencyDependent())
        continue;
      String message = section.getString("messages." + messageType.getKey());
      if (message != null)
        messages.put(messageType, message);
    }
    return Currency.of(section.getName(), viewCommand, adminCommand, transferCommand, leaderboardCommand, leaderboardRefreshInterval, startingBalance, singular, plural, formatType, decimalPlaces, symbol, symbolPosition, customEvents, transactionLogging, messages);
  }
  
  public double getBalance(UUID uniqueId) {
    return QualityEconomyAPI.getBalance(uniqueId, name);
  }
  
  public String getFormattedBalance(UUID uniqueId) {
    return getFormattedAmount(getBalance(uniqueId));
  }
  
  public String getFormattedAmount(double amount) {
    String formattedAmount = CurrencyFormatter.format(amount, formatType);
    if (symbol == null)
      return formattedAmount;
    if (symbolPosition == 1)
      return formattedAmount + symbol;
    return symbol + formattedAmount;
  }
  
  public double getUnformattedAmount(@NotNull String amount) throws NumberFormatException {
    if (amount.isEmpty())
      return 0.0;
    if (amount.contains(symbol)) {
      if (symbolPosition == 1)
        amount = amount.substring(0, amount.length() - symbol.length());
      if (symbolPosition == -1)
        amount = amount.substring(symbol.length());
    }
    return CurrencyFormatter.unformat(amount);
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
  
  public record Command(@Nullable String command, @NotNull String @NotNull [] aliases, @Nullable String permission) {
    
    public static Command fromYaml(@Nullable ConfigurationSection section) {
      if (section == null)
        return new Command(null, new String[0], null);
      String command = section.getString("command");
      String[] aliases = section.getStringList("aliases").toArray(new String[0]);
      String permission = section.getString("permission");
      return new Command(command, aliases, permission);
    }
    
    public static Command fromJson(JsonObject commandJSON) {
      String command = commandJSON.get("COMMAND").getAsString();
      JsonArray commandAliasesJSON = commandJSON.getAsJsonArray("ALIASES");
      String[] aliases = new String[commandAliasesJSON.size()];
      for (int i = 0; i < commandAliasesJSON.size(); i++)
        aliases[i] = commandAliasesJSON.get(i).getAsString();
      String permission = commandJSON.get("PERMISSION").getAsString();
      return new Command(command, aliases, permission);
    }
    
    public JsonObject toJson() {
      JsonObject commandJSON = new JsonObject();
      commandJSON.addProperty("COMMAND", command);
      JsonArray commandAliasesJSON = new JsonArray();
      for (String alias : aliases)
        commandAliasesJSON.add(alias);
      commandJSON.add("ALIASES", commandAliasesJSON);
      commandJSON.addProperty("PERMISSION", permission);
      return commandJSON;
    }
    
  }
  
}
