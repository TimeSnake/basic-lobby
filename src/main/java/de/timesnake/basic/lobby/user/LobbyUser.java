package de.timesnake.basic.lobby.user;

import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.basic.lobby.server.LobbyServer;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.extension.util.chat.Chat;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class LobbyUser extends User {

    private boolean isStoryMode = false;
    private int storyPoint = 1;

    public LobbyUser(Player p) {
        super(p);
        this.setTask(null);
        this.getDatabase().setTeam(null);
        this.getDatabase().setKit(null);
    }

    public boolean isStoryMode() {
        return this.isStoryMode;
    }

    public void setStoryMode(boolean story) {
        this.isStoryMode = story;
    }

    public int getStoryPoint() {
        return this.storyPoint;
    }

    public void setStoryPoint(int storyPoint) {
        this.storyPoint = storyPoint;
    }

    @Override
    public void addCoins(float coins, boolean sendMessage) {
        super.addCoins(coins, sendMessage);
        this.setScoreboardCoins();
    }

    @Override
    public void setCoins(float coins, boolean sendMessage) {
        super.setCoins(coins, sendMessage);
        this.setScoreboardCoins();
    }

    @Override
    public void removeCoins(float coins, boolean sendMessage) {
        super.removeCoins(coins, sendMessage);
        this.setScoreboardCoins();
    }

    public void setScoreboardCoins() {
        this.setSideboardScore(3, String.valueOf(Chat.roundCoinAmount(super.getCoins())));
    }

    public void switchMode() {
        this.updatePermissions(false);
        this.getPlayer().getInventory().clear();
        if (!this.isService()) {
            this.setService(true);
            this.unlockInventory();
            this.sendPluginMessage(Plugin.LOBBY, ChatColor.PERSONAL + "Switched to buildmode!");
        } else {
            this.sendPluginMessage(Plugin.LOBBY, ChatColor.PERSONAL + "Switched to lobbymode!");
            this.joinLobby();
        }

    }

    public void setLobbyInventory() {
        this.setItem(LobbyInventory.RULES);
        this.setItem(LobbyInventory.STORY);
        this.setItem(LobbyInventory.TELEPORTER);
        this.setItem(LobbyInventory.SPEED);
        this.setItem(LobbyInventory.GAMES_HUB);
        this.setItem(LobbyInventory.SPAWN);
        if (this.hasPermission("lobby.build.inventory")) {
            this.setItem(LobbyInventory.BUILD_SERVER);
        }

    }

    public void teleportSpawn() {
        this.getPlayer().teleport(this.getPlayer().getWorld().getSpawnLocation());
    }

    public void openGameHubInventory() {
        this.openInventory(LobbyServer.getGamesMenu().getInventory());
    }

    public void openBuildInventory() {
        this.openInventory(LobbyServer.getBuild().getInventory());
    }

    public void joinLobby() {
        this.setStatus(Status.User.ONLINE);
        this.setService(false);
        this.clearInventory();
        this.getPlayer().setExp(0.0F);
        this.getPlayer().setLevel(0);
        this.getPlayer().setWalkSpeed(0.2F);
        this.getPlayer().setFlySpeed(0.2F);
        this.getPlayer().setFlying(false);
        this.setGameMode(GameMode.ADVENTURE);
        this.getPlayer().setHealthScaled(false);
        this.getPlayer().setInvulnerable(false);
        this.setLobbyInventory();
        if (this.getLocation().getWorld().equals(LobbyServer.getLobbyWorld().getBukkitWorld())) {
            this.teleportSpawn();
        }
        this.setLobbySideboard();
        this.setScoreboardCoins();

    }

    public void setLobbySideboard() {
        this.setSideboard(LobbyServer.getLobbySideboard());
    }
}
