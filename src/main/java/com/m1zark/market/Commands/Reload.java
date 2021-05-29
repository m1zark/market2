package com.m1zark.market.Commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.market.Market;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class Reload implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Market.getInstance().getConfig().reload();
        Market.getInstance().getMsgConfig().reload();

        Chat.sendMessage(src, "&7Market configs successfully reloaded.");

        return CommandResult.success();
    }
}
