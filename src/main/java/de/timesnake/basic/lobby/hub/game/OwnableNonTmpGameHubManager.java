/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.lobby.hub.server.*;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbNonTmpGameInfo;
import de.timesnake.database.util.server.DbNonTmpGameServer;
import de.timesnake.database.util.server.DbServer;
import de.timesnake.library.basic.util.MultiKeyMap;
import de.timesnake.library.basic.util.ServerType;
import de.timesnake.library.game.NonTmpGameInfo;
import de.timesnake.library.network.NetworkServer;

import java.util.HashMap;
import java.util.UUID;

public class OwnableNonTmpGameHubManager extends GameHub<NonTmpGameInfo> implements
    ChannelListener {

  private final HashMap<UUID, OwnableNonTmpGameHub> hubByUuid = new HashMap<>();
  private final MultiKeyMap<String, Integer, GameServerBasis> publicServersByNameOrSlot = new MultiKeyMap<>();

  public OwnableNonTmpGameHubManager(DbNonTmpGameInfo gameInfo) {
    super(new NonTmpGameInfo(gameInfo));

    Server.getChannel().addListener(this);
    this.loadPublicSaves();
  }

  protected void loadPublicSaves() {
    int slot = 9;
    for (String name : Server.getNetwork().getPublicSaveNames(ServerType.GAME, this.getGameInfo().getName())) {
      String serverName = NetworkServer.getPublicSaveServerName(this.getGameInfo().getName(), name);
      this.publicServersByNameOrSlot.put(serverName, slot,
          new NonTmpGameSave(this, name, name, null, null, slot, true));
      slot++;

      if (slot % 9 >= 4) {
        slot += 5;
      }
    }
  }

  protected void addPublicServer(DbNonTmpGameServer server) {
    Integer oldSlot = this.removeServer(server.getName());
    int slot = oldSlot != null ? oldSlot : this.getEmptySlot();

    String name = NetworkServer.getPublicSaveNameFromServerName(server.getName(), this.getGameInfo().getName());
    NonTmpGameServer gameServer = new PublicNonTmpGameServer(this, server, name, slot);

    this.publicServersByNameOrSlot.put(gameServer.getServerName(), slot, gameServer);
    this.updateServer(gameServer);
  }

  @Override
  public void updateServer(GameServer<?> server) {
    this.hubByUuid.values().forEach(h -> h.updateServer(server));
  }

  public Integer removeServer(String name) {

    GameServerBasis server = this.publicServersByNameOrSlot.remove(name,
        this.publicServersByNameOrSlot.get1(name).getSlot());

    if (server != null) {
      for (OwnableNonTmpGameHub hub : this.hubByUuid.values()) {
        hub.removePublicServer(server);
      }
      server.destroy();
      return server.getSlot();
    }
    return null;
  }

  @Override
  public void openServersInventory(User user) {
    user.openInventory(this.hubByUuid.computeIfAbsent(user.getUniqueId(),
        uuid -> new OwnableNonTmpGameHub(this.getGameInfo(), this, uuid,
            this.publicServersByNameOrSlot.values())).getInventory());
  }

  @ChannelHandler(type = ListenerType.SERVER_STATUS)
  public void onServerMessage(ChannelServerMessage<?> msg) {
    DbServer server = Database.getServers().getServer(msg.getName());
    if (!(server instanceof DbNonTmpGameServer)) {
      return;
    }

    String task = ((DbNonTmpGameServer) server).getTask();
    if (task == null || !task.equals(this.gameInfo.getName())) {
      return;
    }

    String serverName = server.getName();

    if (this.publicServersByNameOrSlot.containsKey1(serverName)
        && !(this.publicServersByNameOrSlot.get1(serverName) instanceof NonTmpGameSave)) {
      return;
    }

    if (((DbNonTmpGameServer) server).getOwnerUuid() != null) {
      return;
    }

    this.addPublicServer((DbNonTmpGameServer) server);
  }

  public HashMap<UUID, OwnableNonTmpGameHub> getHubByUuid() {
    return hubByUuid;
  }
}
