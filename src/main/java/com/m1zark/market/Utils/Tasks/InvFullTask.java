package com.m1zark.market.Utils.Tasks;

import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.market.Market;
import com.m1zark.market.Utils.Config.MessageConfig;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.TimeUnit;

public class InvFullTask implements Runnable {
    private Market pl = Market.getInstance();
    private Player player;
    private ItemStack item;
    private String option;

    public InvFullTask(ItemStack item, Player player, String option) {
        this.item = item;
        this.player = player;
        this.option = option;
    }

    @Override
    public void run() {
        if(!Inventories.giveItem(player, item, item.getQuantity())) {
            if(item.get(Keys.CUSTOM_NAME_VISIBLE).isPresent()) item.remove(Keys.CUSTOM_NAME_VISIBLE);

            if(this.option.equalsIgnoreCase("Purchase")) Chat.sendMessage(player, MessageConfig.getMessages("Messages.Purchase-Items.Inventory-Full"));
            else if(this.option.equalsIgnoreCase("PlayerListing")) Chat.sendMessage(player, MessageConfig.getMessages("Messages.Player-Listings.Inventory-Full"));

            pl.getScheduler().createTaskBuilder()
                    .name("MarketDelivery-" + Inventories.getItemName(item).toPlain())
                    .execute(new InvFullTask(item, player, option))
                    .delay(30, TimeUnit.SECONDS)
                    .submit(pl);
        } else {
            if(this.option.equalsIgnoreCase("Purchase")) Chat.sendMessage(player, MessageConfig.getMessages("Messages.Purchase-Items.Success"));
            else if(this.option.equalsIgnoreCase("PlayerListing")) Chat.sendMessage(player, MessageConfig.getMessages("Messages.Player-Listings.Inventory-Full"));
        }
    }
}
