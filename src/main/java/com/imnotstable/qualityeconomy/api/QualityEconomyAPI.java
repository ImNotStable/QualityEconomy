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
  
  public static double getCustomBalance(OfflinePlayer player, String currency) {
    return getCustomBalance(player.getUniqueId(), currency);
  }
  
  public static double getCustomBalance(UUID uuid, String currency) {
    return AccountManager.getAccount(uuid).getCustomBalance(currency);
  }
  
  public static void setCustomBalance(OfflinePlayer player, String currency, double amount) {
    setCustomBalance(player.getUniqueId(), currency, amount);
  }
  
  public static void setCustomBalance(UUID uuid, String currency, double amount) {
    AccountManager.updateAccount(AccountManager.getAccount(uuid).setCustomBalance(currency, amount));
  }
  
  public static void addCustomBalance(OfflinePlayer player, String currency, double amount) {
    addCustomBalance(player.getUniqueId(), currency, amount);
  }
  
  public static void addCustomBalance(UUID uuid, String currency, double amount) {
    setCustomBalance(uuid, currency, getCustomBalance(uuid, currency) + amount);
  }
  
  public static void removeCustomBalance(OfflinePlayer player, String currency, double amount) {
    addCustomBalance(player.getUniqueId(), currency, amount);
  }
  
  public static void removeCustomBalance(UUID uuid, String currency, double amount) {
    setCustomBalance(uuid, currency, getCustomBalance(uuid, currency) - amount);
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
