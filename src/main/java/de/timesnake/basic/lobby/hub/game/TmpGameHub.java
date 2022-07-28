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
import de.timesnake.library.game.TmpGameInfo;

public class TmpGameHub extends GameHub<TmpGameInfo> implements ChannelListener {

    public TmpGameHub(DbTmpGameInfo gameInfo) {
        super(new TmpGameInfo(gameInfo));

        Server.getChannel().addListener(this);
    }

    @Override
    protected void loadServers() {
        for (DbTmpGameServer server : Database.getServers().getServers(Type.Server.TEMP_GAME, this.gameInfo.getName())) {
            if (!server.getType().equals(Type.Server.TEMP_GAME)) {
                continue;
            }
            if (server.getTwinServerPort() != null) {
                this.addGameServer(server);
            }
        }

        Server.printText(Plugin.LOBBY, "Game-Servers for temp-game " + this.gameInfo.getName() + " loaded successfully", "GameHub");
    }

    protected void addGameServer(DbTmpGameServer server) {
        DbLoungeServer loungeServer = server.getTwinServer();
        if (loungeServer != null && loungeServer.exists()) {
            Integer slot = this.getEmptySlot();
            TmpGameServer gameServer = new TmpGameServer(super.getServerNumber(slot), this, loungeServer, slot);
            this.servers.put(loungeServer.getName(), gameServer);
        } else {
            Server.printWarning(Plugin.LOBBY, "Can not load game server " + server.getName() + ", lounge not found",
                    "GameHub");
        }
    }

    @ChannelHandler(type = ListenerType.SERVER_STATUS)
    public void onServerMessage(ChannelServerMessage<?> msg) {
        DbServer server = Database.getServers().getServer(msg.getPort());
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
