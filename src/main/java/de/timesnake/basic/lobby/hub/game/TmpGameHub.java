/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbTmpGameInfo;
import de.timesnake.database.util.server.DbLoungeServer;
import de.timesnake.database.util.server.DbServer;
import de.timesnake.database.util.server.DbTaskServer;
import de.timesnake.database.util.server.DbTmpGameServer;
import de.timesnake.library.basic.util.ServerType;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.game.TmpGameInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class TmpGameHub extends GameHub<TmpGameInfo> implements ChannelListener {

  private final Logger logger = LogManager.getLogger("lobby.gamehub");

  protected final HashMap<String, GameServerBasis> servers = new HashMap<>();

  public TmpGameHub(DbTmpGameInfo gameInfo) {
    super(new TmpGameInfo(gameInfo));

    this.loadServers();

    Server.getChannel().addListener(this);
  }

  protected void loadServers() {
    for (DbServer server : Database.getServers().getServers(ServerType.TEMP_GAME, this.gameInfo.getName())) {
      if (!server.getType().equals(ServerType.TEMP_GAME)) {
        continue;
      }
      if (((DbTmpGameServer) server).getTwinServerName() != null) {
        this.addGameServer(((DbTmpGameServer) server));
      }
    }
  }

  protected void addGameServer(DbTmpGameServer server) {
    DbLoungeServer loungeServer = server.getTwinServer();
    if (loungeServer != null && loungeServer.exists()) {
      Integer slot = this.getEmptySlot();
      TmpGameServer gameServer = new TmpGameServer(super.getServerNumber(slot), this,
          loungeServer, slot);
      this.servers.put(loungeServer.getName(), gameServer);
    } else {
      this.logger.warn("Can not load game server {}, lounge not found", server.getName());
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

    if (server.getType().equals(ServerType.LOUNGE)) {
      DbTmpGameServer gameServer = ((DbLoungeServer) server).getTwinServer();

      if (gameServer == null || !gameServer.exists()) {
        return;
      }

      gameServer.getName();

      if (this.servers.containsKey(gameServer.getName())) {
        return;
      }

      this.addGameServer(gameServer);

    }
  }
}
