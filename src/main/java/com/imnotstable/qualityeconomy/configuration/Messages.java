package com.imnotstable.qualityeconomy.configuration;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Error;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Messages {
  
  private static File file;
  private static final HashMap<String, String> messages = new HashMap<>();
  
  public static String getMessage(MessageType id) {
    return messages.getOrDefault(id.getValue(), "");
  }
  
  public static void load() {
    file = new File(QualityEconomy.getPluginFolder(), "messages.yml");
    if (!file.exists())
      QualityEconomy.getInstance().saveResource("messages.yml", false);
    else
      update();
    reload();
  }
  
  public static void reload() {
    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
    for (String path : configuration.getKeys(true)) {
      messages.put(path, configuration.getString(path, ""));
    }
  }
  
  public static void update() {
    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
    Map<String, Object> values = new HashMap<>();
    configuration.getKeys(true).forEach(key -> values.putIfAbsent(key, configuration.get(key)));
    QualityEconomy.getInstance().saveResource("messages.yml", true);
    YamlConfiguration finalConfiguration = YamlConfiguration.loadConfiguration(file);
    values.forEach((key, value) -> {
      if (finalConfiguration.contains(key))
        finalConfiguration.set(key, value);
    });
    try {
      finalConfiguration.save(file);
    } catch (IOException exception) {
      new Error("Failed to update messages.yml", exception).log();
    }
  }
}
