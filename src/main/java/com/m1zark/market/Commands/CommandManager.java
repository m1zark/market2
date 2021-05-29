package com.m1zark.market.Commands;

import com.m1zark.market.Commands.elements.PlayerElement;
import com.m1zark.market.Market;
import com.m1zark.market.MarketInfo;
import com.m1zark.market.Utils.Logs.LogAction;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import scala.tools.nsc.backend.icode.analysis.TypeFlowAnalysis;

import java.util.HashMap;

public class CommandManager {
    public void registerCommands(Market plugin) {
        Sponge.getCommandManager().register(plugin, Market, "market");

        plugin.getConsole().ifPresent(console -> console.sendMessages(Text.of(MarketInfo.PREFIX, "Registering commands...")));
    }

    CommandSpec logs = CommandSpec.builder()
            .permission("market.admin.logs")
            .arguments(
                    GenericArguments.choices(Text.of("type"),new HashMap<String,String>(){{put("view","view");put("clear","clear");}}),
                    GenericArguments.optionalWeak(GenericArguments.enumValue(Text.of("enum"), LogAction.class)),
                    GenericArguments.optionalWeak(new PlayerElement(Text.of("player"))),
                    GenericArguments.optionalWeak(GenericArguments.integer(Text.of("time")))
            )
            .executor(new viewLog())
            .build();

    CommandSpec reload = CommandSpec.builder()
            .permission("market.admin.reload")
            .description(Text.of("Reload the market config files."))
            .executor(new Reload())
            .build();

    CommandSpec clear = CommandSpec.builder()
            .permission("market.admin.clear")
            .arguments(GenericArguments.optionalWeak(GenericArguments.integer(Text.of("id"))))
            .description(Text.of("Removes all market data from the database."))
            .executor(new Clear())
            .build();

    CommandSpec help = CommandSpec.builder()
            .permission("market.user.help")
            .description(Text.of("Displays command information in chat."))
            .executor(new Help())
            .build();

    CommandSpec sell = CommandSpec.builder()
            .permission("market.user.sell")
            .arguments(
                    GenericArguments.integer(Text.of("price")),
                    GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
            )
            .description(Text.of("Create a market listing."))
            .executor(new CreateListing())
            .build();

    CommandSpec itemSearch = CommandSpec.builder()
            .executor(new Search.ItemSearch())
            .arguments(GenericArguments.remainingJoinedStrings(Text.of("item")))
            .description(Text.of("List all market listings for a specific item."))
            .build();

    CommandSpec nameSearch = CommandSpec.builder()
            .executor(new Search.NameSearch())
            .arguments(GenericArguments.user(Text.of("player")))
            .description(Text.of("List all market listings for a specific player."))
            .build();

    CommandSpec search = CommandSpec.builder()
            .permission("market.user.search")
            .description(Text.of("List all search options."))
            .child(itemSearch, "item")
            .child(nameSearch, "player")
            .executor(new Search())
            .build();

    CommandSpec migrate = CommandSpec.builder()
            .permission("market.admin.migrate")
            .arguments(GenericArguments.choices(Text.of("type"), new HashMap<String,String>(){{put("toSQL","toSQL");put("toH2","toH2");}}))
            .description(Text.of(""))
            .executor(new Migrate())
            .build();

    CommandSpec Market = CommandSpec.builder()
            .description(Text.of("Market main command."))
            .child(reload, "reload")
            .child(logs, "logs")
            .child(clear, "clear")
            .child(sell, "sell","add")
            .child(search, "search")
            .child(help, "help")
            .child(migrate, "migrate")
            .executor(new Listings())
            .build();
}
