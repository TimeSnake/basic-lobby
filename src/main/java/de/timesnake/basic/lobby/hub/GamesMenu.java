/*
 * basic-lobby.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.basic.lobby.hub;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.basic.lobby.hub.game.GameHub;
import de.timesnake.basic.lobby.hub.game.NonTmpGameHub;
import de.timesnake.basic.lobby.hub.game.OwnableNonTmpGameHubManager;
import de.timesnake.basic.lobby.hub.game.TmpGameHub;
import de.timesnake.basic.lobby.user.LobbyUser;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbNonTmpGame;
import de.timesnake.database.util.game.DbTmpGame;

import java.util.HashMap;

public class GamesMenu implements UserInventoryClickListener {

    private final ExInventory inventory = Server.createExInventory(54, "Gamehub");
    private final HashMap<Integer, GameHub<?>> games = new HashMap<>();

    public GamesMenu() {
        Server.getInventoryEventManager().addClickListener(this, GameHub.BACK);
        this.updateInventory();
    }

    public void updateInventory() {
        inventory.getInventory().clear();
        games.clear();

        for (DbGame game : Database.getGames().getGames()) {
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
            if (gameHub == null) {
                Server.printError(Plugin.LOBBY, "Can not load game " + game.getInfo().getName());
            } else {
                inventory.setItemStack(gameHub.getGameInfo().getSlot(), gameHub.getItem());
                games.put(gameHub.getGameInfo().getSlot(), gameHub);
            }
        }
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent e) {
        LobbyUser user = (LobbyUser) e.getUser();
        user.openGameHubInventory();
        user.playSoundItemClicked();
        e.setCancelled(true);
    }


    public ExInventory getInventory() {
        return inventory;
    }

}