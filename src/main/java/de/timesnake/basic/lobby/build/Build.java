/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.build;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.inventory.ExInventory;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.library.basic.util.MultiKeyMap;
import de.timesnake.library.basic.util.ServerType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.inventory.Inventory;

public class Build implements ChannelListener {

  private final Logger logger = LogManager.getLogger("lobby.build");

  private final ExInventory inventory = new ExInventory(54, "Build-Worlds");

  private final MultiKeyMap<String, ExItemStack, BuildCategory> categoryByNameOrItem = new MultiKeyMap<>();

  private int slotCounter = 0;

  public Build() {
    Server.getChannel().addListener(this);
    this.loadExistingWorlds();
  }

  private void loadExistingWorlds() {
    for (String worldName : Server.getNetwork().getWorldNames(ServerType.BUILD, null)) {
      String[] nameParts = worldName.split("_");
      String categoryName = nameParts[0];
      String shortWorldName = worldName.replaceFirst(categoryName + "_", "");
      BuildCategory category = this.categoryByNameOrItem.get1(categoryName);
      if (category == null) {
        category = this.addCategory(categoryName);
      }

      category.addWorld(worldName, shortWorldName);
    }

    this.logger.info("Loaded build map-worlds");
  }

  private void updateWorld(String worldName, String serverName) {
    String categoryName = this.getCategoryFromWorldName(worldName);
    BuildCategory category = this.categoryByNameOrItem.get1(categoryName);

    if (category == null) {
      this.addCategory(categoryName);
    }

    category.updateWorld(worldName, serverName);
  }

  private BuildCategory addCategory(String name) {
    BuildCategory category = new BuildCategory(name, this);
    this.categoryByNameOrItem.put(name, category.getDisplayItem(), category);
    this.inventory.setItemStack(slotCounter, category.getDisplayItem());
    slotCounter++;

    return category;
  }

  private String getCategoryFromWorldName(String worldName) {
    return worldName.split("_", 2)[0];
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

  public Inventory getInventory() {
    return inventory.getInventory();
  }

  @ChannelHandler(type = {ListenerType.SERVER_LOAD_WORLD, ListenerType.SERVER_UNLOAD_WORLD,
      ListenerType.SERVER_LOADED_WORLD, ListenerType.SERVER_UNLOADED_WORLD,
      ListenerType.SERVER_UNLOADED_ALL_WORLDS})
  public void onServerMessage(ChannelServerMessage<?> msg) {
    if (msg.getMessageType().equals(MessageType.Server.UNLOADED_ALL_WORLDS)) {
      this.removeServer(msg.getName());
    } else {
      this.updateWorld((String) msg.getValue(), msg.getName());
    }
  }
}
