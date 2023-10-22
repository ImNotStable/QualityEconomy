package com.imnotstable.qualityeconomy.api;

import com.imnotstable.qualityeconomy.storage.AccountManager;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@SuppressWarnings("unused")
public class QualityEconomyAPI {
  
  public static double getBalance(OfflinePlayer player) {
    return getBalance(player.getUniqueId());
  }
  
  public static double getBalance(UUID uuid) {
    return AccountManager.getAccount(uuid).getBalance();
  }
  
  public static void setBalance(OfflinePlayer player, double amount) {
    setBalance(player.getUniqueId(), amount);
  }
  
  public static void setBalance(UUID uuid, double amount) {
    AccountManager.updateAccount(AccountManager.getAccount(uuid).setBalance(amount));
  }
  
  public static void addBalance(OfflinePlayer player, double amount) {
    addBalance(player.getUniqueId(), amount);
  }
  
  public static void addBalance(UUID uuid, double amount) {
    setBalance(uuid, getBalance(uuid) + amount);
  }
  
  public static void removeBalance(OfflinePlayer player, double amount) {
    addBalance(player.getUniqueId(), amount);
  }
  
  public static void removeBalance(UUID uuid, double amount) {
    setBalance(uuid, getBalance(uuid) - amount);
  }
  
  public static double getSecondaryBalance(OfflinePlayer player) {
    return getSecondaryBalance(player.getUniqueId());
  }
  
  public static double getSecondaryBalance(UUID uuid) {
    return AccountManager.getAccount(uuid).getSecondaryBalance();
  }
  
  public static void setSecondaryBalance(OfflinePlayer player, double amount) {
    setSecondaryBalance(player.getUniqueId(), amount);
  }
  
  public static void setSecondaryBalance(UUID uuid, double amount) {
    AccountManager.updateAccount(AccountManager.getAccount(uuid).setSecondaryBalance(amount));
  }
  
  public static void addSecondaryBalance(OfflinePlayer player, double amount) {
    addSecondaryBalance(player.getUniqueId(), amount);
  }
  
  public static void addSecondaryBalance(UUID uuid, double amount) {
    setSecondaryBalance(uuid, getSecondaryBalance(uuid) + amount);
  }
  
  public static void removeSecondaryBalance(OfflinePlayer player, double amount) {
    addSecondaryBalance(player.getUniqueId(), amount);
  }
  
  public static void removeSecondaryBalance(UUID uuid, double amount) {
    setSecondaryBalance(uuid, getSecondaryBalance(uuid) - amount);
  }
  
  public static void setPayable(OfflinePlayer player, boolean isPayable) {
    setPayable(player.getUniqueId(), isPayable);
  }
  
  public static void setPayable(UUID uuid, boolean isPayable) {
    AccountManager.getAccount(uuid).setPayable(isPayable);
  }
  
  public static boolean isPayable(OfflinePlayer player) {
    return isPayable(player.getUniqueId());
  }
  
  public static boolean isPayable(UUID uuid) {
    return AccountManager.getAccount(uuid).getPayable();
  }
  
}
