package com.imnotstable.qualityeconomy.util;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.CustomCurrencies;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class CommandUtils {
  
  public static boolean requirement(boolean requirement, MessageType errorMessage, CommandSender sender) {
    if (!requirement) {
      Messages.sendParsedMessage(errorMessage, sender);
      return true;
    }
    return false;
  }
  
  public static boolean playerDoesNotExist(UUID uuid, CommandSender sender) {
    return requirement(QualityEconomyAPI.hasAccount(uuid), MessageType.PLAYER_NOT_FOUND, sender);
  }
  
  public static boolean playerDoesNotHaveEnoughMoney(UUID uuid, double requiredBalance, CommandSender sender) {
    return requirement(QualityEconomyAPI.hasBalance(uuid, requiredBalance), MessageType.SELF_NOT_ENOUGH_MONEY, sender);
  }
  
  public static boolean currencyDoesNotExist(String currency, CommandSender sender) {
    return requirement(CustomCurrencies.getCustomCurrencies().contains(currency), MessageType.CURRENCY_NOT_FOUND, sender);
  }
  
}
