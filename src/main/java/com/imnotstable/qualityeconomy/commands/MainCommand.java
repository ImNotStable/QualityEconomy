package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import com.imnotstable.qualityeconomy.util.Debug;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Getter
public class MainCommand extends AbstractCommand {
  
  private final String name = "qualityeconomy";
  
  private final Pattern IMPORT_FILE_PATTERN = Pattern.compile("^QualityEconomy \\d{4}.\\d{2}.\\d{2} \\d{2}-\\d{2}\\.json$");
  private final CommandTree command = new CommandTree(name)
    .withAliases("qe")
    .withPermission("qualityeconomy.admin")
    .then(new LiteralArgument("reload")
      .executes(this::reload)
      .then(new LiteralArgument("messages")
        .executes(this::reloadMessages)))
    .then(new LiteralArgument("database")
      .then(new LiteralArgument("import")
        .then(new GreedyStringArgument("fileName")
          .replaceSuggestions(ArgumentSuggestions.strings(info -> getImportableFiles()))
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
      .withRequirement(sender -> Configuration.areCustomCurrenciesEnabled())
      .then(new LiteralArgument("createCustomCurrency")
        .then(new StringArgument("name")
          .executes(this::createCustomCurrency)))
      .then(new LiteralArgument("deleteCustomCurrency")
        .then(new StringArgument("name")
          .replaceSuggestions(ArgumentSuggestions.strings(info -> StorageManager.getActiveStorageFormat().getCurrencies().toArray(new String[0])))
          .executes(this::deleteCustomCurrency)
        )));
  private boolean isRegistered;
  
  public void register() {
    if (isRegistered)
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
  
  private void reload(CommandSender sender, CommandArguments args) {
    Debug.Timer timer = new Debug.Timer("reload()");
    StorageManager.endStorageProcesses();
    Configuration.load();
    Messages.load();
    CommandManager.unregisterCommands();
    StorageManager.initStorageProcesses();
    CommandManager.registerCommands();
    timer.end();
    sender.sendMessage(Component.text("Reloading QualityEconomy...", NamedTextColor.GRAY));
  }
  
  private void reloadMessages(CommandSender sender, CommandArguments args) {
    Messages.load();
    sender.sendMessage(Component.text("Reloading QualityEconomy messages.yml...", NamedTextColor.GRAY));
  }
  
  private void importDatabase(CommandSender sender, CommandArguments args) {
    sender.sendMessage(Component.text("Importing Database...", NamedTextColor.GRAY));
    StorageManager.importDatabase(args.get("fileName").toString());
    sender.sendMessage(Component.text("Imported Database", NamedTextColor.GREEN));
  }
  
  private void exportDatabase(CommandSender sender, CommandArguments args) {
    sender.sendMessage(Component.text("Exporting Database...", NamedTextColor.GRAY));
    StorageManager.exportDatabase("plugins/QualityEconomy/");
    sender.sendMessage(Component.text("Exported Database", NamedTextColor.GREEN));
  }
  
  private void createFakeEntries(CommandSender sender, CommandArguments args) {
    try {
      int entries = (int) args.get("entries");
      AccountManager.createFakeAccounts(entries != 0 ? entries : 10);
    } catch (NumberFormatException ignored) {
      sender.sendMessage(Component.text("Invalid amount.", NamedTextColor.RED));
    }
  }
  
  private void changeAllEntries(CommandSender sender, CommandArguments args) {
    AccountManager.changeAllAccounts();
  }
  
  private void createCustomCurrency(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("name");
    if (StorageManager.getActiveStorageFormat().getCurrencies().contains(currency)) {
      sender.sendMessage(Component.text("That currency already exists", NamedTextColor.RED));
      return;
    }
    sender.sendMessage(Component.text("Creating custom currency \"" + currency + "\"", NamedTextColor.GRAY));
    StorageManager.getActiveStorageFormat().addCurrency(currency);
  }
  
  private void deleteCustomCurrency(CommandSender sender, CommandArguments args) {
    String currency = (String) args.get("name");
    if (!StorageManager.getActiveStorageFormat().getCurrencies().contains(currency)) {
      sender.sendMessage(Component.text("That currency does not exist", NamedTextColor.RED));
      return;
    }
    sender.sendMessage(Component.text("Deleting custom currency \"" + currency + "\"", NamedTextColor.GRAY));
    StorageManager.getActiveStorageFormat().removeCurrency(currency);
  }
  
  private String[] getImportableFiles() {
    File[] pluginFiles = Optional.ofNullable(QualityEconomy.getInstance().getDataFolder().listFiles((dir, name) ->
      IMPORT_FILE_PATTERN.matcher(name).matches())).orElse(new File[0]);
    File[] backupFiles = Optional.ofNullable(new File(QualityEconomy.getInstance().getDataFolder(), "backup").listFiles((dir, name) ->
      IMPORT_FILE_PATTERN.matcher(name).matches())).orElse(new File[0]);
    
    return Stream.concat(Arrays.stream(pluginFiles), Arrays.stream(backupFiles))
      .map(File::getName)
      .toArray(String[]::new);
  }
  
  
}
