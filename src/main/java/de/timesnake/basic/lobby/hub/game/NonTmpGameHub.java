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

package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbNonTmpGameInfo;
import de.timesnake.database.util.object.Type;
import de.timesnake.database.util.server.DbNonTmpGameServer;
import de.timesnake.database.util.server.DbServer;
import de.timesnake.library.game.NonTmpGameInfo;

import java.util.HashMap;

public class NonTmpGameHub extends GameHub<NonTmpGameInfo> implements ChannelListener {

    protected final HashMap<String, GameServerBasis> servers = new HashMap<>();
    private final CreationRequestManager creationRequestManager;

    public NonTmpGameHub(DbNonTmpGameInfo gameInfo) {
        super(new NonTmpGameInfo(gameInfo));
        Server.getChannel().addListener(this);

        this.creationRequestManager = new CreationRequestManager(this);
        this.inventory.setItemStack(1, this.creationRequestManager.getItem());

        this.loadServers();
    }

    protected void loadServers() {
        for (DbNonTmpGameServer server : Database.getServers().getServers(Type.Server.GAME, this.gameInfo.getName())) {
            this.addGameServer(server);
        }
    }

    public void removeServer(String name) {
        if (this.servers.containsKey(name)) {
            this.inventory.removeItemStack(this.servers.get(name).getItem().getSlot());
            this.servers.remove(name);
        }
    }

    protected void addGameServer(DbNonTmpGameServer server) {
        Integer slot = this.getEmptySlot();
        GameServer<NonTmpGameInfo> gameServer = new GameServer<>(this.getGameInfo().getDisplayName() + " " +
                this.getServerNumber(slot), this, server, slot, true);
        this.servers.put(server.getName(), gameServer);
    }

    @ChannelHandler(type = ListenerType.SERVER_STATUS)
    public void onServerMessage(ChannelServerMessage<?> msg) {
        DbServer server = Database.getServers().getServer(msg.getName());
        if (!(server instanceof DbNonTmpGameServer)) {
            return;
        }

        String task = ((DbNonTmpGameServer) server).getTask();
        if (task == null) {
            return;
        }

        if (!task.equals(this.gameInfo.getName())) {
            return;
        }

        if (this.servers.containsKey(server.getName())) {
            return;
        }

        this.addGameServer((DbNonTmpGameServer) server);
    }
}
