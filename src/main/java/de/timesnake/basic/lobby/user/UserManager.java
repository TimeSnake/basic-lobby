package de.timesnake.basic.lobby.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDamageEvent;
import de.timesnake.basic.lobby.main.BasicLobby;
import de.timesnake.basic.lobby.server.LobbyServer;
import de.timesnake.library.waitinggames.WaitingGameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
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
    public void onPlayerDeath(PlayerDeathEvent e) {
        e.deathMessage(Component.text(""));
        Player p = e.getEntity();
        p.spigot().respawn();
        ((LobbyUser) Server.getUser(p)).setLobbyInventory();

    }

    @EventHandler
    public void onPlayerClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            if (!Server.getUser(((Player) e.getWhoClicked())).isService()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onUserDamage(UserDamageEvent e) {
        if (!e.getDamageCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) && !e.getDamageCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
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
}
