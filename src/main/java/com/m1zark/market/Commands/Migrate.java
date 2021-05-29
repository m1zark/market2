package com.m1zark.market.Commands;

import com.m1zark.m1utilities.api.Chat;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class Migrate implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        Optional<String> type = args.getOne(Text.of("type"));
        type.ifPresent(db-> {
            if(db.equals("toSQL")) {
                com.m1zark.market.Utils.Database.Migrate.migrateSQL();
                Chat.sendMessage(src, "&7All Market data has been migrated to H2.");
            } else {
                com.m1zark.market.Utils.Database.Migrate.migrateH2();
                Chat.sendMessage(src, "&7All Market data has been migrated to H2.");
            }
        });

        return CommandResult.success();
    }
}
