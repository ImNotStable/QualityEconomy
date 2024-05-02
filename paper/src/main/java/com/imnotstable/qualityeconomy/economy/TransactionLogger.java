package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransactionLogger {
  
  private static final File dir = new File(QualityEconomy.getInstance().getDataFolder(), "transactions");
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  
  public static void log(EconomicTransaction transaction) {
    if (!dir.exists())
      if (!dir.mkdirs()) {
        Logger.logError("Failed to create transaction log directory");
        return;
      }
    
    String message = getLogMessage(transaction);
    
    File dataFile = getFile(transaction.getSender());
    if (createPlayerData(dataFile))
      log(dataFile, message);
    
    for (EconomyPlayer player : transaction.getEconomyPlayers()) {
      if (transaction.getSender() instanceof Player sender && player.getUniqueId().equals(sender.getUniqueId()))
        continue;
      dataFile = new File(dir, player.getUniqueId() + ".txt");
      if (createPlayerData(dataFile))
        log(dataFile, message);
    }
  }
  
  private static @NotNull File getFile(CommandSender sender) {
    if (sender instanceof Player player)
      return new File(dir, player.getUniqueId() + ".txt");
    else if (sender instanceof Entity entity)
      return new File(dir, entity.getUniqueId() + ".txt");
    else if (sender instanceof ConsoleCommandSender)
      return new File(dir, "console.txt");
    else if (sender instanceof BlockCommandSender block)
      return new File(dir, "command_block-" + block.getBlock().getLocation() + ".txt");
    else
      throw new IllegalArgumentException("Unknown sender type: " + sender.getClass().getName());
  }
  
  private static void log(File file, String message) {
    try (FileWriter writer = new FileWriter(file, true)) {
      writer.write(message);
    } catch (IOException exception) {
      Logger.logError("Failed to write to transaction log (" + file.getName() + ")", exception);
    }
  }
  
  private static String getLogMessage(EconomicTransaction transaction) {
    StringBuilder message = new StringBuilder();
    if (transaction.isCancelled())
      message.append("[Cancelled] ");
    message.append("[").append(formatter.format(LocalDateTime.now())).append("]")
      .append(transaction.getType().getLogMessage(transaction));
    if (transaction.isSilent())
      message.append(" (Silent)");
    message.append("\n");
    return message.toString();
  }
  
  private static boolean createPlayerData(File file) {
    if (file.exists())
      return true;
    try {
      if (!file.createNewFile())
        throw new Exception("Unknown Reason");
      return true;
    } catch (Exception exception) {
      Logger.logError("Failed to create transaction log (" + file.getName() + ")", exception);
    }
    return false;
  }
  
}
