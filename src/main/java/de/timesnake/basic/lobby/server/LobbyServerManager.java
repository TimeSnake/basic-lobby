/*
 * timesnake.basic-lobby.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.basic.lobby.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.user.event.UserJoinEvent;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.lobby.build.Build;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.basic.lobby.hub.GamesMenu;
import de.timesnake.basic.lobby.main.BasicLobby;
import de.timesnake.basic.lobby.user.LobbyInventory;
import de.timesnake.basic.lobby.user.LobbyUser;
import de.timesnake.basic.lobby.user.UserManager;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.waitinggames.WaitingGameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameRule;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Random;

public class LobbyServerManager extends ServerManager implements ChannelListener, Listener {

    public static LobbyServerManager getInstance() {
        return (LobbyServerManager) ServerManager.getInstance();
    }

    private LobbyInventory lobbyInventory;
    private Build build;
    private GamesMenu gamesMenu;
    private Sideboard sideboard;
    private ExWorld lobbyWorld;
    private UserManager userManager;
    private WaitingGameManager waitingGameManager;

    @Override
    public LobbyUser loadUser(Player player) {
        return new LobbyUser(player);
    }

    public void onLobbyEnable() {
        this.lobbyInventory = new LobbyInventory();
        this.build = new Build();
        this.gamesMenu = new GamesMenu();

        this.userManager = new UserManager();

        this.lobbyWorld = Server.getWorld("world");

        int spawnX = this.lobbyWorld.getSpawnLocation().getChunk().getX();
        int spawnZ = this.lobbyWorld.getSpawnLocation().getChunk().getZ();

        for (int x = -2; x < 2; x++) {
            for (int z = -2; z < 2; z++) {
                this.lobbyWorld.getChunkAt(spawnX + x, spawnZ + z).setForceLoaded(true);
            }
        }


        this.lobbyWorld.restrict(ExWorld.Restriction.ENTITY_EXPLODE, true);
        this.lobbyWorld.restrict(ExWorld.Restriction.PLAYER_DAMAGE, false);
        this.lobbyWorld.restrict(ExWorld.Restriction.FOOD_CHANGE, true);
        this.lobbyWorld.restrict(ExWorld.Restriction.BLOCK_BURN_UP, true);
        this.lobbyWorld.restrict(ExWorld.Restriction.ENTITY_BLOCK_BREAK, true);
        this.lobbyWorld.restrict(ExWorld.Restriction.DROP_PICK_ITEM, true);
        this.lobbyWorld.restrict(ExWorld.Restriction.BLOCK_BREAK, true);
        this.lobbyWorld.restrict(ExWorld.Restriction.PLACE_IN_BLOCK, true);
        this.lobbyWorld.restrict(ExWorld.Restriction.FLINT_AND_STEEL, true);
        this.lobbyWorld.restrict(ExWorld.Restriction.LIGHT_UP_INTERACTION, true);
        this.lobbyWorld.setExceptService(true);
        this.lobbyWorld.setPVP(true);
        this.lobbyWorld.setGameRule(GameRule.KEEP_INVENTORY, true);
        this.lobbyWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        this.lobbyWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

        Thread chatInfoThread = new Thread(new ChatInfoRepeater());
        chatInfoThread.start();

        this.sideboard = Server.getScoreboardManager().registerSideboard("lobby", "§6§lLobby");
        sideboard.setScore(7, "§1Online:");
        this.sideboard.setScore(6, "§6" + Server.getNetwork().getPlayerAmount());
        this.sideboard.setScore(5, "§r§f§f---------------§r");
        this.sideboard.setScore(4, "§1TimeCoins: ");
        //user time coins
        this.sideboard.setScore(2, "§f§f---------------§r");
        this.sideboard.setScore(1, "§8Server: ");
        this.sideboard.setScore(0, "§7" + Server.getName());

        this.waitingGameManager = new WaitingGameManager();

        Server.getWorldManager().getWorldBorderManager().setCustomBorders(false);
        Server.getWorldManager().getWorldBorderManager().allowEnderpearlThrouBorder(false);

        Server.registerListener(this.lobbyInventory, BasicLobby.getPlugin());

        new ServerUpdater();
    }

    public void broadcastInfoMessage() {
        switch (new Random().nextInt(6)) {
            case 0 -> Server.broadcastMessage(Plugin.INFO,
                    Component.text("Do you need help? Use ", ExTextColor.PUBLIC)
                            .append(Component.text("/support", ExTextColor.VALUE)));
            case 1 -> Server.broadcastClickableMessage(Plugin.INFO,
                    Component.text("Want to support the server? Donate via ", ExTextColor.PUBLIC)
                            .append(Component.text("Patreon", ExTextColor.PUBLIC, TextDecoration.UNDERLINED)),
                    Server.PATREON_LINK, Component.text("Click to open the link"), ClickEvent.Action.OPEN_URL);
            case 2 -> Server.broadcastClickableMessage(Plugin.INFO, Component.text("Join our ", ExTextColor.PUBLIC)
                            .append(Component.text("discord", ExTextColor.PUBLIC, TextDecoration.UNDERLINED))
                            .append(Component.text(" and meet our community", ExTextColor.PUBLIC)),
                    Server.DISCORD_LINK, Component.text("Click to open the link"), ClickEvent.Action.OPEN_URL);
            case 3 -> Server.broadcastClickableMessage(Plugin.INFO, Component.text("Visit our ", ExTextColor.PUBLIC)
                            .append(Component.text("website", ExTextColor.PUBLIC, TextDecoration.UNDERLINED))
                            .append(Component.text(" to find out more about the server", ExTextColor.PUBLIC)),
                    Server.WEBSITE_LINK, Component.text("Click to open the link", ExTextColor.PUBLIC),
                    ClickEvent.Action.OPEN_URL);
            case 4 ->
                    Server.broadcastMessage(Plugin.INFO, Component.text("Invite new members to gain", ExTextColor.PUBLIC)
                            .append(Component.text(" 100 TimeCoins", ExTextColor.GOLD))
                            .append(Component.text(" (if the new player reached 100 TimeCoins)", ExTextColor.QUICK_INFO)));
            case 5 ->
                    Server.broadcastMessage(Plugin.INFO, Component.text("Trampling on turtle eggs is forbidden!", ExTextColor.WARNING));
        }
        Server.broadcastNote(Instrument.PLING, Note.natural(1, Note.Tone.C));
    }

    public ExWorld getLobbyWorld() {
        return lobbyWorld;
    }

    public Build getBuild() {
        return build;
    }

    public GamesMenu getGamesMenu() {
        return gamesMenu;
    }

    public LobbyInventory getLobbyInventory() {
        return lobbyInventory;
    }

    public Sideboard getLobbySideboard() {
        return sideboard;
    }

    public WaitingGameManager getWaitingGameManager() {
        return waitingGameManager;
    }

    @EventHandler
    public void onUserJoin(UserJoinEvent e) {
        LobbyUser user = (LobbyUser) e.getUser();
        if (user.isService()) {
            user.sendPluginMessage(Plugin.LOBBY, Component.text("Switched to lobby-mode!", ExTextColor.PERSONAL));
            user.setStatus(Status.User.ONLINE);
        }

        if (!user.agreedDataProtection()) {
            user.setDefault();
        } else {
            user.joinLobby();
        }
    }

    private class ChatInfoRepeater implements Runnable {

        public void run() {
            Server.runTaskTimerSynchrony(LobbyServerManager.this::broadcastInfoMessage, 0, 20 * 60 * 3,
                    BasicLobby.getPlugin());
        }
    }
}
