package com.m1zark.market.Utils.Config;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.market.Market;
import com.m1zark.market.MarketInfo;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageConfig {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static CommentedConfigurationNode main;

    public MessageConfig() {
        this.loadConfig();
    }

    private void loadConfig(){
        Path configFile = Paths.get(Market.getInstance().getConfigDir() + "/messages.conf");
        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(Market.getInstance().getConfigDir())) Files.createDirectory(Market.getInstance().getConfigDir());

            if (!Files.exists(configFile)) Files.createFile(configFile);

            if (main == null) {
                main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            }

            CommentedConfigurationNode messages = main.getNode("Messages");

            messages.setComment("{player} - Player, {item} - Name of Item, {count} - Quantity, {price} - Item Price");
            messages.getNode("ServerAnnounce").getBoolean(true);

            messages.getNode("General","Expired").getString("&3&lMARKET &e»&r &7This item has expired. Please refresh the listing.");
            messages.getNode("General","Sold").getString("&3&lMARKET &e»&r &7This item has already been sold. Please refresh the listings.");

            messages.getNode("Admin","Remove-Listing").getString("&3&lMARKET &e»&r &cSuccessfully removed the item from the listings.");
            messages.getNode("Admin","Mark-Expired").getString("&3&lMARKET &e»&r &cSuccessfully marked the item as expired.");

            messages.getNode("Sell-Item","Error").getString("&3&lMARKET &e»&r &7There was an error trying to create this listing. Please try again.");
            messages.getNode("Sell-Item","Success").getString("&3&lMARKET &e»&r &7You successfully added {count}x {item} to market for {price}.");
            messages.getNode("Sell-Item","No-Item").getString("&3&lMARKET &e»&r &7You need to be holding the item in your main hand.");
            messages.getNode("Sell-Item","Has-Too-Many").getString("&3&lMARKET &e»&r &7You cannot sell anymore items at this time.");
            messages.getNode("Sell-Item","Announce-Added").getString("&3&lMARKET &e»&r &7{player} has added &b{item} &7to the market.");
            messages.getNode("Sell-Item","Blacklist").getString("&3&lMARKET &e»&r &7This item is blacklisted and cannot be added to the market.");
            messages.getNode("Sell-Item","Negative-Price").getString("&3&lMARKET &e»&r &7You can't sell this item for a negative price. Please try again.");
            messages.getNode("Sell-Item","Max-Price").getString("&3&lMARKET &e»&r &7You cannot add an item for over P{maxprice}! Lower the price and try again.");
            messages.getNode("Sell-Item","Quantity").getString("&3&lMARKET &e»&r &7The quantity entered is more than you currently have. Please try again with a smaller quantity.");

            messages.getNode("Purchase-Items","Success").getString("&3&lMARKET &e»&r &7The item has been successfully added to your inventory.");
            messages.getNode("Purchase-Items","Own-Item").getString("&3&lMARKET &e»&r &7You can't purchase your own item. Please use the player listings to remove an item.");
            messages.getNode("Purchase-Items","Not-Enough-Money").getString("&3&lMARKET &e»&r &7Unable to purchase item. Make sure you have enough money to buy it.");
            messages.getNode("Purchase-Items","Inventory-Full").getString("&3&lMARKET &e»&r &7Unable to add the item to your inventory. Please make sure it is not full. Will try to add the item to your inventory again in 30 seconds.");
            messages.getNode("Purchase-Items","Seller-Message").getString("&3&lMARKET &e»&r &7Your {item} was purchased for {price}.");

            messages.getNode("Player-Listings","Remove-Success").getString("&3&lMARKET &e»&r &7Your {item} has been removed from the market and sent to your inventory.");
            messages.getNode("Player-Listings","Relist-Item").getString("&3&lMARKET &e»&r &7Your {item} has been successfully re-listed on the market.");
            messages.getNode("Player-Listings","Not-Expired").getString("&3&lMARKET &e»&r &7Your {item} hasn't expired yet. Unable to re-list it.");
            messages.getNode("Player-Listings","Inventory-Full").getString("&3&lMARKET &e»&r &7Unable to add the item to your inventory. Please make sure it is not full. Will try to add the item to your inventory again in 30 seconds.");

            loader.save(main);
        } catch (IOException e) {
            Market.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(MarketInfo.ERROR_PREFIX, "There was an issue loading the config...")));
            e.printStackTrace();
        }

        Market.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(MarketInfo.PREFIX, "Loading message configuration...")));
    }

    public static void saveConfig() {
        try {
            loader.save(main);
        } catch (IOException var1) {
            var1.printStackTrace();
        }
    }

    public void reload() {
        try {
            main = loader.load();
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }


    public boolean getServerAnnounce() { return main.getNode("Messages", "ServerAnnounce").getBoolean(); }

    public static String getMessages(String value) { return main.getNode((Object[])value.split("\\.")).getString(); }

    public static Text getMessage(String path, HashMap<String, Optional<Object>> replacements) {
        String message = main.getNode((Object[])path.split("\\.")).getString();
        if (message == null) {
            return Text.of(TextColors.RED, "A missing message setup was detected for path: ", TextColors.YELLOW, path);
        } else {
            return replacements != null ? replaceOptions(message, replacements) : TextSerializers.FORMATTING_CODE.deserialize(message);
        }
    }

    private static Text replaceOptions(String original, HashMap<String, Optional<Object>> replacements) {
        String translated = original;
        Tokens[] var3 = Tokens.values();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Tokens token = var3[var5];
            if ((translated.contains("{" + token.getToken() + "}") || translated.contains("{" + token.getToken() + ":s}")) && replacements.containsKey(token.getToken())) {
                if (original.contains("{" + token.getToken() + "}")) {
                    translated = translated.replaceAll(Pattern.quote("{" + token.getToken() + "}"), Matcher.quoteReplacement("" + replacements.get(token.getToken()).orElse("{" + token.getToken() + "}")));
                } else if (replacements.get(token.getToken()).isPresent()) {
                    translated = translated.replaceAll(Pattern.quote("{" + token.getToken() + ":s}"), Matcher.quoteReplacement(replacements.get(token.getToken()).get() + " "));
                } else {
                    translated = translated.replaceAll(Pattern.quote("{" + token.getToken() + ":s}"), "");
                }
            }
        }

        return TextSerializers.FORMATTING_CODE.deserialize(translated);
    }

    public static Text processPlaceholders(Text msg, Map<String, Text> placeholders) {
        if (!msg.getChildren().isEmpty()) {
            msg = msg.toBuilder().removeAll().append(msg.getChildren().stream()
                    .map(child -> processPlaceholders(child, placeholders)).collect(Collectors.toList())).build();
        }

        String plainMsg = msg.toPlain();
        for (String placeholder : placeholders.keySet()) {
            int matches = StringUtils.countMatches(plainMsg, placeholder);

            if (matches != 0) {
                String[] splitMessage = plainMsg.split(Pattern.quote(placeholder));
                Text.Builder finalMsgBuilder = Text.builder();
                for (int i = 0; i < splitMessage.length; i++) {
                    finalMsgBuilder.append(Text.of(splitMessage[i]));
                    if (matches > 0) {
                        finalMsgBuilder.append(placeholders.get(placeholder));
                        matches--;
                    }
                }

                while (matches > 0) {
                    finalMsgBuilder.append(placeholders.get(placeholder));
                    matches--;
                }

                msg = finalMsgBuilder.style(msg.getStyle()).color(msg.getColor()).build();
                return processPlaceholders(msg, placeholders);
            }
        }

        return msg;
    }
}
