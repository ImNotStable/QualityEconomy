package com.imnotstable.qualityeconomy.hooks.skriptelements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.imnotstable.qualityeconomy.storage.AccountManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@Name("Current balance")
@Description("INSERT_DESCRIPTION")
@Examples("INSERT_EXAMPLE")
@Since("INSERT_VERSION")
public class exprBalance extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(exprBalance.class, Object.class, ExpressionType.COMBINED,
                "[the] balance of %player%",
                "[the] %player%'s balance"
        );
    }

   Expression<Player> playerExpression;
    @Override
    protected Object @NotNull [] get(@NotNull Event event) {
        // return the real player balance from your Hooks.
        var player = playerExpression.getSingle(event);
        if (player == null) {
            return new Object[0];
            // player missing
        }
        return new Object[]{AccountManager.getAccount(player.getUniqueId()).getBalance()};
    }

    @Override
    public boolean isSingle() {
        return playerExpression.isSingle();
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "the " + playerExpression.toString(event, b) + " balance";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, @NotNull ParseResult parseResult) {
        playerExpression = (Expression<Player>) expressions[0];
        if (playerExpression == null) {
            // send some error for e.g. player need to be specified.
            System.out.println("Missing player");
            return false;
        }
        return true;
    }
}
