package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.QualityException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

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
        new Debug.QualityError("Failed to create transaction log directory").log();
        return;
      }
    String message = getLogMessage(transaction);
    CommandSender sender = transaction.getSender();
    if (sender != null) {
      File senderData = null;
      if (sender instanceof ConsoleCommandSender)
        senderData = new File(dir, "console.txt");
      else if (sender instanceof Player player)
        senderData = new File(dir, player.getUniqueId() + ".txt");
      if (senderData != null && createPlayerData(senderData))
        log(senderData, message);
    }
    
    for (EconomyPlayer player : transaction.getEconomyPlayers()) {
      File playerData = new File(dir, player.getUniqueId() + ".txt");
      if (createPlayerData(playerData))
        log(playerData, message);
    }
  }
  
  private static void log(File file, String message) {
    try (FileWriter writer = new FileWriter(file, true)) {
      writer.write(message);
    } catch (IOException exception) {
      new Debug.QualityError("Failed to write to transaction log (" + file.getName() + ")", exception).log();
    }
  }
  
  private static String getLogMessage(EconomicTransaction transaction) {
    StringBuilder message = new StringBuilder();
    if (transaction.isCancelled())
      message.append("[Cancelled] ");
    message.append(getFormattedTime());
    message.append(transaction.getType().getLogMessage().apply(transaction));
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
      new Debug.QualityError("Failed to create transaction log (" + file.getName() + ")", exception).log();
    }
    return false;
  }
  
  private static String getFormattedTime() {
    return "[" + formatter.format(LocalDateTime.now()) + "] ";
  }
  
}
