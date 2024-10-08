/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub;

import de.timesnake.basic.bukkit.util.user.inventory.ExInventory;
import de.timesnake.basic.lobby.hub.game.GameHub;
import de.timesnake.basic.lobby.hub.game.NonTmpGameHub;
import de.timesnake.basic.lobby.hub.game.OwnableNonTmpGameHubManager;
import de.timesnake.basic.lobby.hub.game.TmpGameHub;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbNonTmpGame;
import de.timesnake.database.util.game.DbTmpGame;
import net.kyori.adventure.text.Component;

import java.util.HashMap;

public class GamesMenu {

  private final ExInventory inventory = new ExInventory(54, Component.text("Gamehub"));
  private final HashMap<Integer, GameHub<?>> games = new HashMap<>();

  public GamesMenu() {
    this.updateInventory();
  }

  public void updateInventory() {
    inventory.getInventory().clear();
    games.clear();

    for (DbGame game : Database.getGames().getGames()) {
      if (!game.getInfo().isEnabled()) {
        continue;
      }

      GameHub<?> gameHub;
      if (game instanceof DbTmpGame) {
        gameHub = new TmpGameHub(((DbTmpGame) game));
      } else {
        if (((DbNonTmpGame) game).isOwnable()) {
          gameHub = new OwnableNonTmpGameHubManager(((DbNonTmpGame) game));
        } else {
          gameHub = new NonTmpGameHub(((DbNonTmpGame) game));
        }
      }
      inventory.setItemStack(gameHub.getGameInfo().getSlot(), gameHub.getItem());
      games.put(gameHub.getGameInfo().getSlot(), gameHub);
    }
  }

  public ExInventory getInventory() {
    return inventory;
  }

}