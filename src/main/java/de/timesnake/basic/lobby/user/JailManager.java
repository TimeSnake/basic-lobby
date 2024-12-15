/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserQuitEvent;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.ExWorld.Restriction;
import de.timesnake.basic.lobby.main.BasicLobby;
import de.timesnake.database.util.user.DbPunishment;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.basic.util.UserMap;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent.Cause;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

public class JailManager implements Listener {

  private static final int CHECK_PERIOD_TICKS = 20 * 20;

  private final Logger logger = LogManager.getLogger("logger.jail-manager");

  private final ExWorld jailWorld;
  private final UserMap<LobbyUser, BukkitTask> taskByUser = new UserMap<>();
  private final UserMap<LobbyUser, Tuple<Location, Duration>> lastLocationByUser = new UserMap<>();

  public JailManager() {
    this.jailWorld = Server.getWorld("jail");

    this.jailWorld.restrict(Restriction.FOOD_CHANGE, true);
    this.jailWorld.restrict(Restriction.NO_PLAYER_DAMAGE, true);

    Server.registerListener(this, BasicLobby.getPlugin());
  }

  public void jailUser(LobbyUser user) {
    user.clearInventory();
    user.resetPlayerProperties();
    user.resetSideboard();
    user.teleport(this.jailWorld.getSpawnLocation());
    user.addPotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false);

    long s = user.getJailDuration().toSeconds();
    String time = String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));

    user.sendTDMessage("§wYou are jailed for " + time);

    this.runWatcherForUser(user);
  }

  private void runWatcherForUser(LobbyUser user) {
    this.lastLocationByUser.put(user, new Tuple<>(user.getLocation(), Duration.ZERO));
    this.taskByUser.put(user, Server.runTaskTimerAsynchrony(() -> {
      Tuple<Location, Duration> tuple = this.lastLocationByUser.get(user);
      if (tuple.getA().getBlockX() == user.getLocation().getBlockX()
          && tuple.getA().getBlockZ() == user.getLocation().getBlockZ()) {
        this.kickUser(user);
        this.taskByUser.remove(user).cancel();
        return;
      }

      this.lastLocationByUser.put(user, new Tuple<>(user.getLocation(),
          tuple.getB().plusSeconds(CHECK_PERIOD_TICKS / 20)));
      this.checkDuration(user, false);
    }, CHECK_PERIOD_TICKS, CHECK_PERIOD_TICKS, BasicLobby.getPlugin()));
  }

  public void kickUser(LobbyUser user) {
    Server.runTaskSynchrony(() ->
            user.kick(Component.text("You were kicked for being afk"), Cause.IDLING),
        BasicLobby.getPlugin());
  }

  @EventHandler
  public void onUserQuit(UserQuitEvent e) {
    LobbyUser user = ((LobbyUser) e.getUser());

    BukkitTask task = this.taskByUser.remove(user);

    if (task != null) {
      task.cancel();
    }

    if (this.lastLocationByUser.containsKey(user)) {
      this.checkDuration(user, true);
    }
  }

  public void checkDuration(LobbyUser user, boolean update) {
    Duration duration = this.lastLocationByUser.get(user).getB();

    if (duration == null) {
      return;
    }

    DbPunishment punishment = user.getDatabase().getPunishment();
    Duration neededDuration = punishment.getDuration();

    if (neededDuration.toSeconds() == 0) {
      return;
    }

    if (duration.compareTo(neededDuration) > 0) {
      user.getDatabase().getPunishment().delete();
      user.sendTDMessage("§wRejoin to leave the jail!");
    } else if (update) {
      Duration leftDuration = neededDuration.minusSeconds(duration.toSeconds());
      punishment.setDuration(leftDuration);
      this.logger.info("Updated jail duration of {} to {}s", user.getName(), leftDuration.toSeconds());
    }
  }

}
