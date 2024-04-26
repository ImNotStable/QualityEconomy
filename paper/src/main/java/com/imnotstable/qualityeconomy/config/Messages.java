package com.imnotstable.qualityeconomy.config;

import com.imnotstable.qualityeconomy.QualityEconomy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class Messages extends BaseConfig {
  
  private final HashMap<String, String> MESSAGES = new HashMap<>();
  
  public Messages(@NotNull QualityEconomy plugin) {
    super(plugin, "messages.yml");
    load();
  }
  
  public static String getMessage(MessageType id) {
    return QualityEconomy.getMessageConfig().MESSAGES.get(id.getValue());
  }
  
  public static void sendParsedMessage(CommandSender sender, MessageType id, String... tags) {
    sender.sendMessage(getParsedMessage(id, tags));
  }
  
  public static void sendParsedMessage(CommandSender sender, String message, String... tags) {
    sender.sendMessage(getParsedMessage(message, tags));
  }
  
  public static Component getParsedMessage(MessageType id, String... tags) {
    return getParsedMessage(getMessage(id), tags);
  }
  
  public static Component getParsedMessage(String message, String... tags) {
    if (tags.length == 0)
      return MiniMessage.miniMessage().deserialize(message);
    if (tags.length % 2 != 0)
      throw new IllegalArgumentException("Invalid number of tags, found odd length when even is required");
    TagResolver[] tagResolvers = new TagResolver[tags.length / 2];
    for (int i = 0; i < tags.length; i += 2)
      tagResolvers[i / 2] = TagResolver.resolver(tags[i], Tag.selfClosingInserting(Component.text(tags[i + 1])));
    return MiniMessage.miniMessage().deserialize(message, tagResolvers);
  }
  
  public void load() {
    super.load(true);
    for (String path : config.getKeys(true))
      MESSAGES.put(path, config.getString(path, ""));
  }
  
}
