package com.imnotstable.qualitygambling;

import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CoinFlipCommand {
  
  private static final Map<Pair<UUID, UUID>, Double> coinFlipBets = new HashMap<>();
  private static final ThreadLocalRandom random = ThreadLocalRandom.current();
  
  @Getter
  private static final CommandTree command = new CommandTree("coinflip")
    .then(new LiteralArgument("send")
      .then(new PlayerArgument("target")
        .then(new DoubleArgument("amount")
          .executesPlayer((sender, args) -> {
            double amount = (double) args.get("amount");
            if (QualityEconomyAPI.hasBalance(sender.getUniqueId(), amount)) {
              sender.sendMessage(Component.text("You don't have enough money to bet that amount!", NamedTextColor.RED));
              return;
            }
            Player target = (Player) args.get("target");
            coinFlipBets.put(Pair.of(sender.getUniqueId(), target.getUniqueId()), amount);
          }))))
    .then(new LiteralArgument("accept")
      .then(new PlayerArgument("target")
        .executesPlayer((sender, args) -> {
          Pair<UUID, UUID> pair = Pair.of(sender.getUniqueId(), ((Player) args.get("target")).getUniqueId());
          if (coinFlipBets.containsKey(pair)) {
            double amount = coinFlipBets.get(pair);
            coinFlipBets.remove(pair);
            if (QualityEconomyAPI.hasBalance(sender.getUniqueId(), amount)) {
              sender.sendMessage(Component.text("You don't have enough money to bet that amount!", NamedTextColor.RED));
              return;
            }
            if (QualityEconomyAPI.hasBalance(pair.value(), amount)) {
              sender.sendMessage(Component.text("The target doesn't have enough money to bet that amount!", NamedTextColor.RED));
              return;
            }
            Player player = (Player) args.get("target");
            if (random.nextBoolean()) {
              QualityEconomyAPI.addBalance(sender.getUniqueId(), amount);
              QualityEconomyAPI.removeBalance(pair.value(), amount);
              sender.sendMessage(Component.text("You won the coin flip and received " + amount + "!", NamedTextColor.GREEN));
              player.sendMessage(Component.text("You lost the coin flip and lost " + amount + "!", NamedTextColor.RED));
            } else {
              QualityEconomyAPI.addBalance(pair.value(), amount);
              QualityEconomyAPI.removeBalance(sender.getUniqueId(), amount);
              sender.sendMessage(Component.text("You lost the coin flip and lost " + amount + "!", NamedTextColor.RED));
              player.sendMessage(Component.text("You won the coin flip and received " + amount + "!", NamedTextColor.GREEN));
            }
          }
        })));
  
}
