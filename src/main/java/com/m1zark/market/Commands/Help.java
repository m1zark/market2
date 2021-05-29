package com.m1zark.market.Commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class Help implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(Text.of(TextColors.YELLOW, "-------------------", TextColors.AQUA, "Market Help", TextColors.YELLOW, "--------------------"));
        src.sendMessage(Text.of(TextColors.YELLOW, "{}", TextColors.GRAY, "= Required   ", TextColors.YELLOW, "<>", TextColors.GRAY, "= Optional   ", TextColors.YELLOW, "... ", TextColors.GRAY, "= Accepts more than 1"));
        src.sendMessage(Text.EMPTY);
        src.sendMessage(Text.of(TextColors.AQUA, "/market ", TextColors.GRAY, "\u00BB", TextColors.WHITE, " Opens the Market"));
        src.sendMessage(Text.of(TextColors.AQUA, "/market sell|add {price} <quantity> ", TextColors.GRAY, "\u00BB", TextColors.WHITE, " Add the item your holding to the Market."));
        src.sendMessage(Text.of(TextColors.AQUA, "/market search {item} {items...} ", TextColors.GRAY, "\u00BB", TextColors.WHITE, " Search for items in the Market"));
        src.sendMessage(Text.of(TextColors.AQUA, "/market search {player} {name} ", TextColors.GRAY, "\u00BB", TextColors.WHITE, " Search for players in the Market"));
        if (src.hasPermission("market.admin")) {
            src.sendMessage(Text.of(TextColors.AQUA, "/market clear ", TextColors.GRAY, "\u00BB", TextColors.WHITE, " Clear all entries in the Market"));
            src.sendMessage(Text.of(TextColors.AQUA, "/market reload ", TextColors.GRAY, "\u00BB", TextColors.WHITE, " Reload configuration related to Market"));
        }

        src.sendMessage(Text.EMPTY);
        return CommandResult.success();
    }
}
