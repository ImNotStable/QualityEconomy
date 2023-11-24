package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.storage.DBTransferUtils;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MainCommand {
  
  private static final Pattern IMPORT_FILE_PATTERN = Pattern.compile("^QualityEconomy \\d{4}.\\d{2}.\\d{2} \\d{2}-\\d{2}\\.json$");
  private static boolean isRegistered;
  private static final CommandTree command = new CommandTree("qualityeconomy")
    .withAliases("qe")
    .withPermission("qualityeconomy.admin")
    .then(new LiteralArgument("help")
      .executes(MainCommand::help))
    .then(new LiteralArgument("reload")
      .executes(MainCommand::reload)
      .then(new LiteralArgument("messages")
        .executes(MainCommand::reloadMessages))
      .then(new LiteralArgument("configuration")
        .executes(MainCommand::reloadConfiguration)))
    .then(new LiteralArgument("database")
      .then(new LiteralArgument("import")
        .then(new GreedyStringArgument("fileName")
          .replaceSuggestions(ArgumentSuggestions.strings(info -> getImportableFiles()))
          .executes(MainCommand::importDatabase)))
      .then(new LiteralArgument("export")
        .executes(MainCommand::exportDatabase))
      .then(new LiteralArgument("createFakeEntries")
        .withRequirement(sender -> TestToolkit.DEBUG_MODE)
        .then(new IntegerArgument("entries", 1)
          .executes(MainCommand::createFakeEntries))))
    .then(new LiteralArgument("economy")
      .withRequirement(sender -> Configuration.areCustomCurrenciesEnabled())
      .then(new LiteralArgument("createCustomCurrency")
        .then(new StringArgument("name")
          .executes(MainCommand::createCustomCurrency)))
      .then(new LiteralArgument("deleteCustomCurrency")
        .then(new StringArgument("name")
          .replaceSuggestions(ArgumentSuggestions.strings(info -> CustomCurrencies.getCustomCurrencies().toArray(new String[0])))
          .executes(MainCommand::deleteCustomCurrency)
        )));
  
  public static void register() {
    if (isRegistered)
      return;
    command.register();
    isRegistered = true;
  }
  
  public static void unregister() {
    if (!isRegistered)
      return;
    CommandAPI.unregister("qualityeconomy", true);
    isRegistered = false;
  }
  
  private static void help(CommandSender sender, CommandArguments args) {
    sender.sendMessage(Component.text("Find any bugs or think of a suggestion?", NamedTextColor.GRAY));
    sender.sendMessage(Component.text("Let me know here!", NamedTextColor.YELLOW, TextDecoration.UNDERLINED, TextDecoration.ITALIC)
      .clickEvent(ClickEvent.openUrl("https://github.com/ImNotStable/QualityEconomy/issues")));
    sender.sendMessage(Component.empty());
    sender.sendMessage(Component.text("Need help with the plugin?", NamedTextColor.GRAY));
    sender.sendMessage(Component.text("Join my Discord here!", NamedTextColor.YELLOW, TextDecoration.UNDERLINED, TextDecoration.ITALIC)
      .clickEvent(ClickEvent.openUrl("https://discord.gg/PzCGk5XpUU")));
  }
  
  private static void reload(CommandSender sender, CommandArguments args) {
    synchronized (StorageManager.lock) {
      TestToolkit.Timer timer = new TestToolkit.Timer("Reloading QualityEconomy...");
      StorageManager.endStorageProcesses();
      Configuration.load();
      Messages.load();
      CommandManager.unregisterCommands();
      CommandManager.registerCommands();
      StorageManager.initStorageProcesses();
      timer.end("Reloaded QualityEconomy");
    }
    sender.sendMessage(Component.text("Reloading QualityEconomy...", NamedTextColor.GRAY));
  }
  
  private static void reloadMessages(CommandSender sender, CommandArguments args) {
    Messages.load();
    sender.sendMessage(Component.text("Reloading QualityEconomy messages.yml...", NamedTextColor.GRAY));
  }
  
  private static void reloadConfiguration(CommandSender sender, CommandArguments args) {
    Configuration.load();
    sender.sendMessage(Component.text("Reloading QualityEconomy config.yml...", NamedTextColor.GRAY));
  }
  
  private static void importDatabase(CommandSender sender, CommandArguments args) {
    sender.sendMessage(Component.text("Importing Database...", NamedTextColor.GRAY));
    DBTransferUtils.importDatabase(args.get("fileName").toString());
    sender.sendMessage(Component.text("Imported Database", NamedTextColor.GREEN));
  }
  
  private static void exportDatabase(CommandSender sender, CommandArguments args) {
    sender.sendMessage(Component.text("Exporting Database...", NamedTextColor.GRAY));
    DBTransferUtils.exportDatabase("plugins/QualityEconomy/");
    sender.sendMessage(Component.text("Exported Database", NamedTextColor.GREEN));
  }
  
  private static void createFakeEntries(CommandSender sender, CommandArguments args) {
    try {
      int entries = (int) args.get("entries");
      AccountManager.createFakeAccounts(entries != 0 ? entries : 10);
    } catch (NumberFormatException ignored) {
      sender.sendMessage(Component.text("Invalid amount.", NamedTextColor.RED));
    }
  }
  
  private static void createCustomCurrency(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("name");
    if (CustomCurrencies.getCustomCurrencies().contains(currency)) {
      sender.sendMessage(Component.text("That currency already exists", NamedTextColor.RED));
      return;
    }
    sender.sendMessage(Component.text("Creating custom currency \"" + currency + "\"", NamedTextColor.GRAY));
    CustomCurrencies.createCustomCurrency(currency);
  }
  
  private static void deleteCustomCurrency(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("name");
    if (!CustomCurrencies.getCustomCurrencies().contains(currency)) {
      sender.sendMessage(Component.text("That currency does not exist", NamedTextColor.RED));
      return;
    }
    sender.sendMessage(Component.text("Deleting custom currency \"" + currency + "\"", NamedTextColor.GRAY));
    CustomCurrencies.deleteCustomCurrency(currency);
  }
  
  private static String[] getImportableFiles() {
    File[] pluginFiles = Optional.ofNullable(QualityEconomy.getInstance().getDataFolder().listFiles((dir, name) ->
      IMPORT_FILE_PATTERN.matcher(name).matches())).orElse(new File[0]);
    File[] backupFiles = Optional.ofNullable(new File(QualityEconomy.getInstance().getDataFolder(), "backup").listFiles((dir, name) ->
      IMPORT_FILE_PATTERN.matcher(name).matches())).orElse(new File[0]);
    
    return Stream.concat(Arrays.stream(pluginFiles), Arrays.stream(backupFiles))
      .map(File::getName)
      .toArray(String[]::new);
  }
  
}
