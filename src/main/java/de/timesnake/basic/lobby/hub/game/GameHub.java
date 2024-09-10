/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.inventory.ExInventory;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.lobby.hub.server.GameServer;
import de.timesnake.basic.lobby.user.LobbyUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public abstract class GameHub<GameInfo extends de.timesnake.library.game.GameInfo> {

  public static final ExItemStack BACK = new ExItemStack(Material.BLUE_BED)
      .setDisplayName("§cBack")
      .setLore("§fClick to get back")
      .immutable()
      .onClick(event -> {
        LobbyUser user = (LobbyUser) event.getUser();
        user.openGameHubInventory();
        user.playSoundItemClickSuccessful();
      }, true);

  public static final Integer SERVER_SLOTS_START = 9;

  protected final GameInfo gameInfo;

  protected final ExInventory inventory;

  protected final ExItemStack item;

  public GameHub(GameInfo gameInfo) {
    this.gameInfo = gameInfo;
    this.inventory = new ExInventory(54, Component.text(this.gameInfo.getDisplayName()));
    this.inventory.setItemStack(4, BACK);
    this.item = new ExItemStack(this.gameInfo.getItem())
        .onClick(event -> {
          LobbyUser user = (LobbyUser) event.getUser();
          user.playSoundItemClickSuccessful();
          this.openServersInventory(user);
        }, true);
  }

  protected Integer getServerNumber(Integer slot) {
    return slot - SERVER_SLOTS_START;
  }

  public Integer getEmptySlot() {
    return this.inventory.getFirstEmptySlot(SERVER_SLOTS_START);
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
