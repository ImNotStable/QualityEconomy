package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.QualityException;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.jetbrains.annotations.Nullable;

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
      if (dir.mkdirs()) {
        Logger.logError("Failed to create transaction log directory");
        return;
      }
    String message = getLogMessage(transaction);
    
    CommandSender sender = transaction.getSender();
    
    File dataFile = getFile(sender);
    if (dataFile != null && createPlayerData(dataFile))
      log(dataFile, message);
    
    for (EconomyPlayer player : transaction.getEconomyPlayers()) {
      dataFile = new File(dir, player.getUniqueId() + ".txt");
      if (createPlayerData(dataFile))
        log(dataFile, message);
    }
  }
  
  @Nullable
  private static File getFile(CommandSender sender) {
    File dataFile = null;
    if (sender instanceof Player player)
      dataFile = new File(dir, player.getUniqueId() + ".txt");
    else if (sender instanceof ConsoleCommandSender)
      dataFile = new File(dir, "console.txt");
    else if (sender instanceof BlockCommandSender block)
      dataFile = new File(dir, "command_block-" + block.getBlock().getLocation() + ".txt");
    else if (sender instanceof CommandMinecart minecart)
      dataFile = new File(dir, "command_minecart-" + minecart.getUniqueId() + ".txt");
    return dataFile;
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
    message.append("[");
    message.append(formatter.format(LocalDateTime.now()));
    message.append("]");
    message.append(transaction.getType().getLogMessage(transaction));
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
        throw new QualityException("Unknown Reason");
      return true;
    } catch (IOException | QualityException exception) {
      Logger.logError("Failed to create transaction log (" + file.getName() + ")", exception);
    }
    return false;
  }
  
}
