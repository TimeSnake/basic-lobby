/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.lobby.hub.server.*;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.server.DbNonTmpGameServer;
import de.timesnake.database.util.server.DbServer;
import de.timesnake.database.util.user.DbUser;
import de.timesnake.library.basic.util.ServerType;
import de.timesnake.library.game.NonTmpGameInfo;
import de.timesnake.library.network.NetworkServer;
import org.bukkit.Material;

import java.util.*;

public class OwnableNonTmpGameHub extends GameHub<NonTmpGameInfo> implements ChannelListener {

  private static final ExItemStack SEPARATOR = new ExItemStack(Material.GRAY_STAINED_GLASS_PANE)
      .setMoveable(false)
      .setDropable(false)
      .setDisplayName("")
      .immutable();

  private final OwnableNonTmpGameHubManager manager;

  private final UUID holder;
  private final HashMap<String, GameServerBasis> serverByName = new HashMap<>();

  public OwnableNonTmpGameHub(NonTmpGameInfo gameInfo, OwnableNonTmpGameHubManager manager,
                              UUID holder, Collection<GameServerBasis> publicSavesAndServers) {
    super(gameInfo);
    this.manager = manager;
    this.holder = holder;

    for (int i = 13; i < 56; i += 9) {
      this.inventory.setItemStack(i, SEPARATOR);
    }

    Server.getChannel().addListener(this);

    this.loadPublicSavesAndServers(publicSavesAndServers);
    this.loadPrivateSavesAndServers();
  }

  private void loadPublicSavesAndServers(Collection<GameServerBasis> savesAndServers) {
    for (GameServerBasis server : savesAndServers) {
      this.inventory.setItemStack(server.getSlot(), server.getItem());
    }
  }

  private void loadPrivateSavesAndServers() {
    DbUser owner = Database.getUsers().getUser(this.holder);
    String ownerName = owner != null && owner.exists() ? owner.getName() : "unknown";

    for (String name : Server.getNetwork().getPrivateSaveNames(this.holder, ServerType.GAME,
        this.getGameInfo().getName())) {
      String serverName = NetworkServer.getPrivateSaveServerName(this.getGameInfo().getName(), this.holder, name);
      NonTmpGameSave server = new NonTmpGameSave(this, name, name, this.holder, ownerName, this.getEmptySlot(), false);
      this.serverByName.put(serverName, server);

      if (Database.getServers().containsServer(ServerType.GAME, serverName)) {
        this.addOwnedPrivateServer(Database.getServers().getServer(ServerType.GAME, serverName));
      }
    }

    for (Map.Entry<UUID, List<String>> namesByOwnerUuid : Server.getNetwork().getMemberSaveNames(this.holder,
        ServerType.GAME, this.getGameInfo().getName()).entrySet()) {
      UUID ownerUuid = namesByOwnerUuid.getKey();

      for (String name : namesByOwnerUuid.getValue()) {
        String serverName = NetworkServer.getPrivateSaveServerName(this.getGameInfo().getName(), ownerUuid, name);
        DbServer server = Database.getServers().getServer(ServerType.GAME, serverName);
        if (server != null) {
          if (server.getStatus().isRunning()) {
            this.addMemberServer((DbNonTmpGameServer) server, ownerUuid, name);
          }
        }
      }
    }
  }

  @Override
  public Integer getEmptySlot() {
    int slot = 14;

    while (this.inventory.getInventory().getItem(slot) != null) {
      slot++;

      if (slot % 9 < 5) {
        slot += 5;
      }
    }
    return slot;
  }

  protected void addOwnedPrivateServer(DbNonTmpGameServer server) {
    String serverName = server.getName();

    Integer oldSlot = this.removeServer(serverName);
    int slot = oldSlot != null ? oldSlot : this.getEmptySlot();

    UUID ownerUuid = server.getOwnerUuid();
    DbUser owner = Database.getUsers().getUser(ownerUuid);
    String ownerName = owner != null && owner.exists() ? owner.getName() : "unknown";

    assert ownerUuid != null;
    String name = NetworkServer.getPrivateSaveNameFromServerName(serverName, ownerUuid);

    List<UUID> memberUuids = Server.getNetwork().getPrivateSaveMembers(ownerUuid, ServerType.GAME,
        this.getGameInfo().getName(), name);

    OwnNonTmpGameServer gameServer = new OwnNonTmpGameServer(this, server, name, slot,
        ownerUuid, ownerName);

    this.serverByName.put(serverName, gameServer);

    this.updateServer(gameServer);

    for (UUID uuid : memberUuids) {
      if (this.manager.getHubByUuid().containsKey(uuid)) {
        this.manager.getHubByUuid().get(uuid).updateMemberServer(gameServer);
      }
    }
  }

  public void addMemberServer(OwnNonTmpGameServer ownerServer) {
    Integer oldSlot = this.removeServer(ownerServer.getServerName());
    int slot = oldSlot != null ? oldSlot : this.getEmptySlot();

    UUID ownerUuid = ownerServer.getOwner();
    String ownerName = ownerServer.getOwnerName();

    String displayName = ownerName + " - " + ownerServer.getDisplayName();

    NonTmpGameServer server = new OwnNonTmpGameServer(this, (DbNonTmpGameServer) ownerServer.getDatabase(),
        displayName, slot, ownerUuid, ownerName);

    this.serverByName.put(server.getServerName(), server);
    this.updateServer(server);
  }

  private void addMemberServer(DbNonTmpGameServer server, UUID ownerUuid, String name) {
    int slot = this.getEmptySlot();

    DbUser owner = Database.getUsers().getUser(ownerUuid);
    String ownerName = owner != null && owner.exists() ? owner.getName() : "unknown";

    NonTmpGameServer memberGameServer = new OwnNonTmpGameServer(this, server,
        ownerName + " - " + name, slot, ownerUuid, ownerName);

    this.serverByName.put(memberGameServer.getServerName(), memberGameServer);
    this.updateServer(memberGameServer);
  }

  public void updateMemberServer(OwnNonTmpGameServer baseServer) {
    GameServerBasis server = this.serverByName.get(baseServer.getServerName());
    if (server == null || server instanceof NonTmpGameSave) {
      this.addMemberServer(baseServer);
    }
  }

  public Integer removeServer(String name) {
    GameServerBasis server = this.serverByName.remove(name);

    if (server != null) {
      this.inventory.removeItemStack(server.getSlot());
      server.destroy();
      return server.getSlot();
    }
    return null;
  }

  public void removePublicServer(GameServerBasis server) {
    this.inventory.removeItemStack(server.getSlot());
  }

  @Override
  public void updateServer(GameServer<?> gameServer) {
    this.inventory.setItemStack(gameServer.getSlot(), gameServer.getItem());
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

    UUID serverOwner = ((DbNonTmpGameServer) server).getOwnerUuid();
    if (!this.holder.equals(serverOwner)) {
      return;
    }

    if (this.serverByName.values().stream().anyMatch(s -> s.getServerName().equals(serverName)
                                                          && !(s instanceof NonTmpGameSave))) {
      return;
    }

    this.addOwnedPrivateServer((DbNonTmpGameServer) server);
  }
}
