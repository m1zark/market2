package com.m1zark.market.UI;

import com.google.common.collect.Lists;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Time;
import com.m1zark.market.Market;
import com.m1zark.m1utilities.api.GUI.Icon;
import com.m1zark.m1utilities.api.GUI.InventoryManager;
import com.m1zark.market.Utils.Config.MessageConfig;
import com.m1zark.market.Utils.Listing;
import com.m1zark.market.Utils.MarketUtils;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainUI extends InventoryManager {
    private Player player;
    private int page = 1;
    private int maxPage;
    private boolean searching;
    private String searchType;
    private String parameters;

    public MainUI(Player player, int page) {
        super(player, 6, Text.of(Chat.embedColours("&9&lMarket &l&0\u27A5&r &8Current Listings")));
        this.player = player;
        this.page = page;
        this.searching = false;
        this.searchType = null;
        this.parameters = null;
        int size = (int) Market.getInstance().getSql().getAllListings().stream().filter(listing -> !listing.checkHasExpired()).count();
        this.maxPage = size % 36 == 0 && size / 36 != 0 ? size / 36 : size / 36 + 1;

        this.setupInventory();
        this.setupListings();
    }

    public MainUI(Player player, int page, boolean searching, String searchType, String parameters) {
        super(player, 6, Text.of(Chat.embedColours("&9&lMarket &l&0\u27A5&r &8Current Listings")));
        this.player = player;
        this.page = page;
        this.searching = searching;
        this.searchType = searchType;
        this.parameters = parameters;
        int size;
        if (!this.searching) {
            size = (int) Market.getInstance().getSql().getAllListings().stream().filter(listing -> !listing.checkHasExpired()).count();
            this.maxPage = size % 36 == 0 && size / 36 != 0 ? size / 36 : size / 36 + 1;
        } else {
            size = this.searchResults().size();
            this.maxPage = size % 36 == 0 ? size / 36 : size / 36 + 1;
        }

        this.setupInventory();
        this.setupListings();
    }

    private void setupInventory() {
        int x = 0;
        for(int y = 4; x < 9; x++) {
            this.addIcon(SharedIcons.BorderIcon(x + 9 * y, DyeColors.BLUE, true));
        }

        Icon plListings = SharedIcons.playerListingsIcon(45, this.player);
        plListings.addListener(clickable -> {
            Player p = clickable.getPlayer();
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                if(this.searching) {
                    p.openInventory((new PlayerUI(p, 1)).getInventory());
                }else{
                    p.openInventory((new PlayerUI(p, 1, this.searching, this.searchType, this.parameters)).getInventory());
                }
            }).delayTicks(1L).submit(Market.getInstance());
        });
        this.addIcon(plListings);

        Icon previousPage = SharedIcons.pageIcon(48, false);
        previousPage.addListener(clickable -> {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.updatePage(false);
            }).delayTicks(1L).submit(Market.getInstance());
        });
        this.addIcon(previousPage);

        Icon refresh = SharedIcons.refreshIcon(49);
        refresh.addListener(clickable -> {
            this.setupListings();
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

        Icon gts = SharedIcons.gtsInfo(53);
        gts.addListener(clickable -> {
            Player p = clickable.getPlayer();
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                p.closeInventory();
                Sponge.getCommandManager().process(p, "gts");
            }).delayTicks(1L).submit(Market.getInstance());
        });
        this.addIcon(gts);
    }

    private void setupListings() {
        int index = (this.page - 1) * 36;
        int x;
        List<Listing> listings;

        if(this.searching) {
            listings = this.searchResults();
        }else{
            listings = Market.getInstance().getSql().getAllListings();
        }

        listings = listings.stream().filter(listing -> !listing.checkHasExpired()).collect(Collectors.toList());

        for (int y = 0; y < 4; y++) {
            for (x = 0; x < 9; x++, index++) {
                if (index >= listings.size()) break;

                final int pos = index;
                Icon item = new Icon(x + (9 * y), (listings.get(pos)).getItemDisplay());
                item.addListener(clickable -> {
                    int id = (Integer) clickable.getEvent().getCursorTransaction().getFinal().toContainer().get(DataQuery.of("UnsafeData", "ListingID")).get();
                    Optional<Listing> l = Market.getInstance().getSql().getAllListings().stream().filter(list -> list.getListingID() == id).findFirst();

                    if (!l.isPresent()) {
                        Chat.sendMessage(this.player, MessageConfig.getMessages("Messages.General.Sold"));
                    } else if (l.get().checkHasExpired()) {
                        Chat.sendMessage(this.player, MessageConfig.getMessages("Messages.General.Expired"));
                    } else {
                        Sponge.getScheduler().createTaskBuilder().execute(() -> {
                            this.player.openInventory((new ListingUI(this.player, l.get(), this.page, this.searching, this.searchType, this.parameters)).getInventory());
                        }).delayTicks(1L).submit(Market.getInstance());
                    }
                });
                this.addIcon(item);
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

        // Previous Page
        Icon previousPage = SharedIcons.pageIcon(48, false);
        previousPage.addListener(clickable -> {
            this.updatePage(false);
        });
        this.addIcon(previousPage);

        // Next Page
        Icon nextPage = SharedIcons.pageIcon(50, true);
        nextPage.addListener(clickable -> {
            this.updatePage(true);
        });
        this.addIcon(nextPage);

        this.clearIcons(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35);
        this.setupListings();
        this.updateContents(0,35);
    }

    private List<Listing> searchResults() {
        List<Listing> valid = Lists.newArrayList();
        List<Listing> listings = Market.getInstance().getSql().getAllListings();

        if(this.searchType.equalsIgnoreCase("item")){
            List<String> search = Lists.newArrayList(this.parameters.split(" "));
            for (Listing item : listings) {
                for (String s : search) {
                    if (StringUtils.containsIgnoreCase(item.getItemName(), s)) {
                        valid.add(item);
                    }
                }
            }
        }
        else if(this.searchType.equalsIgnoreCase("player")) {
            valid = listings.stream()
                    .filter(name -> name.getSellerUUID().toString().equals(this.parameters))
                    .collect(Collectors.toList());
        }

        return valid;
    }
}