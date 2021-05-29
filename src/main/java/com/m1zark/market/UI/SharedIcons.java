package com.m1zark.market.UI;

import com.google.common.collect.Lists;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Money;
import com.m1zark.m1utilities.api.Time;
import com.m1zark.market.Market;
import com.m1zark.m1utilities.api.GUI.Icon;
import com.m1zark.market.Utils.Listing;
import com.m1zark.market.Utils.MarketUtils;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SharedIcons {
    public SharedIcons() { }

    public static Icon BorderIcon(int slot, DyeColor color, boolean display) {
        ArrayList<Text> itemLore = new ArrayList<>();
        itemLore.add(Text.of(Chat.embedColours("&aTo list an item on the market, just hold")));
        itemLore.add(Text.of(Chat.embedColours("&athe item in your hand and type:")));
        itemLore.add(Text.of(Chat.embedColours("&b/market sell <price> [quantity]")));
        itemLore.add(Text.of(Chat.embedColours("")));
        itemLore.add(Text.of(Chat.embedColours("&aTo search for an item, or for specific")));
        itemLore.add(Text.of(Chat.embedColours("&aplayer listings use the command:")));
        itemLore.add(Text.of(Chat.embedColours("&b/market search <item|price> [item name|player name]")));
        itemLore.add(Text.of(Chat.embedColours("")));
        itemLore.add(Text.of(Chat.embedColours("&cNote: The price is for the entire stack,")));
        itemLore.add(Text.of(Chat.embedColours("&cnot per item.")));

        ItemStack icon = ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).build();
        icon.offer(Keys.DISPLAY_NAME, Text.of(Chat.embedColours(display ? "&l&3How to list an item:" : "&0")));
        icon.offer(Keys.DYE_COLOR, color);
        if(display) icon.offer(Keys.ITEM_LORE, itemLore);

        return new Icon(slot, icon);
    }

    static Icon pageIcon(int slot, boolean nextOrLast) {
        return new Icon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, nextOrLast ? "pixelmon:trade_holder_right" : "pixelmon:trade_holder_left").get())
                .quantity(1)
                .add(Keys.DISPLAY_NAME, nextOrLast ? Text.of(TextColors.GREEN, "\u2192 ", "Next Page", TextColors.GREEN, " \u2192") : Text.of(TextColors.RED, "\u2190 ", "Previous Page", TextColors.RED, " \u2190"))
                //.keyValue(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Current Page: ", TextColors.DARK_AQUA, curr), Text.of(TextColors.GRAY, "Next Page: ", TextColors.DARK_AQUA, next)))
                .build());
    }

    static Icon refreshIcon(int slot) {
        return new Icon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trade_panel").get())
                .quantity(1)
                .add(Keys.DISPLAY_NAME,Text.of(TextColors.GOLD, "Refresh Listings"))
                .build()
        );
    }

    static Icon playerListingsIcon(int slot, Player player) {
        ItemStack icon = ItemStack.builder().itemType(ItemTypes.CHEST)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, TextFormatting.BOLD, "My Listings"))
                .build();

        ArrayList<Text> itemLore = new ArrayList<>();
        itemLore.add(Text.of(Chat.embedColours("&7Balance: &eP" + Money.getBalance(player))));

        List<Listing> total = Market.getInstance().getSql().getAllListings().stream()
                .filter(listing -> listing.getSellerUUID().equals(player.getUniqueId())).collect(Collectors.toList());

        long expired = total.stream().filter(listing-> listing.checkHasExpired()).count();

        String maxListings = (Market.getInstance().getConfig().getMaxListings() == -1) ? "\u221E" : String.valueOf(Market.getInstance().getConfig().getMaxListings());

        itemLore.add(Text.of(Chat.embedColours("&7Total Listings: &b" + total.size() + "&7/&b" + maxListings)));
        itemLore.add(Text.of(Chat.embedColours("&7Total Expired Listings: &b" + expired + "&7/&b" + total.size())));
        itemLore.add(Text.of(Chat.embedColours("")));
        itemLore.add(Text.of(Chat.embedColours("&bClick here to view/edit all the items you are currently selling.")));
        icon.offer(Keys.ITEM_LORE, itemLore);

        return new Icon(slot, icon);
    }

    static Icon gtsInfo(int slot) {
        ItemStack icon = ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trade_machine").get()).build();
        icon.offer(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&9&lLooking to buy/sell Pok\u00E9mon?")));
        ArrayList<Text> itemLore = new ArrayList<>();
        itemLore.add(Text.of(Chat.embedColours("&aClick this icon to open the GTS.")));
        icon.offer(Keys.ITEM_LORE, itemLore);

        return new Icon(slot, icon);
    }

    public static Icon confirmIcon(int slot) {
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.STAINED_HARDENED_CLAY).quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Confirm Purchase"))
                .add(Keys.DYE_COLOR, DyeColors.LIME)
                .add(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Click here to confirm purchase.")))
                .build());
    }

    public static Icon cancelIcon(int slot) {
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.STAINED_HARDENED_CLAY).quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Cancel Purchase"))
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .add(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Click here to cancel purchase.")))
                .build());
    }

    public static Icon listingInformation(int slot, Listing item) {
        Time time = new Time(item.getExpires().toInstant().toEpochMilli());

        ArrayList<Text> itemLore = new ArrayList<>();
        itemLore.add(Text.of(Chat.embedColours("&7Seller: &b" + MarketUtils.getNameFromUUID(item.getSellerUUID()).get())));
        itemLore.add(Text.of(Chat.embedColours("&7Quantity: &b" + item.getQuantity())));
        itemLore.add(Text.of(Chat.embedColours("&7Price: &bP" + item.getPrice())));
        itemLore.add(Text.of(Chat.embedColours("&7Expires: &b" + time.toString("%1$dd %2$dh %3$dm"))));

        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.PAPER)
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&bListing Information")))
                .add(Keys.ITEM_LORE, itemLore)
                .build());
    }

    static Icon lastMenu(int slot) {
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.STAINED_HARDENED_CLAY)
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Back to Main Menu"))
                .build());
    }
}
