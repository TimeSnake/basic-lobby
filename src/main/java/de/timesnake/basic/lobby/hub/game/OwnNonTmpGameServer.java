package de.timesnake.basic.lobby.hub.game;

import de.timesnake.database.util.server.DbNonTmpGameServer;
import de.timesnake.library.game.NonTmpGameInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class OwnNonTmpGameServer extends NonTmpGameServer {

    private final UUID owner;
    private final String ownerName;

    public OwnNonTmpGameServer(GameHub<NonTmpGameInfo> hubGame, DbNonTmpGameServer server, String displayName, int slot, UUID owner, String ownerName) {
        super(hubGame, server, displayName, slot);
        this.owner = owner;
        this.ownerName = ownerName;
    }

    @Override
    public List<String> getPasswordLore() {
        List<String> lore = new LinkedList<>(super.getPasswordLore());
        lore.addAll(List.of("", "§fOwner: §7" + this.ownerName));
        return lore;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return ownerName;
    }
}
