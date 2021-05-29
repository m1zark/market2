package com.m1zark.market.UI;

import com.google.common.collect.Lists;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Inventories;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PlayerUI extends InventoryManager {
    private Player player;
    private int page;
    private int maxPage;
    private boolean searching;
    private String searchType;
    private String parameters;

    public PlayerUI(Player player, int page) {
        super(player, 6, Text.of(Chat.embedColours("&9&lMarket &l&0\u27A5&r &8Player Listings")));
        this.page = page;
        this.player = player;
        this.searching = false;
        this.searchType = null;
        this.parameters = null;

        List<Listing> valid = Lists.newArrayList();
        Market.getInstance().getSql().getAllListings().forEach((lot) -> {
            if (lot.getSellerUUID().equals(player.getUniqueId())) {
                valid.add(lot);
            }
        });
        this.maxPage = valid.size() % 36 == 0 && valid.size() / 36 != 0 ? valid.size() / 36 : valid.size() / 36 + 1;

        this.setupInventory();
        this.clearIcons(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35);
        this.setupListings(player);
    }

    public PlayerUI(Player player, int page, boolean searching, String searchType, String parameters) {
        super(player, 6, Text.of(Chat.embedColours("&9&lMarket &l&0\u27A5&r &8Player Listings")));
        this.page = page;
        this.player = player;
        this.searching = searching;
        this.searchType = searchType;
        this.parameters = parameters;

        List<Listing> valid = Lists.newArrayList();
        Market.getInstance().getSql().getAllListings().forEach((lot) -> {
            if (lot.getSellerUUID().equals(player.getUniqueId())) {
                valid.add(lot);
            }
        });
        this.maxPage = valid.size() % 36 == 0 && valid.size() / 36 != 0 ? valid.size() / 36 : valid.size() / 36 + 1;

        this.setupInventory();
        this.clearIcons(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35);
        this.setupListings(player);
    }

    private void setupInventory() {
        int x = 0;
        for(int y = 4; x < 9; x++) {
            this.addIcon(SharedIcons.BorderIcon(x + 9 * y, DyeColors.BLUE, true));
        }

        this.addIcon(SharedIcons.playerListingsIcon(45, this.player));

        Icon previousPage = SharedIcons.pageIcon(48, false);
        previousPage.addListener(clickable -> {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.updatePage(false);
            }).delayTicks(1L).submit(Market.getInstance());
        });
        this.addIcon(previousPage);

        Icon refresh = SharedIcons.refreshIcon(49);
        refresh.addListener(clickable -> {
            Player p = clickable.getPlayer();

            this.setupListings(p);
            this.updateContents();
        });
        this.addIcon(refresh);

        Icon nextPage = SharedIcons.pageIcon(50, true);
        nextPage.addListener(clickable -> {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.updatePage(true);
            }).delayTicks(1L).submit(Market.getInstance());
        });
        this.addIcon(nextPage);

        Icon lastMenuIcon = SharedIcons.lastMenu(53);
        lastMenuIcon.addListener(clickable -> {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                Player p = clickable.getPlayer();
                //p.closeInventory();
                p.openInventory((new MainUI(p, 1, this.searching, this.searchType, this.parameters)).getInventory());
            }).delayTicks(1L).submit(Market.getInstance());
        });
        this.addIcon(lastMenuIcon);
    }

    private void setupListings(Player player) {
        int index = (this.page - 1) * 36;
        //List<Listing> items = Market.getInstance().getSql().getPlayerListings(player.getUniqueId());
        List<Listing> items = Market.getInstance().getSql().getAllListings().stream()
                .filter(listing -> listing.getSellerUUID().equals(player.getUniqueId())).collect(Collectors.toList());

        int x = 0;
        for(int y = 0; y < 4 && index <= items.size(); ++index) {
            if (x == 9 && y != 3) {
                x = 0;
                ++y;
            } else if (x == 9) {
                break;
            }

            if (index >= items.size()) {
                this.getAllIcons().remove(x + 9 * y);
                ++x;
            } else {
                Listing listing = items.get(index);
                Icon item = new Icon(x + 9 * y, listing.getItemPlayer());
                item.addListener(clickable -> {
                    Player p = clickable.getPlayer();
                    if (!clickable.getEvent().getCursorTransaction().getFinal().getType().equals(ItemTypes.NONE)) {
                        String ListingID = clickable.getEvent().getCursorTransaction().getFinal().toContainer().getString(DataQuery.of("UnsafeData", "ListingID")).get();
                        Optional<Listing> itemCache = Market.getInstance().getSql().getAllListings().stream()
                                .filter(listing1 -> listing1.getListingID() == Integer.valueOf(ListingID)).findFirst();
                        //Listing itemCache = Market.getInstance().getListingCache().stream().filter(l -> l.getListingsID() == Integer.valueOf(ListingID)).findFirst().orElse(null);

                        if (!itemCache.isPresent()) {
                            Chat.sendMessage(p, MessageConfig.getMessages("Messages.General.Sold"));
                        }else {
                            ItemStack is = MarketUtils.deserializeItemStack(itemCache.get().getItem()).get();

                            if (clickable.getEvent() instanceof ClickInventoryEvent.Secondary) {
                                if (itemCache.get().checkHasExpired()) {
                                    Market.getInstance().getSql().updateListing(itemCache.get().getListingID(), false);
                                    Chat.sendMessage(p, MessageConfig.getMessages("Messages.Player-Listings.Relist-Item").replace("{item}", Inventories.getItemName(is).toPlain()));
                                    Sponge.getScheduler().createTaskBuilder().execute(() -> {
                                        this.setupListings(p);
                                        this.updateContents();
                                    }).delayTicks(1L).submit(Market.getInstance());
                                } else {
                                    Chat.sendMessage(p, MessageConfig.getMessages("Messages.Player-Listings.Not-Expired").replace("{item}", Inventories.getItemName(is).toPlain()));
                                }
                            } else if (clickable.getEvent() instanceof ClickInventoryEvent.Primary) {
                                Market.getInstance().getSql().addNewLog(new Log(player.getName(), itemCache.get(), MarketUtils.getDateTimestamp(), LogAction.Removal));

                                returnItem(itemCache.get(), p);
                                Market.getInstance().getSql().removeListing(itemCache.get().getListingID());
                                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                                    this.setupListings(p);
                                    this.updateContents();
                                }).delayTicks(1L).submit(Market.getInstance());
                            }
                        }
                    }
                });
                this.addIcon(item);
                ++x;
            }
        }
    }

    private void updatePage(boolean upOrDown) {
        if (upOrDown) {
            if (this.page < this.maxPage) {
                ++this.page;
            } else {
                this.page = 1;
            }
        } else if (this.page > 1) {
            --this.page;
        } else {
            this.page = this.maxPage;
        }

        Icon previousPage = SharedIcons.pageIcon(48, false);
        previousPage.addListener(clickable -> {
            this.updatePage(false);
        });
        this.addIcon(previousPage);

        Icon nextPage = SharedIcons.pageIcon(50, true);
        nextPage.addListener(clickable -> {
            this.updatePage(true);
        });
        this.addIcon(nextPage);

        this.clearIcons(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35);
        this.setupListings(player);
        this.updateContents(0,35);
    }

    private static void returnItem(Listing item, Player p) {
        ItemStack is = MarketUtils.deserializeItemStack(item.getItem()).get();
        ItemStack nis = is.copy();
        nis.setQuantity(item.getQuantity());

        if(Inventories.doesHaveNBT(nis,"PokeBuilder")) {
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "pb give " + p.getName() + " " + nis.getQuantity());
            Chat.sendMessage(p, MessageConfig.getMessages("Messages.Player-Listings.Remove-Success").replace("{item}",  Inventories.getItemName(nis).toPlain()));
        }
        else if(MarketUtils.checkAuras(nis)){
            String[] aura =  Inventories.getItemName(nis).toPlain().split(" ");
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "ep give " + p.getName() + " " + aura[0].toLowerCase());
            Chat.sendMessage(p, MessageConfig.getMessages("Messages.Player-Listings.Remove-Success").replace("{item}",  Inventories.getItemName(nis).toPlain()));
        }
        else {
            if (Inventories.giveItem(p, nis, nis.getQuantity())) {
                if(nis.get(Keys.CUSTOM_NAME_VISIBLE).isPresent()) nis.remove(Keys.CUSTOM_NAME_VISIBLE);

                Chat.sendMessage(p, MessageConfig.getMessages("Messages.Player-Listings.Remove-Success").replace("{item}",  Inventories.getItemName(nis).toPlain()));
            } else {
                Chat.sendMessage(p, MessageConfig.getMessages("Messages.Player-Listings.Inventory-Full"));
                Market.getInstance().getScheduler().createTaskBuilder()
                        .name("MarketDelivery-" + Inventories.getItemName(nis).toPlain())
                        .execute(new InvFullTask(nis, p, "PlayerListing"))
                        .delay(30, TimeUnit.SECONDS)
                        .submit(Market.getInstance());
            }
        }
    }
}