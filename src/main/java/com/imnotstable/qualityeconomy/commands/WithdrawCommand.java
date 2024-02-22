package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.economy.EconomicTransaction;
import com.imnotstable.qualityeconomy.economy.EconomicTransactionType;
import com.imnotstable.qualityeconomy.economy.EconomyPlayer;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import com.imnotstable.qualityeconomy.util.ComponentSplit;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class WithdrawCommand extends BaseCommand implements Listener {
  
  @Getter
  private static final NamespacedKey amountKey = new NamespacedKey(QualityEconomy.getInstance(), "amount");
  @Getter
  private static final NamespacedKey ownerKey = new NamespacedKey(QualityEconomy.getInstance(), "owner");
  private final CommandTree command = new CommandTree("withdraw")
    .then(new DoubleArgument("amount", Number.getMinimumValue())
      .executesPlayer(this::withdraw));
  
  public static ItemStack getBankNote(double amount, Player player) {
    ItemStack item = new ItemStack(Material.PAPER);
    ItemMeta meta = item.getItemMeta();
    meta.displayName(Messages.getParsedMessage(MessageType.WITHDRAW_BANKNOTE_DISPLAYNAME,
      Number.format(amount, Number.FormatType.COMMAS), player.getName()).decoration(TextDecoration.ITALIC, false));
    meta.lore(ComponentSplit.split(Messages.getParsedMessage(MessageType.WITHDRAW_BANKNOTE_LORE,
      Number.format(amount, Number.FormatType.COMMAS), player.getName()).decoration(TextDecoration.ITALIC, false), "\\|\\|"));
    meta.getPersistentDataContainer().set(amountKey, PersistentDataType.DOUBLE, amount);
    meta.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, player.getName());
    item.setItemMeta(meta);
    return item;
  }
  
  public void register() {
    super.register(command);
  }
  
  public void unregister() {
    super.unregister(command);
  }
  
  @SneakyThrows
  private void withdraw(Player sender, CommandArguments args) {
    double amount = Number.roundObj(args.get("amount"));
    if (CommandUtils.requirement(QualityEconomyAPI.hasBalance(sender.getUniqueId(), amount), MessageType.SELF_NOT_ENOUGH_MONEY, sender))
      return;
    EconomicTransaction.startNewTransaction(EconomicTransactionType.WITHDRAW, amount, EconomyPlayer.of(sender)).execute();
  }
  
  @SneakyThrows
  @EventHandler
  public void on(PlayerInteractEvent event) {
    if (!Configuration.isCommandEnabled(command.getName()) || !event.getAction().isRightClick() || event.getItem() == null || !event.getItem().getType().equals(Material.PAPER))
      return;
    PersistentDataContainer persistentDataContainer = event.getItem().getItemMeta().getPersistentDataContainer();
    if (!persistentDataContainer.has(amountKey) || !persistentDataContainer.has(ownerKey))
      return;
    
    EconomicTransaction.startNewTransaction(EconomicTransactionType.WITHDRAW_CLAIM, 0, EconomyPlayer.of(event.getPlayer())).execute();
  }
  
}
