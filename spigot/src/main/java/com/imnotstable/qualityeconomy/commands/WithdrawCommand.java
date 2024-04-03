package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.config.MessageType;
import com.imnotstable.qualityeconomy.config.Messages;
import com.imnotstable.qualityeconomy.economy.EconomicTransaction;
import com.imnotstable.qualityeconomy.economy.EconomicTransactionType;
import com.imnotstable.qualityeconomy.economy.EconomyPlayer;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import com.imnotstable.qualityeconomy.util.Misc;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class WithdrawCommand extends BaseCommand {
  
  @Getter
  private static final NamespacedKey amountKey = new NamespacedKey(QualityEconomy.getInstance(), "amount");
  @Getter
  private static final NamespacedKey ownerKey = new NamespacedKey(QualityEconomy.getInstance(), "owner");
  private final CommandTree command = new CommandTree("withdraw")
    .withPermission("qualityeconomy.withdraw")
    .then(CommandUtils.AmountArgument()
      .executesPlayer(this::withdraw));
  
  public static ItemStack getBankNote(double amount, Player player) {
    ItemStack item = new ItemStack(Material.PAPER);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(Messages.getParsedMessage(MessageType.WITHDRAW_BANKNOTE_DISPLAYNAME,
      "amount", Number.format(amount, Number.FormatType.COMMAS),
      "player", player.getName()));
    meta.setLore(Arrays.stream(Messages.getParsedMessage(MessageType.WITHDRAW_BANKNOTE_LORE,
      "amount", Number.format(amount, Number.FormatType.COMMAS),
      "player", player.getName()).split("\\|\\|")).toList());
    meta.getPersistentDataContainer().set(amountKey, PersistentDataType.DOUBLE, amount);
    meta.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, player.getName());
    item.setItemMeta(meta);
    return item;
  }
  
  public void register() {
    super.register(command, QualityEconomy.getQualityConfig().BANKNOTES);
    Bukkit.getPluginManager().registerEvents(new Listener() {
      
      @SneakyThrows
      @EventHandler
      public void on(PlayerInteractEvent event) {
        if (!QualityEconomy.getQualityConfig().BANKNOTES || Misc.equals(event.getAction(), Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK) || event.getItem() == null || !event.getItem().getType().equals(Material.PAPER))
          return;
        PersistentDataContainer persistentDataContainer = event.getItem().getItemMeta().getPersistentDataContainer();
        if (!persistentDataContainer.has(amountKey, PersistentDataType.DOUBLE) || !persistentDataContainer.has(ownerKey, PersistentDataType.STRING))
          return;
        
        EconomicTransaction.startNewTransaction(EconomicTransactionType.WITHDRAW_CLAIM, 0, EconomyPlayer.of(event.getPlayer())).execute();
      }
      
    }, QualityEconomy.getInstance());
  }
  
  public void unregister() {
    super.unregister(command);
    PlayerInteractEvent.getHandlerList().unregister(QualityEconomy.getInstance());
  }
  
  @SneakyThrows
  private void withdraw(Player sender, CommandArguments args) {
    double amount = Number.roundObj(args.get("amount"));
    if (CommandUtils.requirement(QualityEconomyAPI.hasBalance(sender.getUniqueId(), amount), MessageType.SELF_NOT_ENOUGH_MONEY, sender))
      return;
    if (CommandUtils.requirement(amount >= Number.getMinimumValue(), MessageType.INVALID_NUMBER, sender))
      return;
    EconomicTransaction.startNewTransaction(EconomicTransactionType.WITHDRAW, amount, EconomyPlayer.of(sender)).execute();
  }
  
}
