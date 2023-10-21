package com.imnotstable.qualityeconomy.api;

import com.imnotstable.qualityeconomy.storage.AccountManager;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@SuppressWarnings("unused")
public class QualityEconomyAPI {
  
  public double getBalance(OfflinePlayer player) {
    return getBalance(player.getUniqueId());
  }
  
  public double getBalance(UUID uuid) {
    return AccountManager.getAccount(uuid).getBalance();
  }
  
  public void setBalance(OfflinePlayer player, double amount) {
    setBalance(player.getUniqueId(), amount);
  }
  
  public void setBalance(UUID uuid, double amount) {
    AccountManager.updateAccount(AccountManager.getAccount(uuid).setBalance(amount));
  }
  
  public void addBalance(OfflinePlayer player, double amount) {
    addBalance(player.getUniqueId(), amount);
  }
  
  public void addBalance(UUID uuid, double amount) {
    setBalance(uuid, getBalance(uuid) + amount);
  }
  
  public void removeBalance(OfflinePlayer player, double amount) {
    addBalance(player.getUniqueId(), amount);
  }
  
  public void removeBalance(UUID uuid, double amount) {
    setBalance(uuid, getBalance(uuid) - amount);
  }
  
  
  
}
