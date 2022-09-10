package de.timesnake.basic.lobby.hub.game;

import de.timesnake.database.util.server.DbNonTmpGameServer;
import de.timesnake.library.game.NonTmpGameInfo;

import java.util.UUID;

public class NonTmpGameServer extends GameServer<NonTmpGameInfo> {

    private final UUID owner;
    protected String gameInfo;

    public NonTmpGameServer(NonTmpGameHub hubGame, DbNonTmpGameServer server, String displayName, int slot, UUID owner) {
        super(displayName, hubGame, server, slot, false);
        this.gameInfo = server.getGameInfo();
        this.owner = owner;
        this.updateItem();
    }

    @Override
    protected void updateItem() {
        if (this.owner == null) {
            super.updateItem();
        }
    }

    public UUID getOwner() {
        return owner;
    }
}
