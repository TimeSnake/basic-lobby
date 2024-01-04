/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.user;

import de.timesnake.basic.bukkit.core.main.BasicBukkit;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.basic.lobby.server.LobbyServer;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.user.DbPunishment;
import de.timesnake.library.basic.util.PunishType;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.chat.Chat;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.time.Duration;

public class LobbyUser extends User {

  private Duration jailDuration = null;

  public LobbyUser(Player p) {
    super(p);
    this.setTask(null);
    this.getDatabase().setTeam(null);
    this.getDatabase().setKit(null);

    DbPunishment punishment = Database.getUsers().getUser(this.getUniqueId()).getPunishment();

    if (punishment.getType() == PunishType.JAIL) {
      this.jailDuration = punishment.getDuration();

      if (this.jailDuration.toSeconds() == 0) {
        punishment.delete();
        this.jailDuration = null;
      }
    }
  }

  public Duration getJailDuration() {
    return jailDuration;
  }

  public boolean isJailed() {
    return jailDuration != null;
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
      this.sendPluginTDMessage(Plugin.LOBBY, "§sSwitched to buildmode!");
    } else {
      this.sendPluginTDMessage(Plugin.LOBBY, "§sSwitched to lobbymode!");
      this.setService(false);
      this.joinLobby(false);
    }

  }

  public void setLobbyInventory() {
    this.setItem(LobbyInventory.RULES.cloneWithId());
    this.setItem(LobbyInventory.TELEPORTER.cloneWithId());
    this.setItem(LobbyInventory.SPEED.cloneWithId());
    this.setItem(LobbyInventory.GAMES_HUB.cloneWithId());
    this.setItem(LobbyInventory.SPAWN.cloneWithId());
    // TODO fix permission load
    Server.runTaskLaterSynchrony(() -> {
      if (this.hasPermission("lobby.build.inventory")) {
        this.setItem(LobbyInventory.BUILD_SERVER.cloneWithId());
      }
    }, 20 * 2, BasicBukkit.getPlugin());

  }

  public void teleportSpawn() {
    this.teleport(LobbyServer.getLobbyWorld().getSpawnLocation());
  }

  public void openGameHubInventory() {
    this.openInventory(LobbyServer.getGamesMenu().getInventory());
  }

  public void openBuildInventory() {
    this.openInventory(LobbyServer.getBuild().getInventory());
  }

  public void joinLobby(boolean teleport) {
    this.setStatus(Status.User.ONLINE);
    this.clearInventory();
    this.setExp(0.0F);
    this.setLevel(0);
    this.setWalkSpeed(0.2F);
    this.setFlySpeed(0.2F);
    this.setFlying(false);
    this.setGameMode(GameMode.ADVENTURE);
    this.setHealthScaled(false);
    this.setInvulnerable(false);
    this.setLobbyInventory();
    this.setLobbySideboard();
    this.setScoreboardCoins();
    if (teleport) {
      this.teleportSpawn();
    }
  }

  public void setLobbySideboard() {
    this.setSideboard(LobbyServer.getLobbySideboard());
  }

  @Override
  public boolean isMuted() {
    return super.isMuted() || this.isJailed();
  }
}
