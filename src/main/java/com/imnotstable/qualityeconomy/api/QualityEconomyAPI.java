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
  
  public double getSecondaryBalance(OfflinePlayer player) {
    return getSecondaryBalance(player.getUniqueId());
  }
  
  public double getSecondaryBalance(UUID uuid) {
    return AccountManager.getAccount(uuid).getSecondaryBalance();
  }
  
  public void setSecondaryBalance(OfflinePlayer player, double amount) {
    setSecondaryBalance(player.getUniqueId(), amount);
  }
  
  public void setSecondaryBalance(UUID uuid, double amount) {
    AccountManager.updateAccount(AccountManager.getAccount(uuid).setSecondaryBalance(amount));
  }
  
  public void addSecondaryBalance(OfflinePlayer player, double amount) {
    addSecondaryBalance(player.getUniqueId(), amount);
  }
  
  public void addSecondaryBalance(UUID uuid, double amount) {
    setSecondaryBalance(uuid, getSecondaryBalance(uuid) + amount);
  }
  
  public void removeSecondaryBalance(OfflinePlayer player, double amount) {
    addSecondaryBalance(player.getUniqueId(), amount);
  }
  
  public void removeSecondaryBalance(UUID uuid, double amount) {
    setSecondaryBalance(uuid, getSecondaryBalance(uuid) - amount);
  }
  
  public void setPayable(OfflinePlayer player, boolean isPayable) {
    setPayable(player.getUniqueId(), isPayable);
  }
  
  public void setPayable(UUID uuid, boolean isPayable) {
    AccountManager.getAccount(uuid).setPayable(isPayable);
  }
  
  public boolean isPayable(OfflinePlayer player) {
    return isPayable(player.getUniqueId());
  }
  
  public boolean isPayable(UUID uuid) {
    return AccountManager.getAccount(uuid).getPayable();
  }
  
}
