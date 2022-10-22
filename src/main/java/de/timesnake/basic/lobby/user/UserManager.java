/*
 * basic-lobby.main
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

package de.timesnake.basic.lobby.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDamageEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDeathEvent;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.basic.lobby.main.BasicLobby;
import de.timesnake.basic.lobby.server.LobbyServer;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.waitinggames.WaitingGameManager;
import net.kyori.adventure.text.Component;
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
        if (!e.getDamageCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)
                && !e.getDamageCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onUserDamageByUser(UserDamageByUserEvent e) {
        WaitingGameManager waitingGameManager = LobbyServer.getWaitingGameManager();

        boolean gameManaged = waitingGameManager.onUserDamage(e);

        if (!gameManaged) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryInteract(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.PHYSICAL) && e.getClickedBlock() != null
                && e.getClickedBlock().getType().equals(Material.TURTLE_EGG)) {
            User user = Server.getUser(e.getPlayer());
            user.removeCoins(10, true);
            Server.broadcastMessage(Plugin.LOBBY, user.getChatNameComponent()
                    .append(Component.text(" trampled on turtle eggs!", ExTextColor.WARNING)));
        }
    }
}
