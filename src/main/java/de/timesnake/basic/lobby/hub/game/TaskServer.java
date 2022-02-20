package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.server.ServerInfo;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.basic.bukkit.util.user.event.UserQuitEvent;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.basic.lobby.hub.ServerPasswordCmd;
import de.timesnake.basic.lobby.user.LobbyUser;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.server.DbTaskServer;
import de.timesnake.library.basic.util.Status;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public class TaskServer extends ServerInfo implements ChannelListener, UserInventoryClickListener, Listener {

    protected static final Material ONLINE = Material.GREEN_WOOL;
    protected static final Material ONLINE_FULL = Material.YELLOW_WOOL;
    protected static final Material SERVICE = Material.RED_WOOL;
    protected static final Material OFFLINE = Material.GRAY_WOOL;
    protected static final Material IN_GAME = Material.ORANGE_WOOL;

    protected static final String SERVER_TITLE_COLOR = org.bukkit.ChatColor.GOLD + "";

    protected static final String SERVER_NAME_COLOR = ChatColor.DARK_GRAY + "";

    protected static final String ONLINE_TEXT = "§aOnline";
    protected static final String OFFLINE_TEXT = "§cOffline";
    protected static final String INGAME_TEXT = "§eIn-game";
    protected static final String SERVICE_TEXT = "§6Service Work";
    protected static final String PLAYER_TEXT = "§9Players:";
    protected static final String PASSWORD_TEXT = "§cThis server is password protected!";
    protected static final String KIT_TEXT = "§bKits";
    protected static final String MAP_TEXT = "§bMaps";
    protected static final String OLD_PVP_TEXT = "§cpre1.9 PvP (1.8 PvP)";
    protected static final String PLAYERS_PER_TEAM_TEXT = "§3Players per Team: §f";
    protected static final String TEAM_AMOUNT = "§3Teams: §f";
    protected static final String QUEUE_JOIN_TEXT = "§7Right click to join the queue";
    protected static final String SPECTATOR_JOIN_TEXT = "§7Left click to join as spectator";

    protected static final String INGAME_OFFLINE_MESSAGE = ChatColor.WARNING + "This server is offline or in-game";
    protected static final String SERVICE_MESSAGE = ChatColor.WARNING + "This server is in service-mode";
    protected static final String FULL_MESSAGE = ChatColor.WARNING + "This server is full";

    protected final String displayName;
    protected final Integer serverNumber;
    protected String serverName;

    protected final String task;

    protected final ExItemStack item;

    protected final HubGame hubGame;

    protected boolean queueing;
    protected final Queue<User> queue = new LinkedList<>();

    public TaskServer(Integer serverNumber, HubGame hubGame, DbTaskServer server, int slot) {
        super(server);
        this.serverNumber = serverNumber;
        this.displayName = hubGame.getDisplayName() + " " + serverNumber;
        this.serverName = server.getName();
        this.task = server.getTask();
        this.hubGame = hubGame;

        this.item = new ExItemStack(slot, Material.WHITE_WOOL, SERVER_TITLE_COLOR + this.displayName);

        this.initUpdate();

        Server.getInventoryEventManager().addClickListener(this, this.item);

        System.out.println("new " + this.port);

        Server.getChannel().addListener(this, () -> Collections.singleton(this.port));
    }

    public String getDisplayName() {
        return displayName;
    }

    public Integer getServerNumber() {
        return serverNumber;
    }

    protected void initUpdate() {
        this.updateItemAmount();
        this.updateItemDescription();
        this.updateItem();
    }

    protected void updateItemAmount() {
        if (this.onlinePlayers != null && this.onlinePlayers != 0) {
            this.item.setAmount(this.onlinePlayers);
        } else {
            this.item.setAmount(1);
        }
        this.updateItem();
    }

    protected void updateItemDescription() {
        List<String> lore = new ArrayList<>();
        lore.add("");

        if (this.status == null) {
            this.setStatus(Status.Server.OFFLINE);
        }

        if (Status.Server.ONLINE.equals(this.status)) {
            if (this.onlinePlayers >= this.maxPlayers) {
                this.item.setType(ONLINE_FULL);
                this.queueing = true;
            } else {
                this.item.setType(ONLINE);
                this.queueing = false;
            }
            lore.add(ONLINE_TEXT);
            lore.add("");
            lore.add(PLAYER_TEXT + " §f" + this.onlinePlayers + " §8/ §f" + this.maxPlayers);
        } else if (Status.Server.OFFLINE.equals(this.status) || Status.Server.STARTING.equals(this.status)) {
            this.item.setAmount(1);
            this.item.setType(OFFLINE);
            lore.add(OFFLINE_TEXT);
            this.queueing = false;
        } else if (Status.Server.IN_GAME.equals(this.status) || Status.Server.PRE_GAME.equals(this.status) || Status.Server.POST_GAME.equals(this.status)) {
            this.item.setType(IN_GAME);
            lore.add(INGAME_TEXT);
            this.queueing = true;
        } else {
            this.item.setType(SERVICE);
            lore.add(SERVICE_TEXT);
            this.queueing = false;
        }

        lore.addAll(this.getPasswordLore());

        lore.addAll(this.getSpectatorLore());

        lore.addAll(this.getQueueLore());

        lore.addAll(this.getServerNameLore());

        this.item.setExLore(lore);
        this.updateItem();
    }

    public List<String> getPasswordLore() {
        if (this.hasPassword()) {
            return List.of("", PASSWORD_TEXT);
        }
        return List.of();
    }

    public List<String> getSpectatorLore() {
        if (this.status.equals(Status.Server.IN_GAME)) {
            return List.of("", SPECTATOR_JOIN_TEXT);
        }
        return List.of();
    }

    public List<String> getQueueLore() {
        if (this.queueing) {
            return List.of("", QUEUE_JOIN_TEXT);
        }
        return List.of();
    }

    public List<String> getServerNameLore() {
        return List.of("", SERVER_NAME_COLOR + this.serverName);
    }

    protected void updateItem() {
        hubGame.getInventory().setItemStack(this.item);
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent e) {
        LobbyUser user = (LobbyUser) e.getUser();
        ClickType clickType = e.getClickType();

        user.playSoundItemClicked();
        Sender sender = user.asSender(Plugin.LOBBY);
        if (this.queueing) {
            if (clickType.isRightClick()) {
                if (this.queue.contains(user)) {
                    this.queue.remove(user);
                    sender.sendPluginMessage(ChatColor.PERSONAL + "Left the queue of server " + ChatColor.VALUE + this.displayName);
                } else {
                    this.queue.add(user);
                    sender.sendPluginMessage(ChatColor.PERSONAL + "Joined the queue of server " + ChatColor.VALUE + this.displayName);
                }
                user.closeInventory();
                e.setCancelled(true);
                return;
            }
        }

        this.moveUserToServer(user);
        e.setCancelled(true);
    }

    @EventHandler
    public void onUserQuit(UserQuitEvent e) {
        this.queue.remove(e.getUser());
    }

    public void moveUserToServer(User user) {
        Sender sender = user.asSender(Plugin.LOBBY);

        if (this.hasPassword()) {
            user.closeInventory();
            sender.sendPluginMessage(ChatColor.PERSONAL + "Enter the server-password:");
            Server.getUserEventManager().addUserChatCommand(user, new ServerPasswordCmd(this));
            return;
        }

        if (this.status.equals(Status.Server.ONLINE)) {
            if (this.isFull()) {
                sender.sendPluginMessage(FULL_MESSAGE);
            } else {
                user.setTask(this.getTask());
                user.switchToServer(this.port);
            }
        } else if (this.status.equals(Status.Server.SERVICE)) {
            if (user.hasPermission("lobby.gamehub.join.service")) {
                user.setTask(this.getTask());
                user.switchToServer(this.port);
            } else {
                sender.sendPluginMessage(SERVICE_MESSAGE);
            }
        } else {
            sender.sendPluginMessage(INGAME_OFFLINE_MESSAGE);
        }
    }

    public void updateQueue() {
        if (this.status.equals(Status.Server.ONLINE)) {
            for (int i = this.onlinePlayers; i <= this.maxPlayers && !this.queue.isEmpty(); i++) {
                this.moveUserToServer(this.queue.poll());
            }
            for (User user : this.queue) {
                user.asSender(Plugin.LOBBY).sendPluginMessage(ChatColor.WARNING + "Server " + ChatColor.VALUE + this.displayName + ChatColor.PERSONAL + " is full. Right click on the server to" + " leave the queue.");
            }
        } else if (this.status.equals(Status.Server.OFFLINE)) {
            for (User user : this.queue) {
                user.sendPluginMessage(Plugin.LOBBY, ChatColor.WARNING + "Server " + ChatColor.VALUE + this.displayName + ChatColor.WARNING + " has gone offline. Removed from queue.");
            }
            this.queue.clear();
        }
    }

    public String getTask() {
        return this.task;
    }

    public boolean hasPassword() {
        return this.password != null;
    }

    public ExItemStack getItem() {
        return this.item;
    }

    public boolean isFull() {
        return this.onlinePlayers >= this.maxPlayers;
    }

    @ChannelHandler(type = {ListenerType.SERVER_PASSWORD, ListenerType.SERVER_STATUS, ListenerType.SERVER_MAX_PLAYERS, ListenerType.SERVER_ONLINE_PLAYERS}, filtered = true)
    public void onChannelMessage(ChannelServerMessage<?> msg) {

        System.out.println("call " + this.port);

        if (!msg.getPort().equals(this.port)) {
            return;
        }

        MessageType<?> type = msg.getMessageType();

        if (type.equals(MessageType.Server.PASSWORD)) {
            this.password = this.database.getPassword();
            this.updateItemDescription();
        } else if (type.equals(MessageType.Server.STATUS)) {
            this.status = this.database.getStatus();
            this.onlinePlayers = this.database.getOnlinePlayers();
            this.updateItemDescription();
            this.updateQueue();
        } else if (type.equals(MessageType.Server.MAX_PLAYERS) || type.equals(MessageType.Server.ONLINE_PLAYERS)) {
            this.onlinePlayers = this.database.getOnlinePlayers();
            if (this.onlinePlayers == null) {
                this.onlinePlayers = 0;
            }
            this.maxPlayers = this.database.getMaxPlayers();
            if (this.maxPlayers == null) {
                this.maxPlayers = 0;
            }
            this.updateItemAmount();
            this.updateItemDescription();
            this.updateQueue();
        }
    }
}
