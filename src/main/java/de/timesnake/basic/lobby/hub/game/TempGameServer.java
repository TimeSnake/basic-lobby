package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.lobby.user.LobbyUser;
import de.timesnake.database.util.object.Type;
import de.timesnake.database.util.server.DbLoungeServer;
import de.timesnake.database.util.server.DbTempGameServer;
import de.timesnake.library.basic.util.Status;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

public class TempGameServer extends TaskServer {

    protected final DbTempGameServer tempGameServer;
    protected final String tempGameServerName;

    protected final boolean kitsEnabled;
    protected final boolean mapsEnabled; // only true if maps are allowed
    protected final boolean oldPvP;
    protected final Integer maxPlayersPerTeam;
    protected final Integer teamAmount;

    public TempGameServer(Integer serverNumber, HubTempGame hubGame, DbLoungeServer server, int slot) {

        super(serverNumber, hubGame, server, slot);

        this.tempGameServer = server.getTwinServer();
        this.tempGameServerName = this.tempGameServer.getName();

        if (hubGame.getKitAvailability().equals(Type.Availability.ALLOWED)) {
            this.kitsEnabled = this.tempGameServer.areKitsEnabled();
        } else {
            this.kitsEnabled = false;
        }
        if (hubGame.getMapAvailability().equals(Type.Availability.ALLOWED)) {
            this.mapsEnabled = this.tempGameServer.areMapsEnabled();
        } else {
            this.mapsEnabled = false;
        }

        this.oldPvP = this.tempGameServer.isOldPvP();

        this.maxPlayersPerTeam = this.tempGameServer.getMaxPlayersPerTeam();

        Integer teamAmount = this.tempGameServer.getTeamAmount();
        if (hubGame.getTeamSizes().size() > 1) {
            this.teamAmount = teamAmount;
        } else {
            this.teamAmount = null;
        }
    }

    @Override
    protected void initUpdate() {
        super.serverName = ((DbLoungeServer) super.getDatabase()).getTwinServer().getName() + " " + this.name;
        super.initUpdate();
    }

    @Override
    protected void updateItemDescription() {
        if (status.equals(Status.Server.OFFLINE)) {
            super.hubGame.removeServer(super.getName());
            Server.getChannel().removeListener(this);
            Server.getInventoryEventManager().removeClickListener(this);
        } else {
            super.updateItemDescription();
        }
    }

    @Override
    public List<String> getPasswordLore() {
        List<String> lore = new ArrayList<>();
        lore.addAll(this.getKitLore());
        lore.addAll(this.getMapLore());
        lore.addAll(this.getPvPLore());
        lore.addAll(this.getPlayersPerTeamLore());
        lore.addAll(this.getTeamAmount());
        lore.addAll(super.getPasswordLore());
        return lore;
    }

    public List<String> getKitLore() {
        return this.kitsEnabled ? List.of("", KIT_TEXT) : List.of();
    }

    public List<String> getMapLore() {
        return this.mapsEnabled ? List.of("", MAP_TEXT) : List.of();
    }

    public List<String> getPvPLore() {
        return this.oldPvP ? List.of("", OLD_PVP_TEXT) : List.of();
    }

    public List<String> getPlayersPerTeamLore() {
        return this.maxPlayersPerTeam != null ? List.of("", PLAYERS_PER_TEAM_TEXT + this.maxPlayersPerTeam) : List.of();
    }

    public List<String> getTeamAmount() {
        return this.teamAmount != null && this.teamAmount > 1 ? List.of(TEAM_AMOUNT + this.teamAmount) : List.of();
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent e) {
        LobbyUser user = (LobbyUser) e.getUser();
        ClickType clickType = e.getClickType();
        if (this.status.equals(Status.Server.IN_GAME)) {
            if (clickType.isLeftClick()) {
                user.setTask(this.getTask());
                user.setStatus(Status.User.SPECTATOR);
                user.switchToServer(((DbLoungeServer) this.database).getTwinServer().getPort());
                e.setCancelled(true);
            }
        }
        super.onUserInventoryClick(e);
    }
}
