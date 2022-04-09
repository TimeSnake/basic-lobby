package de.timesnake.basic.lobby.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.User;
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
import de.timesnake.library.waitinggames.WaitingGameManager;
import net.md_5.bungee.api.chat.ClickEvent;
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

    @Override
    public LobbyUser loadUser(Player player) {
        return new LobbyUser(player);
    }

    private LobbyInventory lobbyInventory;
    private Build build;
    private GamesMenu gamesMenu;

    private Sideboard sideboard;

    private ExWorld lobbyWorld;

    private UserManager userManager;

    private WaitingGameManager waitingGameManager;

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


        this.lobbyWorld.allowEntityExplode(false);
        this.lobbyWorld.allowPlayerDamage(true);
        this.lobbyWorld.allowFoodChange(false);
        this.lobbyWorld.allowBlockBurnUp(false);
        this.lobbyWorld.allowEntityBlockBreak(false);
        this.lobbyWorld.allowDropPickItem(false);
        this.lobbyWorld.allowBlockBreak(false);
        this.lobbyWorld.allowPlaceInBlock(false);
        this.lobbyWorld.allowFlintAndSteel(false);
        this.lobbyWorld.allowLightUpInteraction(false);
        this.lobbyWorld.setExceptService(true);
        this.lobbyWorld.setPVP(false);
        this.lobbyWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        this.lobbyWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

        Thread chatInfoThread = new Thread(new ChatInfoRepeater());
        chatInfoThread.start();

        this.sideboard = Server.getScoreboardManager().registerNewSideboard("lobby", "§6§lLobby");
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
            case 0:
                Server.broadcastMessage(Plugin.INFO, ChatColor.PUBLIC + "Do you need help? Use " + ChatColor.VALUE + "/support");
                break;
            case 1:
                Server.broadcastClickableMessage(Plugin.INFO, "Want to support the server? Donate via §nPatreon", Server.PATREON_LINK, "Click to open the link", ClickEvent.Action.OPEN_URL);
                break;
            case 2:
                Server.broadcastClickableMessage(Plugin.INFO, "Join our §ndiscord§r and meet our community",
                        Server.DISCORD_LINK, "Click to open the link", ClickEvent.Action.OPEN_URL);
                break;
            case 3:
                Server.broadcastClickableMessage(Plugin.INFO, "Visit our §nwebsite§r to find out more about the " +
                        "server", Server.WEBSITE_LINK, "Click to open the link", ClickEvent.Action.OPEN_URL);
                break;
            case 4:
                Server.broadcastMessage(Plugin.INFO, ChatColor.PUBLIC + "Invite new members to gain" + ChatColor.GOLD + " 100 TimeCoins" + ChatColor.VALUE + " (if the new player reached 100 TimeCoins)");
                break;
            case 5:
                Server.broadcastMessage(Plugin.INFO, ChatColor.WARNING + "Trampling on turtle eggs is forbidden!");
                break;
        }
        Server.broadcastNote(Instrument.PLING, Note.natural(1, Note.Tone.C));
    }

    public void msgOnlineTimeAll() {
        for (User user : Server.getUsers()) {
            user.sendPluginMessage(Plugin.LOBBY, ChatColor.PUBLIC + "Der Server ist immer samstags ab 19:30 " + "online.");
        }
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
            user.sendPluginMessage(Plugin.LOBBY, ChatColor.PERSONAL + "Switched to lobby-mode!");
            user.sendPluginMessage(Plugin.LOBBY, ChatColor.PERSONAL + "Use " + ChatColor.VALUE + "/build " + ChatColor.PERSONAL + " to switch mode");
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
            Server.runTaskTimerSynchrony(LobbyServerManager.this::broadcastInfoMessage, 0, 20 * 60 * 3, BasicLobby.getPlugin());
        }
    }
}
