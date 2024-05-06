/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.lobby.hub.server.GameServer;
import de.timesnake.basic.lobby.hub.server.GameServerBasis;
import de.timesnake.basic.lobby.hub.server.NonTmpGameServer;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbNonTmpGameInfo;
import de.timesnake.database.util.server.DbNonTmpGameServer;
import de.timesnake.database.util.server.DbServer;
import de.timesnake.library.basic.util.ServerType;
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
    for (DbServer server : Database.getServers().getServers(ServerType.GAME, this.gameInfo.getName())) {
      this.addGameServer(((DbNonTmpGameServer) server));
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
    GameServer<NonTmpGameInfo> gameServer = new NonTmpGameServer(this, server,
        this.getGameInfo().getDisplayName() + " " + this.getServerNumber(slot), slot);
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
