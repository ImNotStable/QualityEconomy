package com.imnotstable.qualityeconomy.banknotes;

import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.storage.Account;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.DoubleArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.inventory.ItemStack;

public class WithdrawCommand {
  
  
  public static void loadCommand() {
    new CommandAPICommand("withdraw")
      .withArguments(new DoubleArgument("amount", 0.01))
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
        sender.sendMessage(MiniMessage.miniMessage().deserialize(Messages.getMessage("withdraw.withdraw"),
          TagResolver.resolver("amount", Tag.selfClosingInserting(Component.text(Number.formatCommas(amount))))));
      })
      .register();
  }
  
  public static void unloadCommand() {
    CommandAPI.unregister("withdraw", true);
  }
  
}
