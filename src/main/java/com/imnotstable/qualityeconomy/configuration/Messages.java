package com.imnotstable.qualityeconomy.configuration;

import com.imnotstable.qualityeconomy.QualityEconomy;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;

public class Messages {
  
  private static final String RESOURCE_NAME = "messages.yml";
  private static final File PATH = QualityEconomy.getInstance().getDataFolder();
  private static final HashMap<String, String> messages = new HashMap<>();
  
  public static void loadMessages() {
    File file = new File(PATH, RESOURCE_NAME);
    if (!file.exists())
      QualityEconomy.getInstance().saveResource(RESOURCE_NAME, false);
    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
    for (String path : configuration.getKeys(true)) {
      messages.put(path, configuration.getString(path, ""));
    }
  }
  
  public static String getMessage(String id) {
    return messages.getOrDefault(id, "");
  }
}
