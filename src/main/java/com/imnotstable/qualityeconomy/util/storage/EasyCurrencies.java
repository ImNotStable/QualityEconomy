package com.imnotstable.qualityeconomy.util.storage;

import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.util.Debug;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class EasyCurrencies {
  
  protected final Set<String> currencies = new HashSet<>();
  
  public @NotNull Set<String> getCurrencies() {
    if (!Configuration.isCustomCurrenciesEnabled()) {
      new Debug.QualityError("This feature is disabled within QualityEconomy's configuration").log();
      return new HashSet<>();
    }
    return new HashSet<>(currencies);
  }
  
}
