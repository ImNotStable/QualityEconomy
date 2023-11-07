package com.imnotstable.qualityeconomy.storage;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.util.Error;
import com.imnotstable.qualityeconomy.util.Logger;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomCurrencies {
  
  private static final File file = new File(QualityEconomy.getPluginFolder(), "customCurrencies.yml");
  private static List<String> customCurrencies = new ArrayList<>();
  
  public static void loadCustomCurrencies() {
    if (!file.exists())
      return;
    loadCommand();
    customCurrencies = YamlConfiguration.loadConfiguration(file).getStringList("custom-currencies");
    if (customCurrencies.isEmpty())
      file.delete();
  }
  
  public static void createCustomCurrency(String currencyName) {
    if (List.of("uuid", "name", "balance", "payable").contains(currencyName.toLowerCase())) {
      new Error("Currency creation failed due to illegal name").log();
      return;
  }
    loadCommand();
    if (!file.exists()) {
      try {
        if (file.createNewFile())
          Logger.log(Component.text("Successfully created customCurrencies.yml", NamedTextColor.GREEN));
      } catch (IOException exception) {
        new Error("Failed to created customCurrencies.yml", exception).log();
      }
    }
    customCurrencies.add(currencyName);
    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
    configuration.setComments("custom-currencies", List.of("Don't touch this file."));
    configuration.set("custom-currencies", customCurrencies);
    try {
      configuration.save(file);
    } catch (IOException exception) {
      new Error("Failed to save customCurrencies.yml", exception).log();
    }
    StorageManager.getActiveStorageFormat().addCurrency(currencyName);
  }
  
  public static void deleteCustomCurrency(String currencyName) {
    unloadCommand();
    customCurrencies.remove(currencyName);
    if (customCurrencies.isEmpty())
      file.delete();
    else {
      YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
      configuration.set("custom-currencies", customCurrencies);
      try {
        configuration.save(file);
      } catch (IOException exception) {
        new Error("Failed to save customCurrencies.yml", exception).log();
      }
    }
    StorageManager.getActiveStorageFormat().removeCurrency(currencyName);
  }
  
  public static List<String> getCustomCurrencies() {
    return customCurrencies;
  }
  
  public static void loadCommand() {
    if (!Configuration.CUSTOM_ECONOMY_COMMANDS)
      return;
    new CommandTree("customeconomy")
      .withAliases("ceconomy", "customeco", "ceco")
      .withPermission("qualityeconomy.customeconomy")
      .then(new StringArgument("currency")
        .replaceSuggestions(ArgumentSuggestions.strings(info -> CustomCurrencies.getCustomCurrencies().toArray(new String[0])))
        .then(new OfflinePlayerArgument("target")
          .replaceSuggestions(ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList().toArray(new String[0])))
          .then(new LiteralArgument("set")
            .then(new DoubleArgument("amount")
              .executes((sender, args) -> {
                String currency = (String) args.get("currency");
                if (!getCustomCurrencies().contains(currency)) {
                  sender.sendMessage(Component.text("That currency does not exist", NamedTextColor.RED));
                  return;
                }
                OfflinePlayer target = (OfflinePlayer) args.get("target");
                if (!AccountManager.accountExists(target.getUniqueId())) {
                  sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
                  return;
                }
                double balance = (double) args.get("amount");
                AccountManager.updateAccount(AccountManager.getAccount(target.getUniqueId()).setCustomBalance(currency, balance));
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("economy.set"),
                  TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(target.getName()))),
                  TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(balance))))));
              })))
          .then(new LiteralArgument("reset")
            .executes((sender, args) -> {
              String currency = (String) args.get("currency");
              if (!getCustomCurrencies().contains(currency)) {
                sender.sendMessage(Component.text("That currency does not exist", NamedTextColor.RED));
                return;
              }
              OfflinePlayer target = (OfflinePlayer) args.get("target");
              if (!AccountManager.accountExists(target.getUniqueId())) {
                sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
                return;
              }
              AccountManager.updateAccount(AccountManager.getAccount(target.getUniqueId()).setCustomBalance(currency, 0));
              sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("economy.reset"),
                TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(target.getName()))),
                TagResolver.resolver("", Tag.selfClosingInserting(Component.text("")))));
            }))
          .then(new LiteralArgument("add")
            .then(new DoubleArgument("amount")
              .executes((sender, args) -> {
                String currency = (String) args.get("currency");
                if (!getCustomCurrencies().contains(currency)) {
                  sender.sendMessage(Component.text("That currency does not exist", NamedTextColor.RED));
                  return;
                }
                OfflinePlayer target = (OfflinePlayer) args.get("target");
                if (!AccountManager.accountExists(target.getUniqueId())) {
                  sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
                  return;
                }
                Account account = AccountManager.getAccount(target.getUniqueId());
                double balance = (double) args.get("amount");
                AccountManager.updateAccount(account.setCustomBalance(currency, account.getCustomBalance(currency) + balance));
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("economy.add"),
                  TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(balance)))),
                  TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(target.getName())))));
              })))
          .then(new LiteralArgument("remove")
            .then(new DoubleArgument("amount")
              .executes((sender, args) -> {
                String currency = (String) args.get("currency");
                if (!getCustomCurrencies().contains(currency)) {
                  sender.sendMessage(Component.text("That currency does not exist", NamedTextColor.RED));
                  return;
                }
                OfflinePlayer target = (OfflinePlayer) args.get("target");
                if (!AccountManager.accountExists(target.getUniqueId())) {
                  sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
                  return;
                }
                Account account = AccountManager.getAccount(target.getUniqueId());
                double balance = (double) args.get("amount");
                AccountManager.updateAccount(account.setCustomBalance(currency, account.getCustomBalance(currency) - balance));
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("economy.remove"),
                  TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(balance)))),
                  TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(target.getName())))));
              })))))
      .register();
    new CommandTree("custombalance")
      .withAliases("cbalance", "custombal", "cbal")
      .then(new StringArgument("currency")
        .replaceSuggestions(ArgumentSuggestions.strings(info -> CustomCurrencies.getCustomCurrencies().toArray(new String[0])))
      .then(new OfflinePlayerArgument("target")
        .replaceSuggestions(ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList().toArray(new String[0])))
        .executes((sender, args) -> {
          String currency = (String) args.get("currency");
          if (!getCustomCurrencies().contains(currency)) {
            sender.sendMessage(Component.text("That currency does not exist", NamedTextColor.RED));
            return;
          }
          OfflinePlayer target = (OfflinePlayer) args.get("target");
          if (!AccountManager.accountExists(target.getUniqueId())) {
            sender.sendMessage(Component.text("That player does not exist", NamedTextColor.RED));
            return;
          }
          sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("balance.other-balance"),
            TagResolver.resolver("player", Tag.selfClosingInserting(Component.text(target.getName()))),
            TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(AccountManager.getAccount(target.getUniqueId()).getCustomBalance(currency)))))));
        }))
      .executesPlayer((sender, args) -> {
        String currency = (String) args.get("currency");
        if (!getCustomCurrencies().contains(currency)) {
          sender.sendMessage(Component.text("That currency does not exist", NamedTextColor.RED));
          return;
        }
        sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("balance.own-balance"),
          TagResolver.resolver("balance", Tag.selfClosingInserting(Component.text(Number.formatCommas(AccountManager.getAccount(sender.getUniqueId()).getCustomBalance(currency)))))));
      }))
      .register();
  }
  
  public static void unloadCommand() {
    CommandAPI.unregister("customeconomy", true);
    CommandAPI.unregister("custombalance", true);
  }
  
}
