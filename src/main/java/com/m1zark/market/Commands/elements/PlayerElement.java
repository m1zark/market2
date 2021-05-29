package com.m1zark.market.Commands.elements;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class PlayerElement extends CommandElement {
    public PlayerElement(Text key) {
        super(key);
    }

    @Nullable @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return args.next();
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        List<String> players = new ArrayList<>();
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        Collection<GameProfile> profiles = userStorage.get().getAll();
        profiles.forEach(gameProfile -> players.add(gameProfile.getName().get()));

        final Function<CommandSource, Iterable<String>> suggestions;
        suggestions = (s) -> players;

        String arg = args.nextIfPresent().orElse("");
        return ImmutableList.copyOf(Iterables.filter(suggestions.apply(src), f -> f.startsWith(arg)));
    }
}
