/*
 * basic-lobby.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.server.ServerInfo;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.basic.bukkit.util.user.event.UserQuitEvent;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.basic.lobby.hub.ServerPasswordCmd;
import de.timesnake.basic.lobby.main.BasicLobby;
import de.timesnake.basic.lobby.user.LobbyUser;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.server.DbTaskServer;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public class GameServer<GameInfo extends de.timesnake.library.game.GameInfo> extends ServerInfo
        implements ChannelListener, UserInventoryClickListener, Listener, GameServerBasis {

    protected final String displayName;
    protected final String task;
    protected final ExItemStack item;
    protected final GameHub<GameInfo> gameHub;
    protected final Queue<User> queue = new LinkedList<>();
    private final int slot;
    protected String serverName;
    protected boolean queueing;

    public GameServer(String displayName, GameHub<GameInfo> gameHub, DbTaskServer server, int slot, boolean updateItem) {
        super(server);
        this.displayName = displayName;
        this.serverName = server.getName();
        this.slot = slot;
        this.task = server.getTask();
        this.gameHub = gameHub;

        this.item = new ExItemStack(slot, Material.WHITE_WOOL, SERVER_TITLE_COLOR + this.displayName);

        this.updateItemAmount();
        this.updateItemDescription();
        if (updateItem) {
            this.updateItem();
        }

        Server.getInventoryEventManager().addClickListener(this, this.item);

        Server.getChannel().addListener(this, () -> Collections.singleton(this.name));
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSlot() {
        return slot;
    }

    protected void initUpdate() {
        this.updateItemAmount();
        this.updateItemDescription();
        this.updateQueue();
        this.updateItem();
    }

    protected void updateItemAmount() {
        if (this.onlinePlayers != null && this.onlinePlayers != 0) {
            this.item.setAmount(this.onlinePlayers);
        } else {
            this.item.setAmount(1);
        }
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
        } else if (Status.Server.OFFLINE.equals(this.status) || Status.Server.LAUNCHING.equals(this.status)) {
            this.item.setAmount(1);
            this.item.setType(OFFLINE);
            lore.add(OFFLINE_TEXT);
            this.queueing = false;
        } else if (Status.Server.LOADING.equals(this.status)) {
            this.item.setAmount(1);
            this.item.setType(STARTING);
            lore.add(STARTING_TEXT);
            this.queueing = true;
        } else if (Status.Server.IN_GAME.equals(this.status) || Status.Server.PRE_GAME.equals(this.status)
                || Status.Server.POST_GAME.equals(this.status)) {
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
        gameHub.updateServer(this);
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
                    sender.sendPluginMessage(Component.text("Left the queue of server ", ExTextColor.PERSONAL)
                            .append(Component.text(this.displayName, ExTextColor.VALUE)));
                } else {
                    this.queue.add(user);
                    sender.sendPluginMessage(Component.text("Joined the queue of server ", ExTextColor.PERSONAL)
                            .append(Component.text(this.displayName, ExTextColor.VALUE)));
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
            sender.sendPluginMessage(Component.text("Enter the server-password:", ExTextColor.PERSONAL));
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
            Server.runTaskLaterSynchrony(() -> {
                for (int i = this.onlinePlayers; i <= this.maxPlayers && !this.queue.isEmpty(); i++) {
                    this.moveUserToServer(this.queue.poll());
                }
                for (User user : this.queue) {
                    user.asSender(Plugin.LOBBY).sendPluginMessage(Component.text("Server ", ExTextColor.WARNING)
                            .append(Component.text(this.displayName, ExTextColor.VALUE))
                            .append(Component.text(" is full. Right click on the server to leave the queue.", ExTextColor.WARNING)));
                }
            }, 2 * 20, BasicLobby.getPlugin());

        } else if (this.status.equals(Status.Server.OFFLINE)) {
            for (User user : this.queue) {
                user.sendPluginMessage(Plugin.LOBBY, Component.text("Server ", ExTextColor.WARNING)
                        .append(Component.text(this.displayName, ExTextColor.VALUE))
                        .append(Component.text(" has gone offline. Removed from queue.", ExTextColor.WARNING)));
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

    @Override
    public ExItemStack getItem() {
        return this.item;
    }

    public boolean isFull() {
        return this.onlinePlayers >= this.maxPlayers;
    }

    @Override
    public void destroy() {
        Server.getChannel().removeListener(this);
        UserQuitEvent.getHandlerList().unregister(this);
    }

    @ChannelHandler(type = {ListenerType.SERVER_PASSWORD, ListenerType.SERVER_STATUS, ListenerType.SERVER_MAX_PLAYERS
            , ListenerType.SERVER_ONLINE_PLAYERS}, filtered = true)
    public void onChannelMessage(ChannelServerMessage<?> msg) {

        if (!msg.getName().equals(this.name)) {
            return;
        }

        MessageType<?> type = msg.getMessageType();

        if (type.equals(MessageType.Server.PASSWORD)) {
            this.password = this.database.getPassword();
            this.updateItemDescription();
            this.updateItem();
        } else if (type.equals(MessageType.Server.STATUS)) {
            this.status = this.database.getStatus();
            this.onlinePlayers = this.database.getOnlinePlayers();
            this.initUpdate();
        } else if (type.equals(MessageType.Server.MAX_PLAYERS) || type.equals(MessageType.Server.ONLINE_PLAYERS)) {
            this.onlinePlayers = this.database.getOnlinePlayers();
            if (this.onlinePlayers == null) {
                this.onlinePlayers = 0;
            }
            this.maxPlayers = this.database.getMaxPlayers();
            if (this.maxPlayers == null) {
                this.maxPlayers = 0;
            }
            this.initUpdate();
        }
    }
}
