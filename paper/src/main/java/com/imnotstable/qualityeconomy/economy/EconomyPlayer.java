package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import lombok.Getter;
import org.bukkit.OfflinePlayer;

@Getter
public class EconomyPlayer extends Account {
  
  private final OfflinePlayer offlineplayer;
  
  private EconomyPlayer(OfflinePlayer offlineplayer) {
    super(QualityEconomyAPI.getAccount(offlineplayer.getUniqueId()));
    this.offlineplayer = offlineplayer;
  }
  
  public static EconomyPlayer of(OfflinePlayer offlineplayer) {
    return new EconomyPlayer(offlineplayer);
  }
  
}
