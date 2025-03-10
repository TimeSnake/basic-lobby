/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserQuitEvent;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.lobby.hub.ServerPasswordCmd;
import de.timesnake.basic.lobby.hub.game.GameHub;
import de.timesnake.basic.lobby.main.BasicLobby;
import de.timesnake.basic.lobby.server.LobbyServer;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.database.util.server.DbTaskServer;
import de.timesnake.library.basic.util.Status;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class GameServer<GameInfo extends de.timesnake.library.game.GameInfo>
    implements ChannelListener, Listener, GameServerBasis {

  protected final DbTaskServer database;

  protected final String displayName;

  protected final String task;
  protected Status.Server status;
  protected Integer onlinePlayers;
  protected Integer maxPlayers;
  protected String password;

  protected ExItemStack item;
  protected final GameHub<GameInfo> gameHub;

  protected boolean queueing;
  protected final Queue<User> queue = new LinkedList<>();

  private final int slot;

  public GameServer(String displayName, GameHub<GameInfo> gameHub, DbTaskServer server, int slot, boolean updateItem) {
    this.database = server;
    this.displayName = displayName;

    this.task = server.getTask();
    this.status = server.getStatus();
    this.onlinePlayers = server.getOnlinePlayers();
    this.maxPlayers = server.getMaxPlayers();
    this.password = server.getPassword();

    this.slot = slot;
    this.gameHub = gameHub;

    this.item = new ExItemStack(Material.WHITE_WOOL)
        .setDisplayName("§6" + this.displayName)
        .setSlot(slot)
        .onClick(e -> this.onClick(e.getUser(), e.getClickType()), true);

    this.updateItemAmount();
    this.updateItemDescription();
    if (updateItem) {
      this.updateItem();
    }
  }

  protected void update() {
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
      this.status = Status.Server.OFFLINE;
    }

    if (Status.Server.ONLINE.equals(this.status)) {
      if (this.onlinePlayers >= this.maxPlayers) {
        this.item = this.item.withType(ONLINE_FULL);
        this.queueing = true;
      } else {
        this.item = this.item.withType(ONLINE);
        this.queueing = false;
      }
      lore.add(ONLINE_TEXT);
      lore.add("");
      lore.add(PLAYER_TEXT + " §f" + this.onlinePlayers + " §8/ §f" + this.maxPlayers);
    } else if (!this.status.isRunning()) {
      this.item.setAmount(1);
      this.item = this.item.withType(OFFLINE);
      lore.add(OFFLINE_TEXT);
      this.queueing = false;
    } else if (Status.Server.LAUNCHING.equals(this.status) || Status.Server.LOADING.equals(this.status)) {
      this.item.setAmount(1);
      this.item = this.item.withType(STARTING);
      lore.add(STARTING_TEXT);
      this.queueing = true;
    } else if (this.status.isGameState()) {
      this.item = this.item.withType(IN_GAME);

      if (this.status.equals(Status.Server.IN_GAME)) {
        lore.add(IN_GAME_TEXT);
      } else if (this.status.equals(Status.Server.PRE_GAME)) {
        lore.add(PRE_GAME_TEXT);
      } else if (this.status.equals(Status.Server.POST_GAME)) {
        lore.add(POST_GAME_TEXT);
      }
      this.queueing = true;
    } else {
      this.item = this.item.withType(SERVICE);
      lore.add(SERVICE_TEXT);
      this.queueing = false;
    }

    lore.addAll(this.getPasswordLore());

    lore.addAll(this.getSpectatorLore());

    lore.addAll(this.getQueueLore());

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

  protected void updateItem() {
    gameHub.updateServer(this);
  }

  protected void onClick(User user, ClickType type) {
    user.playSoundItemClickSuccessful();

    if (this.queueing) {
      if (type.isRightClick()) {
        if (this.queue.contains(user)) {
          this.queue.remove(user);
          user.sendPluginTDMessage(LobbyServer.PLUGIN, "§sLeft queue of server §v" + this.displayName);
        } else {
          this.queue.add(user);
          user.sendPluginTDMessage(LobbyServer.PLUGIN, "§Joined queue of server §v" + this.displayName);
        }
        user.closeInventory();
        return;
      }
    }

    this.tryMoveUserToServer(user);
  }

  @EventHandler
  public void onUserQuit(UserQuitEvent e) {
    this.queue.remove(e.getUser());
  }

  public void tryMoveUserToServer(User user) {
    if (this.hasPassword()) {
      user.closeInventory();
      user.sendPluginTDMessage(LobbyServer.PLUGIN, "§sEnter server-password:");
      Server.getUserEventManager().addUserChatCommand(user, new ServerPasswordCmd(this.getServerName(), this.password));
      return;
    }

    if (this.status.equals(Status.Server.ONLINE)) {
      this.tryMoveUserToOnlineServer(user);
    } else if (this.status.equals(Status.Server.SERVICE)) {
      this.tryMoveUserToServiceServer(user);
    } else if (this.status.isGameState()) {
      this.tryMoveUserToGameStateServer(user);
    }
  }

  protected void tryMoveUserToOnlineServer(User user) {
    if (this.isFull()) {
      user.sendPluginTDMessage(LobbyServer.PLUGIN, FULL_MESSAGE);
    } else {
      user.setTask(this.getTask());
      user.switchToServer(this.getServerName());
    }
  }

  protected void tryMoveUserToServiceServer(User user) {
    if (user.hasPermission("lobby.gamehub.join.service")) {
      user.setTask(this.getTask());
      user.switchToServer(this.getServerName());
    } else {
      user.sendPluginTDMessage(LobbyServer.PLUGIN, SERVICE_MESSAGE);
    }
  }

  protected void tryMoveUserToGameStateServer(User user) {
    user.sendPluginTDMessage(LobbyServer.PLUGIN, IN_GAME_OFFLINE_MESSAGE);
  }

  public void updateQueue() {
    if (this.status.equals(Status.Server.ONLINE)) {
      Server.runTaskLaterSynchrony(() -> {
        for (int i = this.onlinePlayers; i <= this.maxPlayers && !this.queue.isEmpty(); i++) {
          this.tryMoveUserToServer(this.queue.poll());
        }
        for (User user : this.queue) {
          user.sendPluginTDMessage(LobbyServer.PLUGIN, "§wServer §v" + this.displayName + "§w is full. " +
                                                       "Right click on server to leave queue.");
        }
      }, 2 * 20, BasicLobby.getPlugin());

    } else if (this.status.equals(Status.Server.OFFLINE)) {
      for (User user : this.queue) {
        user.sendPluginTDMessage(LobbyServer.PLUGIN, "§wServer §v" + this.displayName + " has gone offline. " +
                                                     "Removed from queue.");
      }
      this.queue.clear();
    }
  }

  public DbTaskServer getDatabase() {
    return database;
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

  public String getDisplayName() {
    return displayName;
  }

  public int getSlot() {
    return slot;
  }

  public Status.Server getStatus() {
    return status;
  }

  @Override
  public void destroy() {
    Server.getChannel().removeListener(this);
    UserQuitEvent.getHandlerList().unregister(this);
    this.item.clearListeners();
  }

}
