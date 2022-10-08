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
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;

import java.util.Collections;

public class ServerUpdater implements ChannelListener {

    public ServerUpdater() {
        Server.getChannel().addListener(this, () -> Collections.singleton(Server.getNetwork().getName()));
    }

    @ChannelHandler(type = ListenerType.SERVER_ONLINE_PLAYERS, filtered = true)
    public void onServerMessage(ChannelServerMessage<?> msg) {
        LobbyServer.getLobbySideboard().setScore(6, "§6" + msg.getValue());

    }
}
