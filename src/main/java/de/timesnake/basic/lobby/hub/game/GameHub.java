package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.basic.lobby.user.LobbyUser;
import de.timesnake.database.util.server.DbNonTmpGameServer;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;

public abstract class GameHub<GameInfo extends de.timesnake.library.game.GameInfo> implements UserInventoryClickListener {

    public static final ExItemStack BACK = new ExItemStack(Material.BLUE_BED, 1, "§cBack",
            List.of("§fClick to get back"));

    public static final Integer SERVER_SLOTS_START = 9;

    protected final GameInfo gameInfo;

    protected final ExInventory inventory;
    protected final HashMap<String, GameServerBasis> servers = new HashMap<>();
    protected final ExItemStack item;

    public GameHub(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
        this.inventory = Server.createExInventory(54, this.gameInfo.getDisplayName());
        this.inventory.setItemStack(4, BACK);
        this.item = new ExItemStack(this.gameInfo.getItem());

        this.loadServers();

        Server.getInventoryEventManager().addClickListener(this, this.item);
    }

    protected abstract void loadServers();

    protected void addGameServer(DbNonTmpGameServer server) {
        Integer slot = this.getEmptySlot();
        GameServer<GameInfo> gameServer = new GameServer<>(this.getGameInfo().getDisplayName() + " " +
                this.getServerNumber(slot), this, server, slot, true);
        this.servers.put(server.getName(), gameServer);
    }

    protected Integer getServerNumber(Integer slot) {
        return slot - SERVER_SLOTS_START;
    }

    public Integer getEmptySlot() {
        for (int i = SERVER_SLOTS_START; i < this.inventory.getSize(); i++) {
            if (this.inventory.getInventory().getItem(i) == null) {
                return i;
            }
        }
        return 9;
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent event) {
        LobbyUser user = (LobbyUser) event.getUser();
        user.playSoundItemClicked();
        this.openServersInventory(user);
        event.setCancelled(true);
    }

    public ExItemStack getInvItem() {
        return this.item;
    }

    public ExInventory getInventory() {
        return this.inventory;
    }

    public void openServersInventory(User user) {
        user.openInventory(this.inventory);
    }

    public void removeServer(String name) {
        if (this.servers.containsKey(name)) {
            this.inventory.removeItemStack(this.servers.get(name).getItem().getSlot());
            this.servers.remove(name);
        }
    }

    public org.bukkit.inventory.ItemStack getItem() {
        return this.item;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }
}
