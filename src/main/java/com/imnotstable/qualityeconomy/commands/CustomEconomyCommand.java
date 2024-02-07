package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

@Getter
public class CustomEconomyCommand implements Command {
  
  @Getter
  private final String name = "customeconomy";
  
  private final CommandTree command = new CommandTree(name)
    .withAliases("ceconomy", "customeco", "ceco")
    .withPermission("qualityeconomy.customeconomy")
    .then(new StringArgument("currency")
      .replaceSuggestions(ArgumentSuggestions.strings(info -> StorageManager.getActiveStorageFormat().getCurrencies().toArray(new String[0])))
      .then(new OfflinePlayerArgument("target")
        .replaceSuggestions(CommandUtils.getOnlinePlayerSuggestion())
        .then(new LiteralArgument("reset").executes(this::resetBalance))
        .then(new LiteralArgument("set").then(new DoubleArgument("amount").executes(this::setBalance)))
        .then(new LiteralArgument("add").then(new DoubleArgument("amount").executes(this::addBalance)))
        .then(new LiteralArgument("remove").then(new DoubleArgument("amount").executes(this::removeBalance)))));
  private boolean isRegistered = false;
  
  public void register() {
    if (isRegistered || !Configuration.isCommandEnabled("customeconomy") || StorageManager.getActiveStorageFormat().getCurrencies().isEmpty())
      return;
    command.register();
    isRegistered = true;
  }
  
  public void unregister() {
    if (!isRegistered)
      return;
    CommandAPI.unregister(name, true);
    isRegistered = false;
  }
  
  private void resetBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.requirement(StorageManager.getActiveStorageFormat().getCurrencies().contains(currency), MessageType.CURRENCY_NOT_FOUND, sender))
      return;
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    QualityEconomyAPI.setCustomBalance(target.getUniqueId(), currency, 0);
    Messages.sendParsedMessage(sender, MessageType.ECONOMY_RESET,
      target.getName()
    );
  }
  
  private void setBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.requirement(StorageManager.getActiveStorageFormat().getCurrencies().contains(currency), MessageType.CURRENCY_NOT_FOUND, sender))
      return;
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    double balance = Number.roundObj(args.get("amount"));
    QualityEconomyAPI.setCustomBalance(target.getUniqueId(), currency, balance);
    Messages.sendParsedMessage(sender, MessageType.ECONOMY_SET,
      Number.formatCommas(balance),
      target.getName()
    );
  }
  
  private void addBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.requirement(StorageManager.getActiveStorageFormat().getCurrencies().contains(currency), MessageType.CURRENCY_NOT_FOUND, sender))
      return;
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    double balance = Number.roundObj(args.get("amount"));
    QualityEconomyAPI.addCustomBalance(target.getUniqueId(), currency, balance);
    Messages.sendParsedMessage(sender, MessageType.ECONOMY_ADD,
      Number.formatCommas(balance),
      target.getName()
    );
  }
  
  private void removeBalance(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("currency");
    if (CommandUtils.requirement(StorageManager.getActiveStorageFormat().getCurrencies().contains(currency), MessageType.CURRENCY_NOT_FOUND, sender))
      return;
    OfflinePlayer target = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(target.getUniqueId()), MessageType.PLAYER_NOT_FOUND, sender))
      return;
    double balance = Number.roundObj(args.get("amount"));
    QualityEconomyAPI.removeCustomBalance(target.getUniqueId(), currency, balance);
    Messages.sendParsedMessage(sender, MessageType.ECONOMY_REMOVE,
      Number.formatCommas(balance),
      target.getName()
    );
  }
  
}
