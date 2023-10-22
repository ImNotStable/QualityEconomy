package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.Logger;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainCommand {
  
  private static final Pattern IMPORT_FILE_PATTERN = Pattern.compile("^QualityEconomy \\d{4}.\\d{2}.\\d{2} \\d{2}-\\d{2}\\.json$");
  
  public static void loadMainCommand() {
    new CommandTree("qualityeconomy")
      .withAliases("qe")
      .withPermission("qualityeconomy.admin")
      .then(new LiteralArgument("reload")
        .executes((sender, args) -> {
          reload();
          sender.sendMessage(Component.text("Reloading QualityEconomy...", NamedTextColor.GRAY));
        })
        .then(new LiteralArgument("messages")
          .executes((sender, args) -> {
            Messages.loadMessages();
            sender.sendMessage(Component.text("Reloading QualityEconomy messages.yml...", NamedTextColor.GRAY));
          }))
        .then(new LiteralArgument("configuration")
          .executes((sender, args) -> {
            Configuration.loadConfiguration();
            sender.sendMessage(Component.text("Reloading QualityEconomy config.yml...", NamedTextColor.GRAY));
          })))
      .then(new LiteralArgument("database")
        .then(new LiteralArgument("import")
          .then(new GreedyStringArgument("fileName")
            .includeSuggestions(ArgumentSuggestions.strings(getImportableFiles()))
            .executes((sender, args) -> {
              sender.sendMessage(Component.text("Importing Database...", NamedTextColor.GRAY));
              StorageManager.importDatabase(args.get("fileName").toString());
              sender.sendMessage(Component.text("Imported Database", NamedTextColor.GREEN));
            })))
        .then(new LiteralArgument("export")
          .executes((sender, args) -> {
            sender.sendMessage(Component.text("Exporting Database...", NamedTextColor.GRAY));
            StorageManager.exportDatabase("plugins/QualityEconomy/");
            sender.sendMessage(Component.text("Exported Database", NamedTextColor.GREEN));
          }))
        .then(new LiteralArgument("createFakeEntries")
          .withRequirement(sender -> TestToolkit.DEBUG_MODE)
          .then(new IntegerArgument("entries", 1, 9999)
            .executes((sender, args) -> {
              try {
                int entries = (int) args.get("entries");
                AccountManager.createFakeAccounts(entries != 0 ? entries : 10);
              } catch (NumberFormatException ignored) {
                sender.sendMessage(Component.text("Invalid amount.", NamedTextColor.RED));
              }
            }))))
      .register();
  }
  
  private static void reload() {
    if (StorageManager.lock) {
      Logger.log(Component.text("Cancelled reload process (ENTRY_LOCK)", NamedTextColor.RED));
      return;
    }
    TestToolkit.Timer timer = new TestToolkit.Timer("Reloading QualityEconomy...");
    StorageManager.endStorageProcesses();
    Configuration.loadConfiguration();
    Messages.loadMessages();
    StorageManager.initStorageProcesses();
    timer.end("Reloaded QualityEconomy");
  }
  
  private static List<String> getImportableFiles() {
    List<String> importableFiles = new ArrayList<>();
    File[] files = QualityEconomy.getInstance().getDataFolder().listFiles();
    
    if (files != null) {
      
      for (File file : files) {
        String name = file.getName();
        if (IMPORT_FILE_PATTERN.matcher(name).matches()) {
          importableFiles.add(name);
        }
      }
    }
    return importableFiles;
  }
}
