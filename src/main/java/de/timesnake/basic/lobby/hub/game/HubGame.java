package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.game.GameInfo;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.basic.lobby.user.LobbyUser;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.object.Type;
import de.timesnake.database.util.server.DbGameServer;
import de.timesnake.database.util.server.DbServer;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;

public class HubGame extends GameInfo implements UserInventoryClickListener, ChannelListener {

    public static final ExItemStack BACK = new ExItemStack(Material.BLUE_BED, 1, "§cBack", List.of("§fClick to get " +
            "back"));

    public static final Integer SERVER_SLOTS_START = 9;

    protected final ExInventory inventory;
    protected final HashMap<String, TaskServer> servers = new HashMap<>();
    protected final ExItemStack item;

    public HubGame(DbGame<?> game) {
        super(game.getInfo());
        this.inventory = Server.createExInventory(54, super.getDisplayName());
        this.inventory.setItemStack(4, BACK);
        this.item = new ExItemStack(super.item);

        this.loadServers();

        Server.getInventoryEventManager().addClickListener(this, this.item);

        Server.getChannel().addListener(this);
    }

    protected void loadServers() {
        for (DbGameServer server : Database.getServers().getServers(Type.Server.GAME, this.name)) {
            this.addGameServer(server);
        }

        Server.printText(Plugin.LOBBY, "Game-Servers for game " + this.name + " loaded successfully", "GameHub");
    }

    protected void addGameServer(DbGameServer server) {
        Integer slot = this.getEmptySlot();
        TaskServer taskServer = new TaskServer(this.getServerNumber(slot), this, server, slot);
        this.servers.put(server.getName(), taskServer);
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

    public String getName() {
        return this.name;
    }

    public ExItemStack getInvItem() {
        return this.item;
    }

    public Integer getSlot() {
        return this.slot;
    }

    public ExInventory getInventory() {
        return this.inventory;
    }

    public void openServersInventory(User user) {
        user.openInventory(this.inventory);
    }

    public void removeServer(String name) {
        if (this.servers.containsKey(name)) {
            TaskServer server = this.servers.get(name);

            this.inventory.removeItemStack(this.servers.get(name).getItem().getSlot());
            this.servers.remove(name);
            Server.printText(Plugin.LOBBY, "Removed server " + name + " from game-hub", "GameHub");
        }
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

        if (!task.equals(this.name)) {
            return;
        }

        if (this.servers.containsKey(server.getName())) {
            return;
        }

        this.addGameServer((DbGameServer) server);
    }

    @Override
    public org.bukkit.inventory.ItemStack getItem() {
        return this.item;
    }
}
