/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.object.Type;
import de.timesnake.database.util.server.DbNonTmpGameServer;
import de.timesnake.database.util.server.DbServer;
import de.timesnake.database.util.user.DbUser;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.game.NonTmpGameInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class OwnableNonTmpGameHub extends GameHub<NonTmpGameInfo> implements ChannelListener {

    private static String getFullServerName(UUID ownerUuid, String name) {
        return ownerUuid.hashCode() + "_" + name;
    }

    private static String getFullOwnerDisplayName(String serverName) {
        return serverName;
    }

    private static String getFullMemberDisplayName(String ownerName, String serverName) {
        return ownerName + " - " + serverName;
    }

    private static final ExItemStack SEPARATOR = new ExItemStack(Material.GRAY_STAINED_GLASS_PANE).setMoveable(false)
            .setDropable(false).setDisplayName(Component.empty()).immutable();

    private final OwnableNonTmpGameHubManager manager;

    private final UUID holder;
    private final HashMap<String, GameServerBasis> serverByName = new HashMap<>();

    public OwnableNonTmpGameHub(NonTmpGameInfo gameInfo, OwnableNonTmpGameHubManager manager, UUID holder, Collection<GameServerBasis> publicServers) {
        super(gameInfo);
        this.manager = manager;
        this.holder = holder;

        for (int i = 13; i < 56; i += 9) {
            this.inventory.setItemStack(i, SEPARATOR);
        }

        Server.getChannel().addListener(this);

        for (GameServerBasis server : publicServers) {
            this.inventory.setItemStack(server.getSlot(), server.getItem());
        }

        DbUser owner = Database.getUsers().getUser(this.holder);
        String ownerName = owner != null && owner.exists() ? owner.getName() : "unknown";

        for (String name : Server.getNetwork().getOwnerServerNames(this.holder, Type.Server.GAME, this.getGameInfo().getName())) {
            String fullName = getFullServerName(this.holder, name);
            String fullDisplayName = getFullOwnerDisplayName(name);
            UnloadedNonTmpGameServer server = new UnloadedNonTmpGameServer(this, fullName, name, fullDisplayName,
                    this.holder, ownerName, this.getEmptySlot(), false);
            this.serverByName.put(fullName, server);
            this.inventory.setItemStack(server.getSlot(), server.getItem());

            if (Database.getServers().containsServer(Type.Server.GAME, fullName)) {
                this.addGameServer(Database.getServers().getServer(Type.Server.GAME, fullName));
            }
        }

        for (Map.Entry<UUID, List<String>> namesByOwnerUuid : Server.getNetwork().getMemberServerNames(this.holder,
                Type.Server.GAME, this.getGameInfo().getName()).entrySet()) {
            UUID ownerUuid = namesByOwnerUuid.getKey();
            for (String name : namesByOwnerUuid.getValue()) {
                String fullName = getFullServerName(namesByOwnerUuid.getKey(), name);
                DbServer dbServer = Database.getServers().getServer(Type.Server.GAME, fullName);
                if (dbServer != null) {
                    Status.Server status = dbServer.getStatus();
                    if (status != null && status.isRunning()) {
                        this.addMemberServer(ownerUuid, name);
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

    protected void addGameServer(DbNonTmpGameServer server) {
        String serverName = server.getName();

        Integer oldSlot = this.removeServer(serverName);
        int slot = oldSlot != null ? oldSlot : this.getEmptySlot();

        UUID ownerUuid = server.getOwnerUuid();
        DbUser owner = Database.getUsers().getUser(ownerUuid);
        String ownerName = owner != null && owner.exists() ? owner.getName() : "unknown";

        String shortName = serverName.replaceFirst(ownerUuid.hashCode() + "_", "");
        String displayName = getFullOwnerDisplayName(shortName);

        List<UUID> memberUuids = Server.getNetwork().getPlayerServerMembers(ownerUuid, Type.Server.GAME,
                this.getGameInfo().getName(), shortName);

        OwnNonTmpGameServer gameServer = new OwnNonTmpGameServer(this, server, displayName, slot, ownerUuid, ownerName);

        this.serverByName.put(server.getName(), gameServer);

        this.updateServer(gameServer);

        for (UUID uuid : memberUuids) {
            if (this.manager.getHubByUuid().containsKey(uuid)) {
                this.manager.getHubByUuid().get(uuid).updateMemberServer(gameServer);
            }
        }
    }

    public void addMemberServer(OwnNonTmpGameServer server) {
        Integer oldSlot = this.removeServer(server.getName());
        int slot = oldSlot != null ? oldSlot : this.getEmptySlot();

        UUID ownerUuid = server.getOwner();
        String ownerName = server.getOwnerName();

        String shortServerName = server.getName().replaceFirst(ownerUuid.hashCode() + "_", "");

        NonTmpGameServer memberGameServer = new OwnNonTmpGameServer(this, ((DbNonTmpGameServer) server.getDatabase()),
                getFullMemberDisplayName(ownerName, shortServerName), slot, ownerUuid, ownerName);

        this.serverByName.put(memberGameServer.getName(), memberGameServer);
        this.updateServer(memberGameServer);
    }

    private void addMemberServer(UUID ownerUuid, String shortName) {
        int slot = this.getEmptySlot();

        DbUser owner = Database.getUsers().getUser(ownerUuid);
        String ownerName = owner != null && owner.exists() ? owner.getName() : "unknown";

        NonTmpGameServer memberGameServer = new OwnNonTmpGameServer(this,
                Database.getServers().getServer(Type.Server.GAME, getFullServerName(ownerUuid, shortName)),
                getFullMemberDisplayName(ownerName, shortName), slot, ownerUuid, ownerName);

        this.serverByName.put(memberGameServer.getName(), memberGameServer);
        this.updateServer(memberGameServer);
    }

    public void updateMemberServer(OwnNonTmpGameServer baseServer) {
        GameServerBasis server = this.serverByName.get(baseServer.getName());
        if (server == null || server instanceof UnloadedNonTmpGameServer) {
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

        if (this.serverByName.values().stream().anyMatch(s -> s.getName().equals(serverName)
                && !(s instanceof UnloadedNonTmpGameServer))) {
            return;
        }

        this.addGameServer((DbNonTmpGameServer) server);
    }
}
