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
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbTmpGameInfo;
import de.timesnake.database.util.object.Type;
import de.timesnake.database.util.server.DbLoungeServer;
import de.timesnake.database.util.server.DbServer;
import de.timesnake.database.util.server.DbTaskServer;
import de.timesnake.database.util.server.DbTmpGameServer;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.game.TmpGameInfo;

import java.util.HashMap;

public class TmpGameHub extends GameHub<TmpGameInfo> implements ChannelListener {

    protected final HashMap<String, GameServerBasis> servers = new HashMap<>();

    public TmpGameHub(DbTmpGameInfo gameInfo) {
        super(new TmpGameInfo(gameInfo));

        this.loadServers();

        Server.getChannel().addListener(this);
    }

    protected void loadServers() {
        for (DbTmpGameServer server : Database.getServers().getServers(Type.Server.TEMP_GAME, this.gameInfo.getName())) {
            if (!server.getType().equals(Type.Server.TEMP_GAME)) {
                continue;
            }
            if (server.getTwinServerPort() != null) {
                this.addGameServer(server);
            }
        }
    }

    protected void addGameServer(DbTmpGameServer server) {
        DbLoungeServer loungeServer = server.getTwinServer();
        if (loungeServer != null && loungeServer.exists()) {
            Integer slot = this.getEmptySlot();
            TmpGameServer gameServer = new TmpGameServer(super.getServerNumber(slot), this, loungeServer, slot);
            this.servers.put(loungeServer.getName(), gameServer);
        } else {
            Server.printWarning(Plugin.LOBBY, "Can not load game server " + server.getName() + ", lounge not found", "GameHub");
        }
    }

    @Override
    public void updateServer(GameServer<?> server) {
        if (server.getStatus().equals(Status.Server.OFFLINE)) {
            server.destroy();
            this.removeServer(((TmpGameServer) server));
        } else {
            super.updateServer(server);
        }
    }

    public void removeServer(TmpGameServer server) {
        this.inventory.removeItemStack(server.getItem().getSlot());
    }

    @ChannelHandler(type = ListenerType.SERVER_STATUS)
    public void onServerMessage(ChannelServerMessage<?> msg) {
        DbServer server = Database.getServers().getServer(msg.getName());
        if (!(server instanceof DbTmpGameServer || server instanceof DbLoungeServer)) {
            return;
        }

        String task = ((DbTaskServer) server).getTask();
        if (task == null) {
            return;
        }

        if (!task.equals(this.getGameInfo().getName())) {
            return;
        }

        if (this.servers.containsKey(server.getName())) {
            return;
        }

        if (server.getType().equals(Type.Server.LOUNGE)) {
            DbTmpGameServer gameServer = ((DbLoungeServer) server).getTwinServer();


            if (gameServer == null || !gameServer.exists()) {
                return;
            }

            if (gameServer.getName() == null) {
                return;
            }

            if (this.servers.containsKey(gameServer.getName())) {
                return;
            }

            this.addGameServer(gameServer);

        }
    }
}
