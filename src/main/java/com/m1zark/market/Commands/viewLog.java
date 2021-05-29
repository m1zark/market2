package com.m1zark.market.Commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.market.Market;
import com.m1zark.market.MarketInfo;
import com.m1zark.market.Utils.Logs.Log;
import com.m1zark.market.Utils.Logs.LogAction;
import com.m1zark.market.Utils.MarketUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class viewLog implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of("Only players can open the GTS listings!"));

        Optional<String> typeOptional = args.getOne(Text.of("type"));
        Optional<String> playerOptional = args.getOne(Text.of("player"));
        Optional<Integer> timeOptional = args.getOne(Text.of("time"));
        Optional<LogAction> enumOptional = args.getOne(Text.of("enum"));

        if(typeOptional.get().equals("clear")) {
            Market.getInstance().getSql().removeAllLogs();
            Chat.sendMessage(src, "&7All logs have been deleted.");
        } else {
            CompletableFuture.supplyAsync(() -> {
                List<Log> logs = Market.getInstance().getSql().getAllLogs();

                if (playerOptional.isPresent()) logs = logs.stream().filter(log -> log.getPlayer().equalsIgnoreCase(playerOptional.get())).collect(Collectors.toList());
                if (enumOptional.isPresent() && !enumOptional.get().equals(LogAction.All)) logs = logs.stream().filter(log -> log.getLogAction().equals(enumOptional.get())).collect(Collectors.toList());
                logs = logs.stream().filter(log -> checkTime(log, timeOptional.orElse(1))).collect(Collectors.toList());

                return logs;
            }, Market.getInstance().getAsyncExecutorService()).thenAcceptAsync((result) -> {
                if (result.isEmpty()) {
                    Chat.sendMessage(src, "&cThere are no current logs to display.");
                    return;
                }

                result.sort(Comparator.comparing(Log::getTimeStampDate).reversed());

                PaginationList.builder().contents(displayLogs(result))
                        .title(Text.of(Chat.embedColours("&7Market Logs for &a" + playerOptional.orElse("all players"))))
                        .build()
                        .sendTo(src);
            }, Market.getInstance().getSyncExecutorService());
        }

        return CommandResult.success();
    }

    private static List<Text> displayLogs(List<Log> logs) {
        List<Text> texts = new ArrayList<>();
        for (Log log : logs) {
            Text text = Text.of("");
            Text time = Text.builder().color(TextColors.AQUA).append(Text.of("[Date] ")).onHover(TextActions.showText(Text.of(Chat.embedColours("&b" + log.getTimeStamp())))).build();

            switch (log.getLogAction()) {
                case Addition:
                    text = Text.of(time, Chat.embedColours("&b" + log.getPlayer() + " &7added "), buildItemName(log.getListing().getItemDisplayLog()));
                    break;
                case Removal:
                    text = Text.of(time, Chat.embedColours("&a" + MarketUtils.getNameFromUUID(log.getListing().getSellerUUID()).get() + " &7removed "), buildItemName(log.getListing().getItemDisplayLog()));
                    break;
                case Buy:
                    text = Text.of(time, Chat.embedColours("&b" + log.getPlayer() + " &7bought "), buildItemName(log.getListing().getItemDisplayLog()), Chat.embedColours(" &7from &a" + MarketUtils.getNameFromUUID(log.getListing().getSellerUUID()).get()));
                    break;
                case Sell:
                    text = Text.of(time, Chat.embedColours("&a" + MarketUtils.getNameFromUUID(log.getListing().getSellerUUID()).get() + " &7sold "), buildItemName(log.getListing().getItemDisplayLog()), Chat.embedColours(" &7to &b" + log.getPlayer()));
                    break;
            }

            texts.add(text);
        }

        return texts;
    }

    private static boolean checkTime(Log log, int time) {
        SimpleDateFormat ft = new SimpleDateFormat("MM/dd/yyyy @ h:mm a z");
        try {
            Date date = new Date(ft.parse(log.getTimeStamp()).getTime());
            long daysElapsed = ChronoUnit.DAYS.between(date.toInstant() , Instant.now());

            return daysElapsed <= time;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Text buildItemName(ItemStack itemStack) {
        Text displayName;
        TextColor itemColor;

        Optional<Text> displayNameOptional = itemStack.get(Keys.DISPLAY_NAME);

        // If the item has a display name, we'll use that
        if (displayNameOptional.isPresent()) {
            displayName = displayNameOptional.get();
            itemColor = displayName.getColor();

            if (!displayName.getChildren().isEmpty()) {
                itemColor = displayName.getChildren().get(0).getColor();
            }
        } else { // Just grab the item name
            displayName = Text.of(itemStack.getTranslation());
            itemColor = displayName.getColor();

            // Color the item aqua if it has an enchantment
            if(itemStack.get(Keys.ITEM_ENCHANTMENTS).isPresent()) itemColor = TextColors.AQUA;
        }

        // Build the item text with the color
        return Text.builder().color(itemColor)
                .append(Text.of(TextColors.GRAY,"["), displayName, Text.of(TextColors.GRAY,"]"))
                .onHover(TextActions.showItem(itemStack.createSnapshot())).build();
    }
}
