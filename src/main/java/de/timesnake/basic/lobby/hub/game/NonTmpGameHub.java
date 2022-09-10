package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbNonTmpGameInfo;
import de.timesnake.database.util.object.Type;
import de.timesnake.database.util.server.DbNonTmpGameServer;
import de.timesnake.database.util.server.DbServer;
import de.timesnake.library.game.NonTmpGameInfo;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NonTmpGameHub extends GameHub<NonTmpGameInfo> implements ChannelListener {

    private static final ExItemStack SEPARATOR = new ExItemStack(Material.YELLOW_STAINED_GLASS_PANE).setMoveable(false)
            .setDropable(false).setDisplayName(Component.empty()).immutable();

    private final Map<UUID, HashMap<String, GameServerBasis>> playerServersByNameByUuid = new HashMap<>();

    public NonTmpGameHub(DbNonTmpGameInfo gameInfo) {
        super(new NonTmpGameInfo(gameInfo));

        if (this.getGameInfo().isOwnable()) {
            for (int i = 27; i < 36; i++) {
                this.inventory.setItemStack(i, SEPARATOR);
            }
        }

        Server.getChannel().addListener(this);
    }

    @Override
    protected void loadServers() {
        for (String name : Server.getNetwork().getPublicPlayerServerNames(Type.Server.GAME, this.getGameInfo().getName())) {
            this.addGameServer(name, true);
        }
    }

    @Override
    protected void addGameServer(DbNonTmpGameServer server) {
        if (this.servers.containsKey(server.getName())) {
            this.removeServer(server.getName());
        }

        Integer slot = this.getEmptySlot();
        UUID ownerUuid = server.getOwnerUuid();
        String serverName = server.getName(); //ownerUuid != null ? server.getName().replaceFirst(String.valueOf(ownerUuid.hashCode()), "") : server.getName();
        NonTmpGameServer gameServer = new NonTmpGameServer(this, server, serverName, slot, ownerUuid);
        this.servers.put(server.getName(), gameServer);

        if (gameServer.getOwner() != null) {
            this.playerServersByNameByUuid.computeIfAbsent(gameServer.getOwner(), uuid -> new HashMap<>())
                    .put(server.getName(), gameServer);


            for (UUID uuid : Server.getNetwork().getPlayerServerMembers(gameServer.getOwner(), Type.Server.GAME,
                    this.getGameInfo().getName(), gameServer.getName())) {
                if (!this.playerServersByNameByUuid.containsKey(uuid)) {
                    this.loadPlayerServers(uuid);
                }

                this.playerServersByNameByUuid.get(uuid).put(gameServer.getName(), gameServer);
            }
        }
    }

    @Override
    public void removeServer(String name) {
        super.removeServer(name);
        this.playerServersByNameByUuid.values().forEach(m -> m.remove(name));
    }

    protected GameServerBasis addGameServer(String name, boolean isPublic) {
        Integer slot = this.getEmptySlot();
        UnloadedNonTmpGameServer gameServer = new UnloadedNonTmpGameServer(this, name, slot, isPublic);
        this.servers.put(name, gameServer);
        return gameServer;
    }

    @Override
    public void openServersInventory(User user) {
        if (!this.getGameInfo().isOwnable()) {
            super.openServersInventory(user);
            return;
        }

        UUID uuid = user.getUniqueId();

        ExInventory inv = Server.createExInventory(54, this.gameInfo.getDisplayName());

        int slot = 0;
        for (ItemStack item : this.inventory.getInventory().getContents()) {
            inv.setItemStack(slot, item);
            slot++;
        }

        if (!this.playerServersByNameByUuid.containsKey(uuid)) {
            this.loadPlayerServers(uuid);
        }

        slot = 36;
        for (GameServerBasis server : this.playerServersByNameByUuid.get(uuid).values()) {
            inv.setItemStack(slot, server.getItem());
            slot++;
        }

        user.openInventory(inv);
    }

    private void loadPlayerServers(UUID uuid) {
        this.playerServersByNameByUuid.putIfAbsent(uuid, new HashMap<>());
        for (String name : Server.getNetwork().getOwnerServerNames(uuid, Type.Server.GAME, this.getGameInfo().getName())) {
            GameServerBasis server = this.addGameServer(uuid.hashCode() + "_" + name, false);
            this.playerServersByNameByUuid.get(uuid).put(server.getName(), server);
        }
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

        if (this.servers.containsKey(server.getName())
                && !(this.servers.get(server.getName()) instanceof UnloadedNonTmpGameServer)) {
            return;
        }

        this.addGameServer((DbNonTmpGameServer) server);
    }
}
