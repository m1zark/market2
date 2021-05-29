package com.m1zark.market.Utils;

import com.m1zark.market.Market;
import com.m1zark.market.MarketInfo;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import static org.spongepowered.api.Sponge.getGame;

public class MarketUtils {
    public static void checkNames(ItemStack itemStack) {
        if (!MarketUtils.checkAuras(itemStack) && !itemStack.get(Keys.ITEM_LORE).isPresent() && !Market.getInstance().getConfig().allowCustomNames() && itemStack.get(Keys.DISPLAY_NAME).isPresent()) {
            itemStack.remove(Keys.DISPLAY_NAME);
        }
    }

    public static boolean checkAuras(ItemStack itemStack) {
        return itemStack.getType().getId().equalsIgnoreCase("pixelmon:trio_badge") && itemStack.get(Keys.ITEM_ENCHANTMENTS).isPresent();
    }

    public static Optional<String> getNameFromUUID(UUID uuid){
        UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        Optional<User> oUser = uss.get(uuid);

        if (oUser.isPresent()){
            // the name with which that player has been online the last time
            String name = oUser.get().getName();
            return Optional.of(name);
        } else {
            // a player with that uuid has never been on your server
            return Optional.empty();
        }
    }

    public static String getDateTimestamp() {
        SimpleDateFormat ft = new SimpleDateFormat("MM/dd/yyyy @ h:mm a z");
        ft.setTimeZone(TimeZone.getTimeZone("EST"));

        return ft.format(new Date());
    }

    /*
     * Functions that handle the serializing and deserializing of the items
     */
    public static String serializeItem(ItemStack itemStack) {
        ConfigurationNode node = DataTranslators.CONFIGURATION_NODE.translate(itemStack.toContainer());
        StringWriter stringWriter = new StringWriter();
        try {
            HoconConfigurationLoader.builder().setSink(() -> new BufferedWriter(stringWriter)).build().save(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    public static Optional<ItemStack> deserializeItemStack(String item) {
        ConfigurationNode node = null;
        try {
            node = HoconConfigurationLoader.builder().setSource(() -> new BufferedReader(new StringReader(item))).build().load();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        DataContainer dataView = DataTranslators.CONFIGURATION_NODE.translate(node);
        return Sponge.getGame().getDataManager().deserialize(ItemStack.class, dataView);
    }

    /*
    * Optional<ItemStack> itemStackOptional = player.getItemInHand(HandTypes.MAIN_HAND);
    * .serializeSnapShot(itemStackOptional.get().createSnapshot(), args.hasAny("c"));
    *
    * Optional<String> serializedItemOptional = [SERIALIZED_ITEM_TEXT];
    * .deserializeSnapShot(serializedItemOptional.get()).createStack();
    *
   */
    public static String serializeItemStack(ItemStack itemStack, boolean json) throws ObjectMappingException, IOException {
        try {
            DataContainer container = itemStack.toContainer();
            StringWriter stringWriter = new StringWriter();

            if (json) { DataFormats.JSON.writeTo(stringWriter, container); }
            else { DataFormats.HOCON.writeTo(stringWriter, container); }

            return stringWriter.toString();
        } catch (IOException e) {
            Market.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(MarketInfo.ERROR_PREFIX, "Error serializing itemstack! Error: ", e.getMessage())));
            return null;
        }
    }

    public static ItemStack deserializeItem(String text) throws ObjectMappingException, IOException {
        try {
            DataContainer container = DataFormats.HOCON.read(text);
            return ItemStack.builder().fromContainer(container).build();
        } catch (IOException e) {
            Market.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(MarketInfo.ERROR_PREFIX, "Error deserializing itemstack! Error: ", e.getMessage())));
            return null;
        }
    }
}
