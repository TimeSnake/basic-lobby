/*
 * Copyright (C) 2023 timesnake
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
