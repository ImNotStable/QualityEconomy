package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.Misc;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class MainCommand extends BaseCommand {
  
  private final Pattern IMPORT_FILE_PATTERN = Pattern.compile("^QualityEconomy \\d{4}.\\d{2}.\\d{2} \\d{2}-\\d{2}\\.json$");
  private final CommandTree command = new CommandTree("qualityeconomy")
    .withAliases("qe")
    .withPermission("qualityeconomy.admin")
    .then(new LiteralArgument("reload")
      .executes(this::reload)
      .then(new LiteralArgument("messages")
        .executes(this::reloadMessages)))
    .then(new LiteralArgument("database")
      .then(new LiteralArgument("reset")
        .withRequirement(sender -> Debug.DEBUG_MODE)
        .executesConsole(this::resetDatabase))
      .then(new LiteralArgument("import")
        .then(new GreedyStringArgument("importable")
          .replaceSuggestions(getImportSuggestion())
          .executes(this::importDatabase)))
      .then(new LiteralArgument("export")
        .executes(this::exportDatabase))
      .then(new LiteralArgument("createFakeEntries")
        .withRequirement(sender -> Debug.DEBUG_MODE)
        .then(new IntegerArgument("entries", 1)
          .executes(this::createFakeEntries)))
      .then(new LiteralArgument("changeAllEntries")
        .withRequirement(sender -> Debug.DEBUG_MODE)
        .executes(this::changeAllEntries)))
    .then(new LiteralArgument("economy")
      .withRequirement(sender -> QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES)
      .then(new LiteralArgument("createCustomCurrency")
        .then(new StringArgument("name")
          .executes(this::createCustomCurrency)))
      .then(new LiteralArgument("deleteCustomCurrency")
        .then(new StringArgument("name")
          .replaceSuggestions(ArgumentSuggestions.strings(info -> StorageManager.getActiveStorageType().getCurrencies().toArray(new String[0])))
          .executes(this::deleteCustomCurrency)
        )));
  
  public void register() {
    super.register(command);
  }
  
  public void unregister() {
    super.unregister(command);
  }
  
  private void reload(CommandSender sender, CommandArguments args) {
    CompletableFuture.runAsync(() -> {
      Debug.Timer timer = new Debug.Timer("reload()");
      StorageManager.endStorageProcesses();
      QualityEconomy.getQualityConfig().load();
      QualityEconomy.getQualityMessages().load();
      CommandManager.unregisterCommands();
      StorageManager.initStorageProcesses(QualityEconomy.getInstance());
      CommandManager.registerCommands();
      timer.end();
      Misc.sendColoredMessage(sender, "&7Reloaded QualityEconomy");
    });
  }
  
  private void reloadMessages(CommandSender sender, CommandArguments args) {
    QualityEconomy.getQualityMessages().load();
    Misc.sendColoredMessage(sender, "&7Reloading QualityEconomy messages.yml...");
  }
  
  private void resetDatabase(ConsoleCommandSender sender, CommandArguments args) {
    StorageManager.getActiveStorageType().wipeDatabase();
    Misc.sendColoredMessage(sender, "&cResetting database...");
  }
  
  private void importDatabase(CommandSender sender, CommandArguments args) {
    String importable = (String) args.get("importable");
    if (Misc.equals(importable, "Essentials"))
      transferPluginData(importable, sender);
    else {
      boolean completed = false;
      try {
        completed = StorageManager.importDatabase(importable).get();
      } catch (InterruptedException | ExecutionException exception) {
        new Debug.QualityError("Error while importing database", exception).log();
      }
      if (completed)
        Misc.sendColoredMessage(sender, "&aImported Database");
      else
        Misc.sendColoredMessage(sender, "&cFailed to import Database");
    }
  }
  
  private void transferPluginData(String plugin, CommandSender sender) {
    CompletableFuture.runAsync(() -> {
      Debug.Timer timer = new Debug.Timer("transferPluginData()");
      Collection<Account> accounts = new ArrayList<>();
      if (plugin.equals("Essentials")) {
        File[] userdata = new File("plugins/Essentials/userdata").listFiles((dir, name) -> Misc.isUUID(name.split("\\.")[0]));
        if (userdata == null || userdata.length == 0) {
          Misc.sendColoredMessage(sender, "&cFailed to import Database");
          return;
        }
        for (File userfile : userdata) {
          YamlConfiguration user = YamlConfiguration.loadConfiguration(userfile);
          UUID uuid = UUID.fromString(userfile.getName().split("\\.")[0]);
          String username = user.getString("last-account-name");
          double balance = Double.parseDouble(user.getString("money"));
          accounts.add(new Account(uuid).setUsername(username).setBalance(balance));
        }
      }
      StorageManager.getActiveStorageType().wipeDatabase();
      StorageManager.getActiveStorageType().createAccounts(accounts);
      AccountManager.setupAccounts();
      Misc.sendColoredMessage(sender, "&aImported Database");
      timer.end();
    });
  }
  
  private void exportDatabase(CommandSender sender, CommandArguments args) {
    StorageManager.exportDatabase("plugins/QualityEconomy/exports/");
    Misc.sendColoredMessage(sender, "&aExported Database");
  }
  
  private void createFakeEntries(CommandSender sender, CommandArguments args) {
    int entries = (int) args.get("entries");
    AccountManager.createFakeAccounts(entries);
  }
  
  private void changeAllEntries(CommandSender sender, CommandArguments args) {
    AccountManager.changeAllAccounts();
  }
  
  private void createCustomCurrency(CommandSender sender, CommandArguments args) {
    String currency = args.get("name").toString().toUpperCase();
    if (StorageManager.getActiveStorageType().getCurrencies().contains(currency)) {
      Misc.sendColoredMessage(sender, "&cThat currency already exists");
      return;
    }
    Misc.sendColoredMessage(sender, "&aCreating custom currency \"" + currency + "\"");
    StorageManager.addCurrency(currency);
  }
  
  private void deleteCustomCurrency(CommandSender sender, CommandArguments args) {
    String currency = args.get("name").toString().toUpperCase();
    if (!StorageManager.getActiveStorageType().getCurrencies().contains(currency)) {
      Misc.sendColoredMessage(sender, "&cThat currency does not exist");
      return;
    }
    Misc.sendColoredMessage(sender, "&7Deleting custom currency \"" + currency + "\"");
    StorageManager.removeCurrency(currency);
  }
  
  private ArgumentSuggestions<CommandSender> getImportSuggestion() {
    return ArgumentSuggestions.stringCollection(info -> {
      List<String> completions = new ArrayList<>(getImportableFiles("exports"));
      completions.addAll(getImportableFiles("backups"));
      
      if (new File("plugins/Essentials/userdata").isDirectory())
        completions.add("Essentials");
      
      return completions;
    });
  }
  
  public Collection<String> getImportableFiles(String path) {
    File backupFolder = new File(QualityEconomy.getInstance().getDataFolder(), path);
    return Optional.ofNullable(backupFolder.listFiles((dir, name) -> IMPORT_FILE_PATTERN.matcher(name).matches()))
      .map(Arrays::asList)
      .orElse(Collections.emptyList())
      .stream()
      .map(file -> path + "/" + file.getName())
      .toList();
  }
  
}
