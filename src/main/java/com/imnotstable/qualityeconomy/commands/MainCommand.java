package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.storage.StorageManager;
import com.imnotstable.qualityeconomy.util.Logger;
import com.imnotstable.qualityeconomy.util.TestToolkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainCommand implements TabExecutor {

  private static final Pattern IMPORT_FILE_PATTERN = Pattern.compile("^QualityEconomy-\\d{4}-\\d{2}-\\d{2}\\.json$");

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

    if (args.length >= 1 && args[0].equals("reload")) {
      if (args.length == 2) {
        if (args[1].equalsIgnoreCase("messages")) {
          Messages.loadMessages();
        } else if (args[1].equalsIgnoreCase("configuration")) {
          Configuration.loadConfiguration();
        }
      }
      reload();
      sender.sendMessage(Component.text("Successfully reloaded!", NamedTextColor.GREEN));
      return true;

    } else if (args.length > 1 && args[0].equals("database")) {

      if (args[1].equals("import") && args.length == 3) {
        StorageManager.importDatabase(args[2]);
        return true;

      } else if (args[1].equals("export")) {
        StorageManager.exportDatabase("plugins/QualityEconomy/");
        return true;

      } else if (args.length == 3 && TestToolkit.DEBUG_MODE && args[1].equalsIgnoreCase("createFakeEntries")) {
        try {
          int entries = Integer.parseInt(args[2]);
          AccountManager.createFakeAccounts(entries != 0 ? entries : 10);
        } catch (NumberFormatException ignored) {
          sender.sendMessage(Component.text("Invalid amount.", NamedTextColor.RED));
        }
        return true;
      }

    }
    sender.sendMessage(Component.text("Invalid usage.", NamedTextColor.RED));
    return false;
  }

  private void reload() {
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

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    List<String> completions = new ArrayList<>();
    if (args.length == 1) {
      completions.add("reload");
      completions.add("database");
    } else if (args.length == 2) {
      if (args[0].equalsIgnoreCase("database")) {
        completions.add("import");
        completions.add("export");
        if (TestToolkit.DEBUG_MODE) {
          completions.add("createFakeEntries");
        }
      } else if (args[0].equalsIgnoreCase("reload")) {
        completions.add("messages");
        completions.add("configuration");
      }
    } else if (args.length == 3) {
      if (args[0].equalsIgnoreCase("database") && args[1].equalsIgnoreCase("import")) {
        File[] files = QualityEconomy.getInstance().getDataFolder().listFiles();

        if (files != null) {

          for (File file : files) {
            String name = file.getName();
            if (IMPORT_FILE_PATTERN.matcher(name).matches()) {
              completions.add(name);
            }

          }
        }
      } else if (args[0].equalsIgnoreCase("database") && args[1].equalsIgnoreCase("createFakeEntries")) {
        completions.add("<entries>");
      }
    }
    return completions;
  }
}
