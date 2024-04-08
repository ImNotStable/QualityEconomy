package com.imnotstable.qualityeconomy.util.storage;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.util.debug.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class EasyCurrencies {
  
  protected final Set<String> currencies = new HashSet<>();
  
  public @NotNull Set<String> getCurrencies() {
    if (!QualityEconomy.getQualityConfig().CUSTOM_CURRENCIES) {
      Logger.logError("This feature is disabled within QualityEconomy's configuration");
      return new HashSet<>();
    }
    return new HashSet<>(currencies);
  }
  
}
