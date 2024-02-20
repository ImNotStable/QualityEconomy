package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.storage.accounts.Account;
import lombok.Getter;
import org.bukkit.OfflinePlayer;

@Getter
public class EconomyPlayer extends Account {
  
  private final OfflinePlayer offlineplayer;
  
  private EconomyPlayer(Account account, OfflinePlayer offlineplayer) {
    super(account);
    this.offlineplayer = offlineplayer;
  }
  
  public static EconomyPlayer of(OfflinePlayer offlineplayer) {
    return new EconomyPlayer(QualityEconomyAPI.getAccount(offlineplayer.getUniqueId()), offlineplayer);
  }
  
}
