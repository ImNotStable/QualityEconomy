package com.imnotstable.qualityeconomy.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.imnotstable.qualityeconomy.QualityEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateChecker {
  
  public static void load(String rawCurrentVersion) {
    String latestVersion = getLatestVersion();
    if (latestVersion == null)
      return;
    Version currentVersion = new Version(rawCurrentVersion);
    if (Version.compare(currentVersion, new Version(latestVersion)) == -1) {
      new Debug.QualityLogger("QualityEconomy is out of date. Please update it at the link below.", "https://github.com/ImNotStable/QualityEconomy/releases/latest").log();
      if (QualityEconomy.getQualityConfig().UPDATE_NOTIFICATIONS)
        Bukkit.getPluginManager().registerEvents(new Listener() {
          @EventHandler
          public void on(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            if (player.isOp() || player.hasPermission("qualityeconomy.admin")) {
              Misc.sendColoredMessage(player, "&7QualityEconomy is out of date.");
              Misc.sendColoredMessage(player, "&7Please update it at this link.");
              Misc.sendColoredMessage(player, "&ahttps://github.com/ImNotStable/QualityEconomy/releases/latest");
            }
          }
        }, QualityEconomy.getInstance());
    }
  }
  
  private static String getLatestVersion() {
    String url = "https://api.github.com/repos/ImNotStable/QualityEconomy/releases/latest";
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
      JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
      return jsonObject.get("tag_name").getAsString();
    } catch (Exception exception) {
      new Debug.QualityError("Failed to check for update.", exception).log();
    }
    return null;
  }
  
}
