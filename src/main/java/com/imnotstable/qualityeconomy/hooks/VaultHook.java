package com.imnotstable.qualityeconomy.hooks;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.util.Debug;
import com.imnotstable.qualityeconomy.util.Logger;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import java.util.Collections;
import java.util.List;

public class VaultHook implements Economy {
  
  @Getter
  private static Economy economy;
  
  public static void initVaultHook(QualityEconomy plugin) {
    Bukkit.getServicesManager().register(Economy.class, new VaultHook(), plugin, ServicePriority.Highest);
    RegisteredServiceProvider<Economy> econRSP = Bukkit.getServicesManager().getRegistration(Economy.class);
    if (econRSP == null) {
      new Debug.QualityError("Failed to register economy with Vault").log();
      return;
    }
    economy = econRSP.getProvider();
    Logger.log(Component.text("Successfully registered economy with Vault", NamedTextColor.GREEN));
  }
  
  @Override
  public boolean isEnabled() {
    return getEconomy() != null;
  }
  
  @Override
  public String getName() {
    return QualityEconomy.getInstance().getName();
  }
  
  @Override
  public int fractionalDigits() {
    return Configuration.getDecimalPlaces();
  }
  
  @Override
  public String format(double amount) {
    return String.valueOf(amount);
  }
  
  @Override
  public String currencyNamePlural() {
    return "Dollars";
  }
  
  @Override
  public String currencyNameSingular() {
    return "Dollar";
  }
  
  @Override
  public boolean hasAccount(String player) {
    return hasAccount(Bukkit.getOfflinePlayer(player));
  }
  
  @Override
  public boolean hasAccount(OfflinePlayer player) {
    return QualityEconomyAPI.hasAccount(player.getUniqueId());
  }
  
  @Override
  public boolean hasAccount(String player, String worldName) {
    return hasAccount(player);
  }
  
  @Override
  public boolean hasAccount(OfflinePlayer player, String worldName) {
    return hasAccount(player);
  }
  
  @Override
  public double getBalance(String player) {
    return getBalance(Bukkit.getOfflinePlayer(player));
  }
  
  @Override
  public double getBalance(OfflinePlayer player) {
    return QualityEconomyAPI.getBalance(player.getUniqueId());
  }
  
  @Override
  public double getBalance(String player, String world) {
    return getBalance(player);
  }
  
  @Override
  public double getBalance(OfflinePlayer player, String world) {
    return getBalance(player);
  }
  
  @Override
  public boolean has(String player, double amount) {
    return has(Bukkit.getOfflinePlayer(player), amount);
  }
  
  @Override
  public boolean has(OfflinePlayer player, double amount) {
    return getBalance(player) >= amount;
  }
  
  @Override
  public boolean has(String player, String worldName, double amount) {
    return has(player, amount);
  }
  
  @Override
  public boolean has(OfflinePlayer player, String worldName, double amount) {
    return has(player, amount);
  }
  
  @Override
  public EconomyResponse withdrawPlayer(String player, double amount) {
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
    if (!offlinePlayer.hasPlayedBefore()) {
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player doesn't exist.");
    }
    return withdrawPlayer(offlinePlayer, amount);
  }
  
  @Override
  public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
    if (player == null) {
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player cannot be null.");
    }
    if (amount < 0) {
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw a negative amount.");
    }
    if (!hasAccount(player)) {
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player doesn't exist.");
    }
    if (!has(player, amount)) {
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player doesn't have funds.");
    }
    QualityEconomyAPI.removeBalance(player.getUniqueId(), amount);
    return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
  }
  
  @Override
  public EconomyResponse withdrawPlayer(String player, String worldName, double amount) {
    return withdrawPlayer(player, amount);
  }
  
  @Override
  public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
    return withdrawPlayer(player, amount);
  }
  
  @Override
  public EconomyResponse depositPlayer(String player, double amount) {
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
    if (!offlinePlayer.hasPlayedBefore()) {
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player doesn't exist.");
    }
    return depositPlayer(offlinePlayer, amount);
  }
  
  @Override
  public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
    if (player == null) {
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player cannot be null.");
    }
    if (amount < 0) {
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit a negative amount.");
    }
    if (!hasAccount(player)) {
      return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player doesn't exist.");
    }
    QualityEconomyAPI.addBalance(player.getUniqueId(), amount);
    return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
  }
  
  @Override
  public EconomyResponse depositPlayer(String player, String worldName, double amount) {
    return depositPlayer(player, amount);
  }
  
  @Override
  public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
    return depositPlayer(player, amount);
  }
  
  @Override
  public boolean createPlayerAccount(String player) {
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(player);
    return offlinePlayer != null && createPlayerAccount(offlinePlayer);
  }
  
  @Override
  public boolean createPlayerAccount(OfflinePlayer player) {
    QualityEconomyAPI.createAccount(player.getUniqueId());
    return QualityEconomyAPI.hasAccount(player.getUniqueId());
  }
  
  @Override
  public boolean createPlayerAccount(String player, String worldName) {
    return createPlayerAccount(player);
  }
  
  @Override
  public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
    return createPlayerAccount(player);
  }
  
  @Override
  public boolean hasBankSupport() {
    return false;
  }
  
  @Override
  public EconomyResponse createBank(String name, String player) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }
  
  @Override
  public EconomyResponse createBank(String name, OfflinePlayer player) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }
  
  @Override
  public EconomyResponse deleteBank(String name) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }
  
  @Override
  public EconomyResponse bankBalance(String name) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }
  
  @Override
  public EconomyResponse bankHas(String name, double amount) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }
  
  @Override
  public EconomyResponse bankWithdraw(String name, double amount) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }
  
  @Override
  public EconomyResponse bankDeposit(String name, double amount) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }
  
  @Override
  public EconomyResponse isBankOwner(String name, String player) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }
  
  @Override
  public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }
  
  @Override
  public EconomyResponse isBankMember(String name, String player) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }
  
  @Override
  public EconomyResponse isBankMember(String name, OfflinePlayer player) {
    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts aren't supported.");
  }
  
  @Override
  public List<String> getBanks() {
    return Collections.emptyList();
  }
}
