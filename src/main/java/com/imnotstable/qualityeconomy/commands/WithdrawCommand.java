package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import com.imnotstable.qualityeconomy.util.Number;
import com.imnotstable.qualityeconomy.util.QualityError;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class WithdrawCommand extends AbstractCommand implements Listener {
  
  private final @Getter String name = "withdraw";
  
  private final CommandAPICommand command = new CommandAPICommand(name)
    .withArguments(new DoubleArgument("amount", Number.getMinimumValue()))
    .executesPlayer(this::withdraw);
  private boolean isRegistered = false;
  
  public void register() {
    if (isRegistered || !Configuration.areBanknotesEnabled())
      return;
    command.register();
    isRegistered = true;
  }
  
  public void unregister() {
    if (!isRegistered)
      return;
    CommandAPI.unregister(name, true);
    isRegistered = true;
  }
  
  private void withdraw(Player sender, CommandArguments args) {
    double amount = Number.roundObj(args.get("amount"));
    if (CommandUtils.playerDoesNotHaveEnoughMoney(sender.getUniqueId(), amount, sender))
      return;
    QualityEconomyAPI.removeBalance(sender.getUniqueId(), amount);
    sender.getInventory().addItem(getBankNote(amount, sender));
    Messages.sendParsedMessage(MessageType.WITHDRAW, new String[]{
      Number.formatCommas(amount)
    }, sender);
  }
  
  public ItemStack getBankNote(double amount, Player player) {
    ItemStack item = new ItemStack(Material.PAPER);
    ItemMeta meta = item.getItemMeta();
    meta.displayName(
      Component.text().append(Component.text("$" + amount, NamedTextColor.GREEN), Component.text(" Banknote", NamedTextColor.GRAY)).build()
        .decoration(TextDecoration.ITALIC, false)
    );
    meta.lore(List.of(Component.text(""), Component.text("From: " + player.getName(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
    meta.setCustomModelData(1234567890);
    item.setItemMeta(meta);
    return item;
  }
  
  @EventHandler
  public void on(PlayerInteractEvent event) {
    if (!Configuration.areBanknotesEnabled() || event.getItem() == null || !event.getItem().getType().equals(Material.PAPER) || !event.getAction().isRightClick())
      return;
    ItemMeta meta = event.getItem().getItemMeta();
    if (!meta.hasCustomModelData() || meta.getCustomModelData() != 1234567890)
      return;
    double amount;
    try {
      amount = Double.parseDouble(LegacyComponentSerializer.legacyAmpersand().serialize(meta.displayName()).substring(3).split("&7 ")[0]);
    } catch (NumberFormatException exception) {
      new QualityError("Failed to format number", exception).log();
      return;
    }
    Inventory inventory = event.getPlayer().getInventory();
    int i = inventory.first(event.getItem());
    ItemStack item = inventory.getItem(i);
    item.setAmount(item.getAmount() - 1);
    QualityEconomyAPI.addBalance(event.getPlayer().getUniqueId(), amount);
  }
  
}
