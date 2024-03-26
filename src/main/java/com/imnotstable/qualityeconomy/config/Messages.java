package com.imnotstable.qualityeconomy.config;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Debug;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Messages {
  
  private final QualityEconomy plugin;
  private final File file;
  private final HashMap<String, String> MESSAGES = new HashMap<>();
  
  public Messages(QualityEconomy plugin) {
    this.plugin = plugin;
    this.file = new File(plugin.getDataFolder(), "messages.yml");
    load();
  }
  
  public static void sendParsedMessage(CommandSender sender, MessageType id, String... tags) {
    sender.sendMessage(getParsedMessage(id, tags));
  }
  
  public static Component getParsedMessage(MessageType id, String... tags) {
    if (tags.length > 0) {
      if (tags.length % 2 != 0)
        throw new IllegalArgumentException("Invalid number of tags, found odd length when even is required");
      TagResolver[] tagResolvers = new TagResolver[tags.length / 2];
      for (int i = 0; i < tags.length; i += 2)
        tagResolvers[i / 2] = TagResolver.resolver(tags[i], Tag.selfClosingInserting(Component.text(tags[i + 1])));
      return MiniMessage.miniMessage().deserialize(QualityEconomy.getQualityMessages().MESSAGES.get(id.getValue()), tagResolvers);
    }
    return MiniMessage.miniMessage().deserialize(QualityEconomy.getQualityMessages().MESSAGES.get(id.getValue()));
  }
  
  public void load() {
    if (!file.exists())
      plugin.saveResource("messages.yml", false);
    else
      update();
    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
    for (String path : configuration.getKeys(true))
      MESSAGES.put(path, configuration.getString(path, ""));
  }
  
  public void update() {
    boolean save = false;
    YamlConfiguration internalMessages;
    YamlConfiguration messages;
    try (InputStream inputStream = plugin.getResource(file.getName());
         InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
      internalMessages = YamlConfiguration.loadConfiguration(inputStreamReader);
      messages = YamlConfiguration.loadConfiguration(file);
    } catch (IOException exception) {
      new Debug.QualityError("Failed to load internal messages.yml", exception).log();
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
          new Debug.QualityError("Failed to update messages.yml", exception).log();
        }
    }
  }
  
}
