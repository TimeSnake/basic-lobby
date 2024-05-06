/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.lobby.hub.game.GameHub;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.server.DbNonTmpGameServer;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.game.NonTmpGameInfo;

import java.util.Set;

public class NonTmpGameServer extends GameServer<NonTmpGameInfo> {

  protected String serverName;
  protected String gameInfo;

  public NonTmpGameServer(GameHub<NonTmpGameInfo> hubGame, DbNonTmpGameServer server, String displayName, int slot) {
    super(displayName, hubGame, server, slot, false);
    this.serverName = server.getName();
    this.gameInfo = server.getGameInfo();
    this.updateItem();

    Server.getChannel().addListener(this, Set.of(this.serverName));
  }


  @ChannelHandler(type = {
      ListenerType.SERVER_PASSWORD,
      ListenerType.SERVER_STATUS,
      ListenerType.SERVER_MAX_PLAYERS,
      ListenerType.SERVER_ONLINE_PLAYERS
  }, filtered = true)
  public void onChannelMessage(ChannelServerMessage<?> msg) {
    MessageType<?> type = msg.getMessageType();

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

  @Override
  public String getServerName() {
    return this.serverName;
  }
}
