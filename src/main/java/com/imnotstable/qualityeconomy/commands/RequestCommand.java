package com.imnotstable.qualityeconomy.commands;

import com.imnotstable.qualityeconomy.QualityEconomy;
import com.imnotstable.qualityeconomy.api.QualityEconomyAPI;
import com.imnotstable.qualityeconomy.configuration.Configuration;
import com.imnotstable.qualityeconomy.configuration.MessageType;
import com.imnotstable.qualityeconomy.configuration.Messages;
import com.imnotstable.qualityeconomy.util.CommandUtils;
import com.imnotstable.qualityeconomy.util.Number;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RequestCommand implements Command {
  
  @Getter
  private final String name = "request";
  
  //<Requestee, <Requester, Amount>>
  private Map<UUID, Map<UUID, Double>> requests;
  
  private final CommandTree command = new CommandTree(name)
    .then(new LiteralArgument("toggle")
      .executesPlayer(this::toggleRequests))
    .then(new MultiLiteralArgument("answer", "accept", "deny")
      .withRequirement(sender -> {
        if (!(sender instanceof Player player)) return false;
        return requests.containsKey(player.getUniqueId());
      })
      .then(new PlayerArgument("target")
        .executesPlayer(this::answerRequest)))
    .then(new LiteralArgument("send")
      .then(new PlayerArgument("target")
        .replaceSuggestions(CommandUtils.getOnlinePlayerSuggestion())
        .then(new DoubleArgument("amount", Number.getMinimumValue())
          .executesPlayer(this::request))));
  private boolean isRegistered = false;
  
  public void register() {
    if (isRegistered || !Configuration.isCommandEnabled("request"))
      return;
    command.register();
    requests = new HashMap<>();
    isRegistered = true;
  }
  
  public void unregister() {
    if (!isRegistered)
      return;
    CommandAPI.unregister(name, true);
    requests = null;
    isRegistered = false;
  }
  
  private void toggleRequests(Player sender, CommandArguments args) {
    boolean toggle = !QualityEconomyAPI.isRequestable(sender.getUniqueId());
    QualityEconomyAPI.setRequestable(sender.getUniqueId(), toggle);
    if (toggle) Messages.sendParsedMessage(sender, MessageType.REQUEST_TOGGLE_ON);
    else Messages.sendParsedMessage(sender, MessageType.REQUEST_TOGGLE_OFF);
  }
  
  private void request(Player requester, CommandArguments args) {
    Player requestee = (Player) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(requestee.getUniqueId()), MessageType.PLAYER_NOT_FOUND, requester))
      return;
    if (CommandUtils.requirement(requestee.isOnline(), MessageType.PLAYER_NOT_ONLINE, requester))
      return;
    if (CommandUtils.requirement(QualityEconomyAPI.isRequestable(requestee.getUniqueId()), MessageType.NOT_ACCEPTING_REQUESTS, requester))
      return;
    double amount = Number.roundObj(args.get("amount"));
    if (CommandUtils.requirement(QualityEconomyAPI.hasBalance(requestee.getUniqueId(), amount), MessageType.OTHER_NOT_ENOUGH_MONEY, requester))
      return;
    Messages.sendParsedMessage(requester, MessageType.REQUEST_SEND,
      Number.formatCommas(amount),
      requestee.getName()
    );
    Messages.sendParsedMessage(requestee, MessageType.REQUEST_RECEIVE,
      Number.formatCommas(amount),
      requester.getName()
    );
    UUID requesterUUID = requester.getUniqueId();
    UUID requesteeUUID = requestee.getUniqueId();
    requests.computeIfAbsent(requesteeUUID, uuid -> new HashMap<>()).put(requesterUUID, amount);
    Bukkit.getScheduler().runTaskLater(QualityEconomy.getInstance(), () -> requests.get(requesteeUUID).remove(requesterUUID, amount), 1200);
  }
  
  private void answerRequest(Player requestee, CommandArguments args) {
    String answer = (String) args.get("answer");
    if (answer.equalsIgnoreCase("accept")) accept(requestee, args);
    else if (answer.equalsIgnoreCase("deny")) deny(requestee, args);
  }
  
  private void accept(Player requestee, CommandArguments args) {
    OfflinePlayer requester = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(requester.getUniqueId()), MessageType.PLAYER_NOT_FOUND, requestee))
      return;
    double amount = Number.round(requests.get(requestee.getUniqueId()).get(requester.getUniqueId()));
    if (CommandUtils.requirement(QualityEconomyAPI.hasBalance(requestee.getUniqueId(), amount), MessageType.SELF_NOT_ENOUGH_MONEY, requestee))
      return;
    Messages.sendParsedMessage(requestee, MessageType.REQUEST_ACCEPT_SEND,
      Number.formatCommas(amount),
      requester.getName()
    );
    if (requester.isOnline())
      Messages.sendParsedMessage(requestee, MessageType.REQUEST_ACCEPT_RECEIVE,
        Number.formatCommas(amount),
        requestee.getName()
      );
    QualityEconomyAPI.transferBalance(requester.getUniqueId(), requestee.getUniqueId(), amount);
  }
  
  private void deny(Player requestee, CommandArguments args) {
    OfflinePlayer requester = (OfflinePlayer) args.get("target");
    if (CommandUtils.requirement(QualityEconomyAPI.hasAccount(requester.getUniqueId()), MessageType.PLAYER_NOT_FOUND, requestee))
      return;
    double amount = Number.roundObj(args.get("amount"));
    if (CommandUtils.requirement(QualityEconomyAPI.hasBalance(requestee.getUniqueId(), amount), MessageType.SELF_NOT_ENOUGH_MONEY, requestee))
      return;
    Messages.sendParsedMessage(requestee, MessageType.REQUEST_DENY_SEND,
      Number.formatCommas(amount),
      requester.getName()
    );
    if (requester.isOnline())
      Messages.sendParsedMessage(requestee, MessageType.REQUEST_DENY_RECEIVE,
        Number.formatCommas(amount),
        requestee.getName()
      );
  }
  
}
