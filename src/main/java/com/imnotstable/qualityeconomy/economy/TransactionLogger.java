package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.QualityException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TransactionLogger {
  
  private static final File dir = new File(QualityEconomy.getInstance().getDataFolder(), "transactions");
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  
  public static void log(EconomicTransaction transaction) {
    if (!dir.exists())
      if (dir.mkdirs()) {
        new Debug.QualityError("Failed to create transaction log directory").log();
        return;
      }
    for (EconomyPlayer player : transaction.getEconomyPlayers()) {
      if (!createPlayerData(player.getUniqueId()))
        continue;
      File playerFile = new File(dir, player.getUniqueId() + ".txt");
      try (FileWriter writer = new FileWriter(playerFile, true)) {
        writer.write(getFormattedTime() + transaction.getType().getLogMessage().apply(transaction));
        if (transaction.isSilent())
          writer.write(" (Silent)");
        if (transaction.isCancelled())
          writer.write(" (Cancelled)");
        writer.write("\n");
      } catch (IOException exception) {
        new Debug.QualityError("Failed to write to transaction log (" + player.getUniqueId() + ")", exception).log();
      }
    }
  }
  
  private static boolean createPlayerData(UUID uniqueId) {
    File file = new File(dir, uniqueId + ".txt");
    if (file.exists())
      return true;
    try {
      if (!file.createNewFile())
        throw new QualityException("Unknown Reason");
      return true;
    } catch (IOException | QualityException exception) {
      new Debug.QualityError("Failed to create transaction log (" + uniqueId + ")", exception).log();
    }
    return false;
  }
  
  private static String getFormattedTime() {
    return "[" + formatter.format(LocalDateTime.now()) + "] ";
  }
  
}
