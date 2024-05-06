/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.inventory.ExInventory;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryClickListener;
import de.timesnake.basic.lobby.hub.server.GameServer;
import de.timesnake.basic.lobby.user.LobbyUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.List;

public abstract class GameHub<GameInfo extends de.timesnake.library.game.GameInfo> implements
    UserInventoryClickListener {

  public static final ExItemStack BACK = new ExItemStack(Material.BLUE_BED, 1, "§cBack",
      List.of("§fClick to get back"));

  public static final Integer SERVER_SLOTS_START = 9;

  protected final GameInfo gameInfo;

  protected final ExInventory inventory;

  protected final ExItemStack item;

  public GameHub(GameInfo gameInfo) {
    this.gameInfo = gameInfo;
    this.inventory = new ExInventory(54, Component.text(this.gameInfo.getDisplayName()));
    this.inventory.setItemStack(4, BACK);
    this.item = new ExItemStack(this.gameInfo.getItem());

    Server.getInventoryEventManager().addClickListener(this, this.item);
  }

  protected Integer getServerNumber(Integer slot) {
    return slot - SERVER_SLOTS_START;
  }

  public Integer getEmptySlot() {
    return this.inventory.getFirstEmptySlot(SERVER_SLOTS_START);
  }

  @Override
  public void onUserInventoryClick(UserInventoryClickEvent event) {
    LobbyUser user = (LobbyUser) event.getUser();
    user.playSoundItemClickSuccessful();
    this.openServersInventory(user);
    event.setCancelled(true);
  }

  public ExItemStack getInvItem() {
    return this.item;
  }

  public ExInventory getInventory() {
    return this.inventory;
  }

  public void openServersInventory(User user) {
    user.openInventory(this.inventory);
  }

  public org.bukkit.inventory.ItemStack getItem() {
    return this.item;
  }

  public GameInfo getGameInfo() {
    return gameInfo;
  }

  public void updateServer(GameServer<?> server) {
    this.inventory.setItemStack(server.getSlot(), server.getItem());
  }
}
