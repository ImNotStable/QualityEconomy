package com.imnotstable.qualityeconomy.banknotes;

import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.Account;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.util.Error;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.DoubleArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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

public class BankNotes implements Listener {
  
  public static void loadCommand() {
    new CommandAPICommand("withdraw")
      .withArguments(new DoubleArgument("amount", Number.getMinimumValue()))
      .executesPlayer((sender, args) -> {
        double amount = Number.round((double) args.get("amount"));
        Account account = AccountManager.getAccount(sender.getUniqueId());
        double balance = account.getBalance();
        if (balance < amount) {
          sender.sendMessage(Component.text("You do not have enough money", NamedTextColor.RED));
          return;
        }
        AccountManager.updateAccount(account.setBalance(account.getBalance() - amount));
        ItemStack item = BankNotes.getBankNote(amount, sender);
        sender.getInventory().addItem(item);
        sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage(MessageType.WITHDRAW),
          TagResolver.resolver("amount", Tag.selfClosingInserting(Component.text(Number.formatCommas(amount))))));
      })
      .register();
  }
  
  public static void unloadCommand() {
    CommandAPI.unregister("withdraw", true);
  }
  
  public static ItemStack getBankNote(double amount, Player player) {
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
      new Error("Failed to format number", exception).log();
      return;
    }
    Inventory inventory = event.getPlayer().getInventory();
    int i = inventory.first(event.getItem());
    ItemStack item = inventory.getItem(i);
    item.setAmount(item.getAmount() - 1);
    Account account = AccountManager.getAccount(event.getPlayer().getUniqueId());
    AccountManager.updateAccount(account.setBalance(account.getBalance() + amount));
  }
  
}
