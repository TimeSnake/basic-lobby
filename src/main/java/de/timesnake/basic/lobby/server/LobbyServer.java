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

package de.timesnake.basic.lobby.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.lobby.build.Build;
import de.timesnake.basic.lobby.hub.GamesMenu;
import de.timesnake.basic.lobby.user.LobbyInventory;
import de.timesnake.library.waitinggames.WaitingGameManager;

public class LobbyServer extends Server {

    public static void msgHelp() {
        server.broadcastInfoMessage();
    }

    public static ExWorld getLobbyWorld() {
        return server.getLobbyWorld();
    }

    public static Build getBuild() {
        return server.getBuild();
    }

    public static GamesMenu getGamesMenu() {
        return server.getGamesMenu();
    }

    public static LobbyInventory getLobbyInventory() {
        return server.getLobbyInventory();
    }

    public static Sideboard getLobbySideboard() {
        return server.getLobbySideboard();
    }

    public static WaitingGameManager getWaitingGameManager() {
        return server.getWaitingGameManager();
    }

    private static final LobbyServerManager server = LobbyServerManager.getInstance();

}
