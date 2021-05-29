package com.m1zark.market.Commands;

import com.google.common.collect.Maps;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.market.Market;
import com.m1zark.market.Utils.Config.MessageConfig;
import com.m1zark.market.Utils.Listing;
import com.m1zark.market.Utils.Logs.Log;
import com.m1zark.market.Utils.Logs.LogAction;
import com.m1zark.market.Utils.MarketUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CreateListing implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of("Only players can add items to the market!"));

        HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
        Player player = (Player) src;

        textOptions.put("max", Optional.of(Market.getInstance().getConfig().getMaxListings()));

        int total = (int) Market.getInstance().getSql().getAllListings().stream().filter(listing -> listing.getSellerUUID().equals(player.getUniqueId())).count();
        if(Market.getInstance().getConfig().getMaxListings() != -1 && total >= Market.getInstance().getConfig().getMaxListings()) throw new CommandException(MessageConfig.getMessage("Messages.Sell-Item.Has-Too-Many", textOptions));

        if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
            ItemStack itemStack = player.getItemInHand(HandTypes.MAIN_HAND).get();

            if(itemStack.isEmpty() || itemStack.getType() == ItemTypes.AIR) throw new CommandException(MessageConfig.getMessage("Messages.Sell-Item.No-Item", textOptions));

            if(isBlacklisted(itemStack)) throw new CommandException(MessageConfig.getMessage("Messages.Sell-Item.Blacklist", textOptions));

            Optional<Integer> oprice = args.getOne(Text.of("price"));
            Optional<Integer> oquan = args.getOne(Text.of("quantity"));

            if(oquan.isPresent() && (oquan.get() > itemStack.getQuantity())) throw new CommandException(MessageConfig.getMessage("Messages.Sell-Item.Quantity", textOptions));

            oprice.ifPresent(price -> {
                try {
                    textOptions.put("price", Optional.of(price));

                    if (price < 1) {
                        Chat.sendMessage(player, MessageConfig.getMessage("Messages.Sell-Item.Negative-Price", textOptions));
                    } else if (price > Market.getInstance().getConfig().getMaxPrice()) {
                        textOptions.put("maxprice", Optional.of(Market.getInstance().getConfig().getMaxPrice()));
                        Chat.sendMessage(player, MessageConfig.getMessage("Messages.Sell-Item.Max-Price", textOptions));
                    } else {
                        int quantity = oquan.orElse(itemStack.getQuantity());
                        long expires = Market.getInstance().getConfig().getListingsTime();

                        Listing listing = new Listing(0, MarketUtils.serializeItem(itemStack), player.getUniqueId(), Integer.toString(price), quantity, Instant.now().plusSeconds(expires).toString());

                        if (!Market.getInstance().getSql().addListing(listing)) {
                            Chat.sendMessage(player, MessageConfig.getMessage("Messages.Sell-Item.Error", textOptions));
                        } else {
                            Log log = new Log(player.getName(), listing, MarketUtils.getDateTimestamp(), LogAction.Addition);
                            Market.getInstance().getSql().addNewLog(log);

                            textOptions.put("count", Optional.of(String.valueOf(quantity)));
                            textOptions.put("player", Optional.of(player.getName()));
                            textOptions.put("item", Optional.of(Inventories.getItemName(itemStack).toPlain()));

                            if (Market.getInstance().getMsgConfig().getServerAnnounce()) {
                                Sponge.getServer().getOnlinePlayers().forEach((pl) -> {
                                    if (pl != player) Chat.sendMessage(pl, MessageConfig.getMessage("Messages.Sell-Item.Announce-Added", textOptions));
                                });
                            }

                            Chat.sendMessage(player, MessageConfig.getMessage("Messages.Sell-Item.Success", textOptions));
                            Inventories.removeItem(player, player.getItemInHand(HandTypes.MAIN_HAND).get(), quantity);
                        }
                    }
                } catch (NumberFormatException e) {
                    Chat.sendMessage(player, MessageConfig.getMessage("Messages.Sell-Item.Invalid-Number", textOptions));
                }
            });
        } else {
            throw new CommandException(MessageConfig.getMessage("Messages.Sell-Item.No-Item", textOptions));
        }

        return CommandResult.success();
    }

    private boolean isBlacklisted(ItemStack item) {
        List<String> blacklist = Market.getInstance().getConfig().getBlacklist();

        for(String s : blacklist) {
            String[] check = s.trim().split("\\s*,\\s*");

            if(check[0].equals(item.getType().getId())) {
                if(check.length > 1) {
                    return check[1].equals(item.toContainer().getString(DataQuery.of("UnsafeDamage")).get());
                }
                return true;
            }
        }

        return false;
    }
}
