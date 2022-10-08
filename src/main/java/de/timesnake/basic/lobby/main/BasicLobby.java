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

package de.timesnake.basic.lobby.main;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.basic.lobby.server.LobbyServerManager;
import de.timesnake.basic.lobby.user.LobbyCmd;
import org.bukkit.plugin.java.JavaPlugin;

public class BasicLobby extends JavaPlugin {


    public static BasicLobby getPlugin() {
        return plugin;
    }

    private static BasicLobby plugin;

    @Override
    public void onLoad() {
        ServerManager.setInstance(new LobbyServerManager());
    }

    @Override
    public void onEnable() {
        plugin = this;

        Server.getCommandManager().addCommand(this, "lobbybuild", new LobbyCmd(), Plugin.LOBBY);
        Server.registerListener(LobbyServerManager.getInstance(), this);

        LobbyServerManager.getInstance().onLobbyEnable();

    }
}
