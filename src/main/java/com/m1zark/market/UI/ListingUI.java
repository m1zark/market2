package com.m1zark.market.UI;

import com.google.common.collect.Lists;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.m1utilities.api.Money;
import com.m1zark.m1utilities.api.Time;
import com.m1zark.market.Market;
import com.m1zark.m1utilities.api.GUI.Icon;
import com.m1zark.m1utilities.api.GUI.InventoryManager;
import com.m1zark.market.Utils.Config.MessageConfig;
import com.m1zark.market.Utils.Listing;
import com.m1zark.market.Utils.Logs.Log;
import com.m1zark.market.Utils.Logs.LogAction;
import com.m1zark.market.Utils.MarketUtils;
import com.m1zark.market.Utils.Tasks.InvFullTask;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ListingUI extends InventoryManager {
    private Player player;
    private Listing listing;
    private int page;
    private boolean searching;
    private String searchType;
    private String parameters;
    private boolean admin = false;

    public ListingUI(Player player, Listing listing, int page, boolean searching, String searchType, String parameters) {
        super(player, 3, Text.of(Chat.embedColours("&9&lMarket &l&0\u27A5&r &8Confirm Purchase")));
        this.player = player;
        this.listing = listing;
        this.page = page;
        this.searching = searching;
        this.parameters = parameters;
        this.searchType = searchType;

        if(player.hasPermission("market.admin.listings") || player.hasPermission("market.admin.listings.remove") || player.hasPermission("market.admin.listings.expire")) {
            this.admin = true;
        }

        this.setupInventory();
    }

    private void setupInventory() {
        int x = 0;
        int y;
        int index = 0;

        for(y = 0; y < 6 && index <= 26; ++index) {
            if (x == 9) {
                x = 0;
                ++y;
            }
            this.addIcon(SharedIcons.BorderIcon(x + 9 * y, DyeColors.BLUE, false));
            ++x;
        }

        Icon confirm = SharedIcons.confirmIcon(10);
        confirm.addListener(clickable -> {
            Player p = clickable.getPlayer();

            Optional<Listing> itemCache = Market.getInstance().getSql().getAllListings().stream()
                    .filter(listing1 -> listing1.getListingID() == this.listing.getListingID()).findFirst();

            if (!itemCache.isPresent()) {
                Chat.sendMessage(p, MessageConfig.getMessages("Messages.General.Sold"));
            }else if(p.getUniqueId().equals(this.listing.getSellerUUID())) {
                Chat.sendMessage(p, MessageConfig.getMessages("Messages.Purchase-Items.Own-Item"));
            }else if(itemCache.get().checkHasExpired()){
                Chat.sendMessage(p, MessageConfig.getMessages("Messages.General.Expired"));
            }else {
                completePurchase(this.listing, p);
                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    p.openInventory((new MainUI(p, this.page, this.searching, this.searchType, this.parameters)).getInventory());
                }).delayTicks(1L).submit(Market.getInstance());
            }
        });
        this.addIcon(confirm);

        this.addIcon(new Icon(12, this.listing.getItemNoData()));
        this.addIcon(SharedIcons.listingInformation(14, listing));

        Icon cancel = SharedIcons.cancelIcon(16);
        cancel.addListener(clickable -> {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                Player p = clickable.getPlayer();
                //p.closeInventory();
                p.openInventory((new MainUI(p, this.page, this.searching, this.searchType, this.parameters)).getInventory());
            }).delayTicks(1L).submit(Market.getInstance());
        });
        this.addIcon(cancel);

        if(this.admin) {
            List<Text> lore = Lists.newArrayList(Text.of(TextColors.GREEN, "* Left click to mark item as expired"), Text.of(TextColors.RED, "* Right click to remove listing and destroy item"));
            Icon admin = new Icon(22, ItemStack.builder().itemType(ItemTypes.BARRIER).quantity(1).add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Admin Actions")).add(Keys.ITEM_LORE, lore).build());
            admin.addListener(clickable -> {
                Player p = clickable.getPlayer();

                if (clickable.getEvent() instanceof ClickInventoryEvent.Secondary) {
                    if(this.player.hasPermission("market.admin.listings.remove")) {
                        Market.getInstance().getSql().removeListing(this.listing.getListingID());
                        Chat.sendMessage(p, MessageConfig.getMessages("Messages.Admin.Remove-Listing"));
                    } else {
                        Chat.sendMessage(p, "&cYou do not have permission to do this.");
                    }
                }else if(clickable.getEvent() instanceof ClickInventoryEvent.Primary) {
                    if(this.player.hasPermission("market.admin.listings.expire")) {
                        Market.getInstance().getSql().updateListing(this.listing.getListingID(), true);
                        Chat.sendMessage(p, MessageConfig.getMessages("Messages.Admin.Mark-Expired"));
                    } else {
                        Chat.sendMessage(p, "&cYou do not have permission to do this.");
                    }
                }

                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    p.closeInventory();
                    p.openInventory((new MainUI(p, this.page, this.searching, this.searchType, this.parameters)).getInventory());
                }).delayTicks(1L).submit(Market.getInstance());
            });
            this.addIcon(admin);
        }
    }

    private static void completePurchase(Listing listing, Player p) {
        if(Money.canPay(p, Integer.valueOf(listing.getPrice()))){
            ItemStack dummy = getItem(p, listing);

            if(dummy == null) Chat.sendMessage(p, MessageConfig.getMessages("Messages.Purchase-Items.Not-Enough-Money"));
            else {
                Market.getInstance().getSql().addNewLog(new Log(p.getName(), listing, MarketUtils.getDateTimestamp(), LogAction.Buy));
                Market.getInstance().getSql().addNewLog(new Log(p.getName(), listing, MarketUtils.getDateTimestamp(), LogAction.Sell));

                Market.getInstance().getSql().removeListing(listing.getListingID());

                if(Inventories.doesHaveNBT(dummy,"PokeBuilder")) {
                    Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "pb give " + p.getName() + " " + dummy.getQuantity());
                    Chat.sendMessage(p, MessageConfig.getMessages("Messages.Purchase-Items.Success").replace("{count}", String.valueOf(listing.getQuantity())).replace("{item}", listing.getItemName()).replace("{price}", listing.getPrice()));
                }
                else if(MarketUtils.checkAuras(dummy)){
                    String[] aura =  Inventories.getItemName(dummy).toPlain().split(" ");
                    Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "ep give " + p.getName() + " " + aura[0].toLowerCase());
                    Chat.sendMessage(p, MessageConfig.getMessages("Messages.Purchase-Items.Success").replace("{count}", String.valueOf(listing.getQuantity())).replace("{item}", listing.getItemName()).replace("{price}", listing.getPrice()));
                }
                else {
                    if (Inventories.giveItem(p, dummy, dummy.getQuantity())) {
                        Chat.sendMessage(p, MessageConfig.getMessages("Messages.Purchase-Items.Success").replace("{count}", String.valueOf(listing.getQuantity())).replace("{item}", listing.getItemName()).replace("{price}", listing.getPrice()));
                    } else {
                        Chat.sendMessage(p, MessageConfig.getMessages("Messages.Purchase-Items.Inventory-Full"));
                        Market.getInstance().getScheduler().createTaskBuilder()
                                .name("MarketDelivery-" + Inventories.getItemName(dummy).toPlain())
                                .execute(new InvFullTask(dummy, p, "Purchase"))
                                .delay(30, TimeUnit.SECONDS)
                                .submit(Market.getInstance());
                    }
                }

                if (Sponge.getServer().getPlayer(listing.getSellerUUID()).isPresent()) {
                    Chat.sendMessage(Sponge.getServer().getPlayer(listing.getSellerUUID()).get(), MessageConfig.getMessages("Messages.Purchase-Items.Seller-Message")
                            .replace("{item}", listing.getItemName())
                            .replace("{count}", String.valueOf(listing.getQuantity()))
                            .replace("{price}", listing.getPrice()));
                }
            }
        }
    }

    private static ItemStack getItem(Player player, Listing listing) {
        if (Money.transfer(player.getUniqueId(), listing.getSellerUUID(), Integer.valueOf(listing.getPrice()))) {
            ItemStack is = MarketUtils.deserializeItemStack(listing.getItem()).get();
            int quantity = listing.getQuantity();
            return ItemStack.builder().from(is).quantity(quantity).build();
        }else{
            return null;
        }
    }
}