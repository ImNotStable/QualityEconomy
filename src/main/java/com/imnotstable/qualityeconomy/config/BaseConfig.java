package com.imnotstable.qualityeconomy.config;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Debug;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BaseConfig {
  
  protected final File file;
  private final QualityEconomy plugin;
  
  protected BaseConfig(QualityEconomy plugin, String fileName) {
    this.plugin = plugin;
    this.file = new File(plugin.getDataFolder(), fileName);
  }
  
  protected YamlConfiguration baseLoad() {
    if (!file.exists())
      plugin.saveResource(file.getName(), false);
    else
      update();
    return YamlConfiguration.loadConfiguration(file);
  }
  
  protected void update() {
    boolean save = false;
    YamlConfiguration internalMessages;
    YamlConfiguration messages;
    try (InputStream inputStream = plugin.getResource(file.getName());
         InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
      internalMessages = YamlConfiguration.loadConfiguration(inputStreamReader);
      messages = YamlConfiguration.loadConfiguration(file);
    } catch (IOException exception) {
      new Debug.QualityError("Failed to load internal " + file.getName(), exception).log();
      return;
    }
    
    for (String key : internalMessages.getKeys(true))
      if (!messages.contains(key)) {
        messages.set(key, internalMessages.get(key));
        save = true;
      }
    
    for (String key : messages.getKeys(true)) {
      if (!internalMessages.contains(key)) {
        messages.set(key, null);
        save = true;
      }
      
      if (save)
        try {
          messages.save(file);
        } catch (IOException exception) {
          new Debug.QualityError("Failed to update " + file.getName(), exception).log();
        }
    }
  }
  
}
