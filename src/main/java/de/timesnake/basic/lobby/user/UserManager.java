/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDamageEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDeathEvent;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.basic.lobby.main.BasicLobby;
import de.timesnake.basic.lobby.server.LobbyServer;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class UserManager implements Listener {

  public UserManager() {
    Server.registerListener(this, BasicLobby.getPlugin());
  }

  @EventHandler
  public void onDamage(PlayerItemDamageEvent e) {
    if (!Server.getUser(e.getPlayer()).isService()) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerPickUpArrow(PlayerPickupArrowEvent e) {
    if (!Server.getUser(e.getPlayer()).isService()) {
      e.setCancelled(true);
    }

  }

  @EventHandler
  public void onPlayerDeath(UserDeathEvent e) {
    e.setBroadcastDeathMessage(false);
    e.setAutoRespawn(true);
  }

  @EventHandler
  public void onUserDamage(UserDamageEvent e) {
    if (LobbyServer.getWaitingGameManager().onUserDamage(e)) {
      return;
    }

    if (!e.getDamageCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)
        && !e.getDamageCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onUserDamageByUser(UserDamageByUserEvent e) {
    if (LobbyServer.getWaitingGameManager().onUserDamageByUser(e)) {
      return;
    }

    e.setCancelled(true);
  }

  @EventHandler
  public void onInventoryInteract(PlayerInteractEvent e) {
    if (e.getAction().equals(Action.PHYSICAL)
        && e.getClickedBlock() != null
        && e.getClickedBlock().getType().equals(Material.TURTLE_EGG)) {
      User user = Server.getUser(e.getPlayer());
      user.removeCoins(10, true);
      Server.broadcastTDMessage(Plugin.LOBBY, user.getTDChatName() + "§w trampled on turtle eggs!");
    }
  }
}
