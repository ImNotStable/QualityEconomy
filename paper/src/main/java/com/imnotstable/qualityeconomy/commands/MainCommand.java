package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.Misc;
import com.imnotstable.qualityeconomy.util.debug.Debug;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import com.imnotstable.qualityeconomy.util.debug.Timer;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class MainCommand extends BaseCommand {
  
  private static MainCommand INSTANCE;
  private final Pattern IMPORT_FILE_PATTERN = Pattern.compile("^QualityEconomy \\d{4}.\\d{2}.\\d{2} \\d{2}-\\d{2}\\.json$");
  
  public static void load() {
    if (INSTANCE != null)
      INSTANCE.unregister();
    INSTANCE = new MainCommand();
    INSTANCE.register();
  }
  
  public void register() {
    super.register(command);
  }
  
  public void unregister() {
    super.unregister(command);
  }  private final CommandTree command = new CommandTree("qualityeconomy")
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
        .executes(this::changeAllEntries)));
  
  private void reload(CommandSender sender, CommandArguments args) {
    CompletableFuture.runAsync(() -> {
      Timer timer = new Timer("reload()");
      StorageManager.endStorageProcesses();
      QualityEconomy.getQualityConfig().load();
      QualityEconomy.getMessageConfig().load();
      QualityEconomy.getCurrencyConfig().load();
      MainCommand.load();
      StorageManager.initStorageProcesses(QualityEconomy.getInstance());
      timer.end();
      sender.sendMessage(Component.text("Reloading QualityEconomy...", NamedTextColor.GRAY));
    });
  }
  
  private void reloadMessages(CommandSender sender, CommandArguments args) {
    QualityEconomy.getMessageConfig().load();
    sender.sendMessage(Component.text("Reloading QualityEconomy messages.yml...", NamedTextColor.GRAY));
  }
  
  private void resetDatabase(ConsoleCommandSender sender, CommandArguments args) {
    StorageManager.getActiveStorageType().wipeDatabase();
    sender.sendMessage(Component.text("Resetting database...", NamedTextColor.RED));
  }
  
  private void importDatabase(CommandSender sender, CommandArguments args) {
    String importable = (String) args.get("importable");
    if (Misc.equals(importable, "Essentials"))
      transferPluginData(importable, sender);
    else {
      boolean completed = false;
      try {
        completed = StorageManager.importData(importable).get();
      } catch (InterruptedException | ExecutionException exception) {
        Logger.logError("Error while importing database", exception);
      }
      if (completed)
        sender.sendMessage(Component.text("Imported Database", NamedTextColor.GREEN));
      else
        sender.sendMessage(Component.text("Failed to import Database", NamedTextColor.RED));
    }
  }
  
  private void transferPluginData(String plugin, CommandSender sender) {
  
  }
  
  private void exportDatabase(CommandSender sender, CommandArguments args) {
    StorageManager.exportData(StorageManager.ExportType.NORMAL);
    sender.sendMessage(Component.text("Exporting database", NamedTextColor.GREEN));
  }
  
  private void createFakeEntries(CommandSender sender, CommandArguments args) {
    int entries = (int) args.get("entries");
    AccountManager.createFakeAccounts(entries);
  }
  
  private void changeAllEntries(CommandSender sender, CommandArguments args) {
    AccountManager.changeAllAccounts();
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
