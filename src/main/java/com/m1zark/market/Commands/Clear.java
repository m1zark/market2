package com.m1zark.market.Commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.market.Market;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class Clear implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<Integer> id = args.getOne(Text.of("id"));

        if(id.isPresent()) {
            Market.getInstance().getSql().removeListing(id.get());
            Chat.sendMessage(src, "&7Entry " + id.get() + " has been removed.");
        } else {
            Market.getInstance().getSql().clearTables();
            Chat.sendMessage(src, "&7All Market data has been deleted.");
        }

        return CommandResult.success();
    }
}
