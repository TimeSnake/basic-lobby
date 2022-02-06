package de.timesnake.basic.lobby.server;

import de.timesnake.channel.api.message.ChannelServerMessage;
import de.timesnake.channel.listener.ChannelServerListener;

public class ServerUpdater implements ChannelServerListener {

    @Override
    public void onServerMessage(ChannelServerMessage msg) {
        if (msg.getType().equals(ChannelServerMessage.MessageType.ONLINE_PLAYERS)) {
            LobbyServer.getLobbySideboard().setScore(6, "§6" + msg.getValue());
        }

    }
}
