package de.timesnake.basic.lobby.hub;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.basic.lobby.hub.game.HubGame;
import de.timesnake.basic.lobby.hub.game.HubTempGame;
import de.timesnake.basic.lobby.user.LobbyUser;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbTmpGame;

import java.util.HashMap;

public class GamesMenu implements UserInventoryClickListener {

    private final ExInventory inventory = Server.createExInventory(54, "Gamehub");
    private final HashMap<Integer, HubGame> games = new HashMap<>();

    public GamesMenu() {
        Server.getInventoryEventManager().addClickListener(this, HubGame.BACK);
        this.updateInventory();
    }

    public void updateInventory() {
        inventory.getInventory().clear();
        games.clear();

        for (DbGame<?> game : Database.getGames().getGames()) {
            HubGame hubGame;
            if (game instanceof DbTmpGame) {
                hubGame = new HubTempGame(((DbTmpGame) game));
            } else {
                hubGame = new HubGame(game);
            }
            if (hubGame == null) {
                Server.printError(Plugin.LOBBY, "Can not load game " + game.getInfo().getName());
            } else {
                inventory.setItemStack(hubGame.getSlot(), hubGame.getItem());
                games.put(hubGame.getSlot(), hubGame);
            }
        }
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent e) {
        LobbyUser user = (LobbyUser) e.getUser();
        user.openGameHubInventory();
        user.playSoundItemClicked();
        e.setCancelled(true);
    }


    public ExInventory getInventory() {
        return inventory;
    }

}