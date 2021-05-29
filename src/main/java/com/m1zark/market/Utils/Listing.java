package com.m1zark.market.Utils;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.m1utilities.api.Time;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.time.Instant;
import java.util.*;

@Getter
public class Listing {
    private int ListingID;
    private String Item;
    private UUID SellerUUID;
    private String Price;
    private int Quantity;
    private Date expires;
    private String expiresRaw;
    private String itemName;

    public Listing(int ID, String item, UUID uuid, String price, int quantity, String expires) {
        this.ListingID = ID;
        this.Item = item;
        this.SellerUUID = uuid;
        this.Price = price;
        this.Quantity = quantity;
        this.expires = Date.from(Instant.parse(expires));
        this.expiresRaw = expires;
        this.itemName = Inventories.getItemName(MarketUtils.deserializeItemStack(this.Item).get()).toPlain();
    }

    public boolean checkHasExpired() {
        return getExpires().before(Date.from(Instant.now()));
    }

    public ItemStack getItemDisplay() {
        ItemStack Item = MarketUtils.deserializeItemStack(this.Item).get();
        Item.setQuantity(this.Quantity);

        this.setItemData(Item, true);
        return ItemStack.builder().fromContainer(Item.toContainer().set(DataQuery.of("UnsafeData","ListingID"), this.ListingID)).build();
    }

    public ItemStack getItemDisplayLog() {
        ItemStack Item = MarketUtils.deserializeItemStack(this.Item).get();
        Item.setQuantity(this.Quantity);

        this.setItemData(Item, false);
        return ItemStack.builder().fromContainer(Item.toContainer().set(DataQuery.of("UnsafeData","ListingID"), this.ListingID)).build();
    }

    public ItemStack getItemNoData(){
        ItemStack Item = MarketUtils.deserializeItemStack(this.Item).get();
        Item.setQuantity(this.Quantity);

        return Item;
    }

    public ItemStack getItemPlayer() {
        ItemStack Item = MarketUtils.deserializeItemStack(this.Item).get();
        Item.setQuantity(this.Quantity);

        Time time = new Time(this.expires.toInstant().toEpochMilli());

        ArrayList<Text> itemLore = new ArrayList<>();
        Item.get(Keys.ITEM_LORE).ifPresent(lore -> lore.forEach(text -> itemLore.add(Text.of(text))));
        itemLore.add(Text.of(Chat.embedColours("&7--------------------")));
        itemLore.add(Text.of(Chat.embedColours("&7Quantity: &b" + this.Quantity)));
        itemLore.add(Text.of(Chat.embedColours("&7Price: &bP" + this.Price)));
        itemLore.add(Text.of(Chat.embedColours("&7Expires: &b" + time.toString("%1$dd %2$dh %3$dm"))));
        itemLore.add(Text.of(Chat.embedColours("&7--------------------")));
        itemLore.add(Text.of(Chat.embedColours("")));
        itemLore.add(Text.of(Chat.embedColours("&c* Left click to remove this listing from the market.")));
        itemLore.add(Text.of(Chat.embedColours("&c* Right click to re-list an expired item.")));
        Item.offer(Keys.ITEM_LORE, itemLore);

        return ItemStack.builder().fromContainer(Item.toContainer().set(DataQuery.of("UnsafeData","ListingID"), this.ListingID)).build();
    }

    private void setItemData(ItemStack item, boolean display) {
        Time time = new Time(this.expires.toInstant().toEpochMilli());

        ArrayList<Text> itemLore = new ArrayList<>();
        item.get(Keys.ITEM_LORE).ifPresent(lore -> lore.forEach(text -> itemLore.add(Text.of(text))));
        itemLore.add(Text.of(Chat.embedColours("&7--------------------")));
        if(display) itemLore.add(Text.of(Chat.embedColours("&7Seller: &b" + MarketUtils.getNameFromUUID(this.SellerUUID).get())));
        itemLore.add(Text.of(Chat.embedColours("&7Quantity: &b" + this.Quantity)));
        itemLore.add(Text.of(Chat.embedColours("&7Price: &bP" + this.Price)));
        if(display) itemLore.add(Text.of(Chat.embedColours("&7Expires: &b" + time.toString("%1$dd %2$dh %3$dm"))));
        itemLore.add(Text.of(Chat.embedColours("&7--------------------")));
        item.offer(Keys.ITEM_LORE, itemLore);
    }
}
