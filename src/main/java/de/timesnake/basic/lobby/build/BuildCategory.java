/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.build;

import de.timesnake.basic.bukkit.util.user.inventory.ExInventory;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.library.basic.util.MultiKeyMap;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.chat.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.util.Comparator;
import java.util.TreeSet;

public class BuildCategory {

  private final String name;
  private ExItemStack displayItem;

  private final ExInventory inventory;

  private final Build build;

  private final MultiKeyMap<String, ExItemStack, BuildWorld> worldByNameOrItem = new MultiKeyMap<>();

  public BuildCategory(String name, Build build) {
    this.name = name;
    this.displayItem = new ExItemStack(Material.GRAY_WOOL)
        .setDisplayName(ChatColor.BLUE + name)
        .onClick(event -> event.getUser().openInventory(this.getInventory()), true);

    this.inventory = new ExInventory(6 * 9, this.name);
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
    } else {
      this.addWorld(worldName, worldName.split("_", 2)[1]);
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
    TreeSet<Tuple<String, ExItemStack>> sortedItems = new TreeSet<>(
        Comparator.comparing(Tuple::getA));

    for (BuildWorld world : this.worldByNameOrItem.values()) {
      sortedItems.add(new Tuple<>(world.getName(), world.getItem()));
    }

    int slot = 0;
    for (Tuple<String, ExItemStack> element : sortedItems) {
      this.inventory.setItemStack(slot, element.getB());
      slot++;
    }

    long loadedWorlds = this.worldByNameOrItem.values().stream().filter(BuildWorld::isLoaded)
        .count();

    if (loadedWorlds == 0) {
      this.displayItem = this.displayItem.withType(Material.GRAY_WOOL);
      this.displayItem.setAmount(1);
    } else {
      this.displayItem = this.displayItem.withType(Material.GREEN_WOOL);
      this.displayItem.setAmount((int) loadedWorlds);
    }

    this.displayItem.setLore("", "§7" + loadedWorlds + " worlds loaded");
    this.build.updateInventory();
  }

  public Inventory getInventory() {
    return inventory.getInventory();
  }

}
