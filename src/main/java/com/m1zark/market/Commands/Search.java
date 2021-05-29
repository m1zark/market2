package com.m1zark.market.Commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.market.UI.MainUI;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class Search implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of("Only players may use this command."));

        Chat.sendMessage(src, "&a/market search <item|player> <search>");
        return CommandResult.success();
    }

    public static class ItemSearch implements CommandExecutor {
        @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) throw new CommandException(Text.of("Only players can open the Market listings!"));

            Optional<String> oit = args.getOne(Text.of("item"));
            if (oit.isPresent()) {
                ((Player)src).openInventory((new MainUI((Player)src, 1, true, "item", oit.get())).getInventory());
            } else {
                Chat.sendMessage(src, "&cYou must enter something to search for!");
            }

            return CommandResult.success();
        }
    }

    public static class NameSearch implements CommandExecutor {
        @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) throw new CommandException(Text.of("Only players can open the Market listings!"));

            Optional<User> ou = args.getOne(Text.of("player"));
            if (ou.isPresent()) {
                ((Player)src).openInventory((new MainUI((Player)src, 1, true, "player", ou.get().getUniqueId().toString())).getInventory());
            } else {
                Chat.sendMessage(src, "&cInvalid player name.");
            }

            return CommandResult.success();
        }
    }
}
