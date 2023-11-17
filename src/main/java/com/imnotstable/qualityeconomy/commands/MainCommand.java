package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainCommand {
  
  private static final Pattern IMPORT_FILE_PATTERN = Pattern.compile("^QualityEconomy \\d{4}.\\d{2}.\\d{2} \\d{2}-\\d{2}\\.json$");
  
  public static void loadCommand() {
    new CommandTree("qualityeconomy")
      .withAliases("qe")
      .withPermission("qualityeconomy.admin")
      .then(new LiteralArgument("help")
        .executes((sender, args) -> {
          sender.sendMessage(Component.text("Find any bugs or think of a suggestion?", NamedTextColor.GRAY));
          sender.sendMessage(Component.text("Let me know here!", NamedTextColor.YELLOW, TextDecoration.UNDERLINED, TextDecoration.ITALIC)
            .clickEvent(ClickEvent.openUrl("https://github.com/ImNotStable/QualityEconomy/issues")));
          sender.sendMessage(Component.empty());
          sender.sendMessage(Component.text("Need help with the plugin?", NamedTextColor.GRAY));
          sender.sendMessage(Component.text("Join my Discord here!", NamedTextColor.YELLOW, TextDecoration.UNDERLINED, TextDecoration.ITALIC)
            .clickEvent(ClickEvent.openUrl("https://discord.gg/PzCGk5XpUU")));
        }))
      .then(new LiteralArgument("reload")
        .executes((sender, args) -> {
          reload();
          sender.sendMessage(Component.text("Reloading QualityEconomy...", NamedTextColor.GRAY));
        })
        .then(new LiteralArgument("messages")
          .executes((sender, args) -> {
            Messages.load();
            sender.sendMessage(Component.text("Reloading QualityEconomy messages.yml...", NamedTextColor.GRAY));
          }))
        .then(new LiteralArgument("configuration")
          .executes((sender, args) -> {
            Configuration.load();
            sender.sendMessage(Component.text("Reloading QualityEconomy config.yml...", NamedTextColor.GRAY));
          })))
      .then(new LiteralArgument("database")
        .then(new LiteralArgument("import")
          .then(new GreedyStringArgument("fileName")
            .replaceSuggestions(ArgumentSuggestions.strings(info -> getImportableFiles()))
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
          .then(new IntegerArgument("entries", 1)
            .executes((sender, args) -> {
              try {
                int entries = (int) args.get("entries");
                AccountManager.createFakeAccounts(entries != 0 ? entries : 10);
              } catch (NumberFormatException ignored) {
                sender.sendMessage(Component.text("Invalid amount.", NamedTextColor.RED));
              }
            }))))
      .then(new LiteralArgument("economy")
        .then(new LiteralArgument("createCustomCurrency")
          .then(new StringArgument("name")
            .executes((sender, args) -> {
              CustomCurrencies.createCustomCurrency((String) args.get("name"));
            })))
        .then(new LiteralArgument("deleteCustomCurrency")
          .then(new StringArgument("name")
            .replaceSuggestions(ArgumentSuggestions.strings(info -> CustomCurrencies.getCustomCurrencies().toArray(new String[0])))
            .executes((sender, args) -> {
              CustomCurrencies.deleteCustomCurrency((String) args.get("name"));
            })
          )))
      .register();
  }
  
  private static void reload() {
    synchronized (StorageManager.lock) {
      TestToolkit.Timer timer = new TestToolkit.Timer("Reloading QualityEconomy...");
      StorageManager.endStorageProcesses();
      Configuration.load();
      Messages.load();
      CommandManager.unloadCommands();
      CommandManager.loadCommands();
      StorageManager.initStorageProcesses();
      timer.end("Reloaded QualityEconomy");
    }
  }
  
  private static String[] getImportableFiles() {
    List<String> importableFiles = new ArrayList<>();
    File[] files = QualityEconomy.getPluginFolder().listFiles();
    
    if (files != null) {
      
      for (File file : files) {
        String name = file.getName();
        if (IMPORT_FILE_PATTERN.matcher(name).matches()) {
          importableFiles.add(name);
        }
      }
    }
    return importableFiles.toArray(new String[0]);
  }
  
}
