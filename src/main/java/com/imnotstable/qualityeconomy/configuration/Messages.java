package com.imnotstable.qualityeconomy.configuration;

import com.imnotstable.qualityeconomy.QualityEconomy;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;

public class Messages {
  
  private static final HashMap<String, String> messages = new HashMap<>();
  
  public static void loadMessages() {
    File file = new File(QualityEconomy.getPluginFolder(), "messages.yml");
    if (!file.exists())
      QualityEconomy.getInstance().saveResource("messages.yml", false);
    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
    for (String path : configuration.getKeys(true)) {
      messages.put(path, configuration.getString(path, ""));
    }
  }
  
  public static String getMessage(String id) {
    return messages.getOrDefault(id, "");
  }
}
