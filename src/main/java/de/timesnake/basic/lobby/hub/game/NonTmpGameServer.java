/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub.game;

import de.timesnake.database.util.server.DbNonTmpGameServer;
import de.timesnake.library.game.NonTmpGameInfo;

public class NonTmpGameServer extends GameServer<NonTmpGameInfo> {

    protected String gameInfo;

    public NonTmpGameServer(GameHub<NonTmpGameInfo> hubGame, DbNonTmpGameServer server, String displayName, int slot) {
        super(displayName, hubGame, server, slot, false);
        this.gameInfo = server.getGameInfo();
        this.updateItem();
    }

}
