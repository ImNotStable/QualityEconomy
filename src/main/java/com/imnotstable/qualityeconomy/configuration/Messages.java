package com.imnotstable.qualityeconomy.configuration;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.QualityError;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Messages {
  
  private static final File messageFile = new File(QualityEconomy.getInstance().getDataFolder(), "messages.yml");
  private static final HashMap<String, String> messages = new HashMap<>();
  
  public static void sendParsedMessage(MessageType id, CommandSender sender) {
    sender.sendMessage(getParsedMessage(id));
  }
  
  public static void sendParsedMessage(MessageType id, String[] tags, CommandSender sender) {
    sender.sendMessage(getParsedMessage(id, tags));
  }
  
  public static Component getParsedMessage(MessageType id, String[] tags) {
    int tagsReq = id.getTags().length;
    if (tags.length != tagsReq)
      throw new IllegalArgumentException("Found " + tags.length + " tags when required " + tagsReq);
    TagResolver[] tagResolvers = new TagResolver[tagsReq];
    for (int i = 0; i < tagsReq; i++) {
      tagResolvers[i] = TagResolver.resolver(id.getTags()[i], Tag.selfClosingInserting(Component.text(tags[i])));
    }
    return MiniMessage.miniMessage().deserialize(messages.get(id.getValue()), tagResolvers);
  }
  
  public static Component getParsedMessage(MessageType id) {
    return MiniMessage.miniMessage().deserialize(messages.get(id.getValue()));
  }
  
  public static void load() {
    if (!messageFile.exists())
      QualityEconomy.getInstance().saveResource("messages.yml", false);
    else
      update();
    reload();
  }
  
  public static void reload() {
    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(messageFile);
    for (String path : configuration.getKeys(true)) {
      messages.put(path, configuration.getString(path, ""));
    }
  }
  
  public static void update() {
    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(messageFile);
    Map<String, Object> values = new HashMap<>();
    configuration.getKeys(true).forEach(key -> values.putIfAbsent(key, configuration.get(key)));
    QualityEconomy.getInstance().saveResource("messages.yml", true);
    YamlConfiguration finalConfiguration = YamlConfiguration.loadConfiguration(messageFile);
    values.forEach((key, value) -> {
      if (finalConfiguration.contains(key))
        finalConfiguration.set(key, value);
    });
    try {
      finalConfiguration.save(messageFile);
    } catch (IOException exception) {
      new QualityError("Failed to update messages.yml", exception).log();
    }
  }
}
