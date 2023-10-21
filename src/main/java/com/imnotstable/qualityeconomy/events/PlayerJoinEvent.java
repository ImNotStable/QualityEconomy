package com.imnotstable.qualityeconomy.events;

import com.imnotstable.qualityeconomy.storage.AccountManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerJoinEvent implements Listener {
  
  @EventHandler
  public void on(org.bukkit.event.player.PlayerJoinEvent event) {
    Player player = event.getPlayer();
    AccountManager.updateAccount(AccountManager.getAccount(player.getUniqueId()).setName(player.getName()));
  }
  
}
