package com.imnotstable.qualityeconomy.config;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.Logger;
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
  
  private static final File file = new File(QualityEconomy.getInstance().getDataFolder(), "messages.yml");
  private static final HashMap<String, String> messages = new HashMap<>();
  
  public static void sendParsedMessage(CommandSender sender, MessageType id, String... tags) {
    sender.sendMessage(getParsedMessage(id, tags));
  }
  
  public static Component getParsedMessage(MessageType id, String... tags) {
    if (tags.length > 0) {
      if (tags.length % 2 != 0)
        throw new IllegalArgumentException("Invalid number of tags, found odd length when even is required");
      else
        Logger.log("Length of tags is " + tags.length);
      TagResolver[] tagResolvers = new TagResolver[tags.length / 2];
      for (int i = 0; i < tags.length; i += 2)
        tagResolvers[i / 2] = TagResolver.resolver(tags[i], Tag.selfClosingInserting(Component.text(tags[i + 1])));
      return MiniMessage.miniMessage().deserialize(messages.get(id.getValue()), tagResolvers);
    }
    return MiniMessage.miniMessage().deserialize(messages.get(id.getValue()));
  }
  
  public static void load() {
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
    boolean save = false;
    YamlConfiguration internalMessages;
    YamlConfiguration messages;
    try (InputStream inputStream = QualityEconomy.getInstance().getResource(file.getName());
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
