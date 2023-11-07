package com.imnotstable.qualityeconomy.configuration;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Error;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
  
  public static void updateMessages() {
    Map<String, Object> values = new HashMap<>();
    File file = new File(QualityEconomy.getPluginFolder(), "messages.yml");
    final YamlConfiguration tempMessages = YamlConfiguration.loadConfiguration(file);
    tempMessages.getKeys(true).forEach(key -> values.putIfAbsent(key, tempMessages.get(key)));
    QualityEconomy.getInstance().saveResource("messages.yml", true);
    YamlConfiguration messages = YamlConfiguration.loadConfiguration(file);
    values.forEach(messages::set);
    try {
      messages.save(file);
    } catch (IOException exception) {
      new Error("Failed to update configuration", exception).log();
    }
    Messages.loadMessages();
  }
  
}
