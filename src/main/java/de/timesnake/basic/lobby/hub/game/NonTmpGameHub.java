package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbNonTmpGameInfo;
import de.timesnake.database.util.object.Type;
import de.timesnake.database.util.server.DbGameServer;
import de.timesnake.database.util.server.DbServer;
import de.timesnake.library.game.NonTmpGameInfo;

public class NonTmpGameHub extends GameHub<NonTmpGameInfo> implements ChannelListener {

    public NonTmpGameHub(DbNonTmpGameInfo gameInfo) {
        super(new NonTmpGameInfo(gameInfo));
        Server.getChannel().addListener(this);
    }

    protected void loadServers() {
        for (DbGameServer server : Database.getServers().getServers(Type.Server.GAME, this.gameInfo.getName())) {
            this.addGameServer(server);
        }

        Server.printText(Plugin.LOBBY, "Game-Servers for game " + this.gameInfo.getName() + " loaded successfully", "GameHub");
    }

    @ChannelHandler(type = ListenerType.SERVER_STATUS)
    public void onServerMessage(ChannelServerMessage<?> msg) {
        DbServer server = Database.getServers().getServer(msg.getPort());
        if (!(server instanceof DbGameServer)) {
            return;
        }

        String task = ((DbGameServer) server).getTask();
        if (task == null) {
            return;
        }

        if (!task.equals(this.gameInfo.getName())) {
            return;
        }

        if (this.servers.containsKey(server.getName())) {
            return;
        }

        this.addGameServer((DbGameServer) server);
    }
}
