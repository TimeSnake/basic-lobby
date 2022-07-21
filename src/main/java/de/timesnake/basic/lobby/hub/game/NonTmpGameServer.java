package de.timesnake.basic.lobby.hub.game;

import de.timesnake.database.util.server.DbGameServer;
import de.timesnake.library.game.NonTmpGameInfo;

import java.util.ArrayList;
import java.util.List;

public class NonTmpGameServer extends GameServer<NonTmpGameInfo> {

    protected String gameInfo;

    public NonTmpGameServer(Integer serverNumber, NonTmpGameHub hubGame, DbGameServer server, int slot) {
        super(serverNumber, hubGame, server, slot);
        this.gameInfo = server.getGameInfo();
    }

    @Override
    public List<String> getPasswordLore() {
        List<String> lore = new ArrayList<>();
        lore.addAll(this.getServerInfoLore());
        lore.addAll(super.getPasswordLore());
        return lore;
    }

    public List<String> getServerInfoLore() {
        if (this.serverName != null) {
            return List.of("", this.serverName);
        } else {
            return List.of();
        }
    }
}
