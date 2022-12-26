/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.basic.lobby.build;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.object.Type;
import de.timesnake.library.basic.util.MultiKeyMap;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class Build implements ChannelListener, UserInventoryClickListener, InventoryHolder {

    private final ExInventory inventory = new ExInventory(54, Component.text("Build-Worlds"), this);

    private final MultiKeyMap<String, ExItemStack, BuildCategory> categoryByNameOrItem = new MultiKeyMap<>();

    public Build() {
        Server.getChannel().addListener(this);
        Server.getInventoryEventManager().addClickListener(this, this);
        this.loadExistingWorlds();
    }

    private void loadExistingWorlds() {
        int slot = 0;
        for (String worldName : Server.getNetwork().getWorldNames(Type.Server.BUILD, null)) {
            String[] nameParts = worldName.split("_");
            String categoryName = nameParts[0];
            String shortWorldName = worldName.replaceFirst(categoryName + "_", "");
            BuildCategory category = this.categoryByNameOrItem.get1(categoryName);
            if (category == null) {
                category = new BuildCategory(categoryName, this);
                this.categoryByNameOrItem.put(categoryName, category.getDisplayItem(), category);
                this.inventory.setItemStack(slot, category.getDisplayItem());
                slot++;
            }

            category.addWorld(worldName, shortWorldName);
        }

        Server.printText(Plugin.LOBBY, "Loaded build map-worlds", "Build");
    }

    private void updateWorld(String worldName, String serverName) {
        for (BuildCategory category : this.categoryByNameOrItem.values()) {
            category.updateWorld(worldName, serverName);
        }
    }

    private void removeServer(String serverName) {
        this.categoryByNameOrItem.values().forEach(category -> category.removeServer(serverName));
    }

    public void updateInventory() {
        int slot = 0;
        for (BuildCategory category : this.categoryByNameOrItem.values()) {
            this.inventory.setItemStack(slot, category.getDisplayItem());
            slot++;
        }
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory.getInventory();
    }

    @ChannelHandler(type = {ListenerType.SERVER_LOAD_WORLD, ListenerType.SERVER_UNLOAD_WORLD,
            ListenerType.SERVER_LOADED_WORLD, ListenerType.SERVER_UNLOADED_WORLD, ListenerType.SERVER_UNLOADED_ALL_WORLDS})
    public void onServerMessage(ChannelServerMessage<?> msg) {
        if (msg.getMessageType().equals(MessageType.Server.UNLOADED_ALL_WORLDS)) {
            this.removeServer(msg.getName());
        } else {
            this.updateWorld((String) msg.getValue(), msg.getName());
        }
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent event) {
        User user = event.getUser();
        ExItemStack item = event.getClickedItem();

        BuildCategory category = this.categoryByNameOrItem.get2(item);

        if (category != null) {
            user.openInventory(category.getInventory());
        }

        event.setCancelled(true);

    }
}
