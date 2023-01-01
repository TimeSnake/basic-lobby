/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.build;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.library.basic.util.MultiKeyMap;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.basic.util.chat.ChatColor;
import java.util.Comparator;
import java.util.TreeSet;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class BuildCategory implements InventoryHolder, UserInventoryClickListener {

    private final String name;
    private final ExItemStack displayItem;

    private final ExInventory inventory;

    private final Build build;

    private final MultiKeyMap<String, ExItemStack, BuildWorld> worldByNameOrItem = new MultiKeyMap<>();

    public BuildCategory(String name, Build build) {
        this.name = name;
        this.displayItem = new ExItemStack(Material.GRAY_WOOL).setDisplayName(ChatColor.BLUE + name);

        this.inventory = new ExInventory(6 * 9, Component.text(this.name), this);
        Server.getInventoryEventManager().addClickListener(this, this);

        this.build = build;
    }

    public ExItemStack getDisplayItem() {
        return displayItem;
    }

    public void addWorld(String worldName, String shortWorldName) {
        BuildWorld world = new BuildWorld(worldName, shortWorldName);
        this.worldByNameOrItem.put(world.getName(), world.getItem(), world);
        this.updateInventory();
    }

    public void updateWorld(String worldName, String serverName) {
        BuildWorld buildWorld = this.worldByNameOrItem.get1(worldName);
        if (buildWorld != null) {
            boolean result = buildWorld.update(serverName);
            if (result) {
                this.updateInventory();
            }
        }
    }

    public void removeServer(String serverName) {
        boolean update = false;
        for (BuildWorld world : this.worldByNameOrItem.values()) {
            if (world.removeIfServer(serverName)) {
                update = true;
            }
        }

        if (update) {
            this.updateInventory();
        }
    }

    private void updateInventory() {
        TreeSet<Tuple<String, ExItemStack>> sortedItems = new TreeSet<>(Comparator.comparing(Tuple::getA));

        for (BuildWorld world : this.worldByNameOrItem.values()) {
            sortedItems.add(new Tuple<>(world.getName(), world.getItem()));
        }

        int slot = 0;
        for (Tuple<String, ExItemStack> element : sortedItems) {
            this.inventory.setItemStack(slot, element.getB());
            slot++;
        }

        long loadedWorlds = this.worldByNameOrItem.values().stream().filter(BuildWorld::isLoaded).count();

        if (loadedWorlds == 0) {
            this.displayItem.setType(Material.GRAY_WOOL);
            this.displayItem.setAmount(1);
        } else if (loadedWorlds >= 1) {
            this.displayItem.setType(Material.GREEN_WOOL);
            this.displayItem.setAmount((int) loadedWorlds);
        }

        this.displayItem.setLore("", "§7" + loadedWorlds + " worlds loaded");
        this.build.updateInventory();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory.getInventory();
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent event) {
        User user = event.getUser();
        ExItemStack item = event.getClickedItem();

        BuildWorld world = this.worldByNameOrItem.get2(item);

        if (world != null) {
            world.moveUser(user);
        }

        event.setCancelled(true);
    }
}
