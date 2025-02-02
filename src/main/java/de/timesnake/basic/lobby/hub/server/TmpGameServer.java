/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.lobby.hub.game.TmpGameHub;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.server.DbLoungeServer;
import de.timesnake.database.util.server.DbTmpGameServer;
import de.timesnake.library.basic.util.Availability;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.game.TmpGameInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TmpGameServer extends GameServer<TmpGameInfo> {

  private final String loungeServerName;
  private final String gameServerName;

  protected final boolean kitsEnabled;
  protected final boolean mapsEnabled;
  protected final boolean oldPvP;
  protected final Integer maxPlayersPerTeam;
  protected final Integer teamAmount;

  public TmpGameServer(Integer serverNumber, TmpGameHub gameHub, DbLoungeServer server, int slot) {
    super(gameHub.getGameInfo().getDisplayName() + " " + serverNumber, gameHub, server, slot, true);

    this.loungeServerName = server.getName();

    DbTmpGameServer gameServer = server.getTwinServer();
    this.gameServerName = gameServer.getName();

    this.kitsEnabled = gameServer.areKitsEnabled();
    this.mapsEnabled = gameServer.areMapsEnabled();

    this.oldPvP = gameServer.isOldPvP();

    this.maxPlayersPerTeam = gameServer.getMaxPlayersPerTeam();

    Integer teamAmount = gameServer.getTeamAmount();
    if (gameHub.getGameInfo().getTeamAmounts().size() > 1) {
      this.teamAmount = teamAmount;
    } else {
      this.teamAmount = null;
    }

    Server.getChannel().addListener(this, Set.of(this.getServerName(), gameServerName));
  }

  @Override
  public List<String> getPasswordLore() {
    List<String> lore = new ArrayList<>();
    lore.addAll(this.getKitLore());
    lore.addAll(this.getMapLore());
    lore.addAll(this.getPvPLore());
    lore.addAll(this.getTeamLore());
    lore.addAll(super.getPasswordLore());
    return lore;
  }

  public List<String> getKitLore() {
    if (this.kitsEnabled && this.gameHub.getGameInfo().getKitAvailability().equals(Availability.ALLOWED)) {
      return List.of("", KIT_TEXT);
    }
    return List.of();
  }

  public List<String> getMapLore() {
    if (this.mapsEnabled && this.gameHub.getGameInfo().getMapAvailability().equals(Availability.ALLOWED)) {
      return List.of("", MAP_TEXT);
    }
    return List.of();
  }

  public List<String> getPvPLore() {
    return this.oldPvP ? List.of("", OLD_PVP_TEXT) : List.of();
  }

  public List<String> getTeamLore() {
    if (this.maxPlayersPerTeam == null || this.teamAmount == null || this.teamAmount <= 1) {
      return List.of();
    }

    return List.of(TEAM_AMOUNT + String.join(" vs ",
        Collections.nCopies(this.teamAmount, String.valueOf(this.maxPlayersPerTeam))));
  }

  @Override
  protected void tryMoveUserToGameStateServer(User user) {
    if (this.status.equals(Status.Server.IN_GAME)) {
      user.setTask(this.getTask());
      user.setStatus(Status.User.SPECTATOR);
      user.switchToServer(gameServerName);
    }

    super.tryMoveUserToGameStateServer(user);
  }

  @ChannelHandler(type = {ListenerType.SERVER_PASSWORD, ListenerType.SERVER_STATUS,
      ListenerType.SERVER_MAX_PLAYERS, ListenerType.SERVER_ONLINE_PLAYERS}, filtered = true)
  public void onChannelMessage(ChannelServerMessage<?> msg) {
    MessageType<?> type = msg.getMessageType();
    if (this.status.isGameState() && msg.getIdentifier().equals(this.gameServerName)) {
      if (type.equals(MessageType.Server.STATUS)) {
        this.status = ((Status.Server) msg.getValue());
        this.update();
      } else if (type.equals(MessageType.Server.MAX_PLAYERS)) {
        this.maxPlayers = (Integer) msg.getValue();
        if (this.maxPlayers == null) {
          this.maxPlayers = 0;
        }
        this.update();
      } else if (type.equals(MessageType.Server.ONLINE_PLAYERS)) {
        this.onlinePlayers = (Integer) msg.getValue();
        if (this.onlinePlayers == null) {
          this.onlinePlayers = 0;
        }
        this.update();
      }
    } else if (msg.getIdentifier().equals(this.loungeServerName)) {
      if (type.equals(MessageType.Server.PASSWORD)) {
        this.password = (String) msg.getValue();
        this.updateItemDescription();
        this.updateItem();
      } else if (type.equals(MessageType.Server.STATUS)) {
        this.status = (Status.Server) msg.getValue();
        this.update();
      } else if (type.equals(MessageType.Server.MAX_PLAYERS)) {
        this.maxPlayers = (Integer) msg.getValue();
        if (this.maxPlayers == null) {
          this.maxPlayers = 0;
        }
        this.update();
      } else if (type.equals(MessageType.Server.ONLINE_PLAYERS)) {
        this.onlinePlayers = (Integer) msg.getValue();
        if (this.onlinePlayers == null) {
          this.onlinePlayers = 0;
        }
        this.update();
      }
    }
  }

  @Override
  public String getServerName() {
    return this.status.equals(Status.Server.IN_GAME) ? this.gameServerName : this.loungeServerName;
  }
}
