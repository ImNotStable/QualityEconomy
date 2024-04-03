package com.imnotstable.qualityeconomy.config;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.Misc;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;

public final class Messages extends BaseConfig {
  
  private final HashMap<String, String> MESSAGES = new HashMap<>();
  
  public Messages(QualityEconomy plugin) {
    super(plugin, "messages.yml");
    load();
  }
  
  public static void sendParsedMessage(CommandSender sender, MessageType id, String... tags) {
    sender.sendMessage(getParsedMessage(id, tags));
  }
  
  public static String getParsedMessage(MessageType id, String... tags) {
    String message = QualityEconomy.getQualityMessages().MESSAGES.get(id.getValue());
    if (tags.length == 0)
      return Misc.colored(message);
    if (tags.length % 2 != 0)
      throw new IllegalArgumentException("Invalid number of tags, found odd length when even is required");
    for (int i = 0; i < tags.length; i += 2)
      message = message.replace("%" + tags[i] + "%", tags[i + 1]);
    return Misc.colored(message);
  }
  
  public void load() {
    YamlConfiguration configuration = super.baseLoad();
    for (String path : configuration.getKeys(true))
      MESSAGES.put(path, configuration.getString(path, ""));
  }
  
}
