package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.object.Type;
import de.timesnake.database.util.server.DbLoungeServer;
import de.timesnake.database.util.server.DbServer;
import de.timesnake.database.util.server.DbTaskServer;
import de.timesnake.database.util.server.DbTempGameServer;

public class HubTempGame extends HubGame implements ChannelListener {

    public HubTempGame(DbGame game) {
        super(game);

        Server.getChannel().addListener(this);
    }

    @Override
    protected void loadServers() {
        for (DbServer server : Database.getServers().getServers(Type.Server.TEMP_GAME, this.name)) {
            if (!server.getType().equals(Type.Server.TEMP_GAME)) {
                continue;
            }
            if (((DbTempGameServer) server).getTwinServerPort() != null) {
                this.addGameServer(((DbTempGameServer) server));
            }
        }

        Server.printText(Plugin.LOBBY, "Game-Servers for temp-game " + this.name + " loaded successfully", "GameHub");
    }

    protected void addGameServer(DbTempGameServer server) {
        DbLoungeServer loungeServer = server.getTwinServer();
        if (loungeServer != null && loungeServer.exists()) {
            Integer slot = this.getEmptySlot();
            TempGameServer gameServer = new TempGameServer(super.getServerNumber(slot), this, loungeServer, slot);
            this.servers.put(loungeServer.getName(), gameServer);
        } else {
            Server.printWarning(Plugin.LOBBY, "Can not load game server " + server.getName() + ", lounge not found",
                    "GameHub");
        }
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent e) {
        User user = e.getUser();
        user.playSoundItemClicked();
        this.openServersInventory(user);
        e.setCancelled(true);
    }

    @ChannelHandler(type = ListenerType.SERVER_STATUS)
    public void onServerMessage(ChannelServerMessage<?> msg) {
        DbServer server = Database.getServers().getServer(msg.getPort());
        if (!(server instanceof DbTempGameServer || server instanceof DbLoungeServer)) {
            return;
        }

        String task = ((DbTaskServer) server).getTask();
        if (task == null) {
            return;
        }

        if (!task.equals(this.name)) {
            return;
        }

        if (this.servers.containsKey(server.getName())) {
            return;
        }

        if (server.getType().equals(Type.Server.LOUNGE)) {
            DbTempGameServer gameServer = ((DbLoungeServer) server).getTwinServer();


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
