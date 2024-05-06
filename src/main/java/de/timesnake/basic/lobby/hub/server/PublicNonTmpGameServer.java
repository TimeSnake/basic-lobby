/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub.server;

import de.timesnake.basic.lobby.hub.game.GameHub;
import de.timesnake.database.util.server.DbNonTmpGameServer;
import de.timesnake.library.game.NonTmpGameInfo;

import java.util.LinkedList;
import java.util.List;

public class PublicNonTmpGameServer extends NonTmpGameServer {

  public PublicNonTmpGameServer(GameHub<NonTmpGameInfo> hubGame, DbNonTmpGameServer server,
                                String displayName, int slot) {
    super(hubGame, server, displayName, slot);
  }

  @Override
  public List<String> getPasswordLore() {
    List<String> lore = new LinkedList<>(super.getPasswordLore());
    lore.addAll(List.of("", "§7public server"));
    return lore;
  }
}
