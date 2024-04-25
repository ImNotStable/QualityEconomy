package com.imnotstable.qualityeconomy.economy;

import com.imnotstable.qualityeconomy.storage.accounts.AccountManager;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
public class Currency {
  
  private final String name;
  private final double defaultBalance;
  private final String viewCommand;
  private final String[] viewAliases;
  private final String adminCommand;
  private final String[] adminAliases;
  private final String transferCommand;
  private final String[] transferAliases;
  private final String singular;
  private final String plural;
  
  private Currency(@NotNull String name, double defaultBalance,
                   @NotNull String @NotNull [] viewCommands,
                   @NotNull String @NotNull [] adminCommands,
                   @NotNull String @NotNull [] transferCommands,
                   String singular, String plural) {
    this.name = name;
    this.defaultBalance = defaultBalance;
    if (viewCommands.length > 0)
      this.viewCommand = viewCommands[0];
    else
      this.viewCommand = null;
    if (viewCommands.length > 1) {
      this.viewAliases = new String[viewCommands.length - 1];
      System.arraycopy(viewCommands, 1, viewAliases, 0, viewAliases.length);
    } else
      this.viewAliases = new String[0];
    if (adminCommands.length > 0)
      this.adminCommand = adminCommands[0];
    else
      this.adminCommand = null;
    if (adminCommands.length > 1) {
      this.adminAliases = new String[adminCommands.length - 1];
      System.arraycopy(adminCommands, 1, adminAliases, 0, adminAliases.length);
    } else
      this.adminAliases = new String[0];
    if (transferCommands.length > 0)
      this.transferCommand = transferCommands[0];
    else
      this.transferCommand = null;
    if (transferCommands.length > 1) {
      this.transferAliases = new String[transferCommands.length - 1];
      System.arraycopy(transferCommands, 1, transferAliases, 0, transferAliases.length);
    } else
      this.transferAliases = new String[0];
    this.singular = singular;
    this.plural = plural;
  }
  
  public double getBalance(UUID uniqueId) {
    return AccountManager.getAccount(uniqueId).getBalance(name);
  }
  
  public static Currency of(String name, double startingBalance, String[] viewCommands, String[] adminCommands, String[] transferCommands, String singular, String plural) {
    return new Currency(name, startingBalance, viewCommands, adminCommands, transferCommands, singular, plural);
  }
  
}
