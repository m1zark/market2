package com.m1zark.market.Utils.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.m1zark.market.Market;
import com.m1zark.market.MarketInfo;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;

public class ConfigManager {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static CommentedConfigurationNode main;

    public static String storageType;
    public static String mysqlURL;
    public static String mysqlUsername;
    public static String mysqlPassword;

    public ConfigManager() {
        this.loadConfig();
    }

    private void loadConfig(){
        Path configFile = Paths.get(Market.getInstance().getConfigDir() + "/settings.conf");

        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(Market.getInstance().getConfigDir())) Files.createDirectory(Market.getInstance().getConfigDir());

            if (!Files.exists(configFile)) Files.createFile(configFile);

            if (main == null) {
                main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            }

            CommentedConfigurationNode storage = main.getNode("Storage");
            storageType = storage.getNode("storageType").setComment("Types: h2, mysql").getString("h2");
            mysqlURL = storage.getNode("MYSQL","URL").getString("[host]:[port]/[database]");
            mysqlUsername = storage.getNode("MYSQL","Username").getString("");
            mysqlPassword = storage.getNode("MYSQL","Password").getString("");

            CommentedConfigurationNode general = main.getNode("General");
            general.getNode("Listings-Time").setComment("Sets the default duration of a listing (In Minutes)");
            general.getNode("Listings-Time").getInt(60);
            general.getNode("Max-Listings").setComment("Set how many listings a player is allowed to have, -1 for no limit");
            general.getNode("Max-Listings").getInt(-1);
            general.getNode("removedExpiredLogs").setComment("Days to keep logs before they are auto removed").getInt(30);

            general.getNode("AllowCustomNames").getBoolean(false);

            general.getNode("Max-Price").setComment("Max price for listings").getInt(1000000);

            CommentedConfigurationNode blacklist = main.getNode("Blacklist");
            blacklist.getList(TypeToken.of(String.class), Lists.newArrayList("pixelmon:orb","minecraft:diamond","minecraft:wool,1"));

            loader.save(main);
        } catch (ObjectMappingException | IOException e) {
            Market.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(MarketInfo.ERROR_PREFIX, "There was an issue loading the config...")));
            e.printStackTrace();
        }

        Market.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(MarketInfo.PREFIX, "Loading configuration...")));
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

    public static String getStorageType() {  return main.getNode("Storage","storageType").getString(); }

    public int getMaxListings() { return main.getNode("General","Max-Listings").getInt(); }

    public long getListingsTime() { return (long)(main.getNode("General","Listings-Time").getInt() * 60); }

    public int getMaxPrice() { return main.getNode("General","Max-Price").getInt(); }

    public static int getRemoveExpiredLogs() { return main.getNode("General","removedExpiredLogs").getInt(); }

    public boolean allowCustomNames() { return main.getNode("General","AllowCustomNames").getBoolean(); }

    public List<String> getBlacklist() {
        try {
            List<String> blacklist = new ArrayList<>();
            Iterator<String> list = main.getNode("Blacklist").getList(TypeToken.of(String.class)).iterator();

            list.forEachRemaining(blacklist::add);

            return blacklist;
        } catch (ObjectMappingException e) {
            e.printStackTrace();
            return Lists.newArrayList();
        }
    }
}

