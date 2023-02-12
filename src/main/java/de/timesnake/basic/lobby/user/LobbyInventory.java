/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryInteractEvent;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryInteractListener;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.basic.lobby.main.BasicLobby;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class LobbyInventory implements Listener, UserInventoryInteractListener {

    public static final ExItemStack TELEPORTER = new ExItemStack(5, Material.ENDER_PEARL)
            .setDisplayName("§1Teleporter")
            .setLore("§fThrow to teleport")
            .setMoveable(false)
            .setDropable(false);
    public static final ExItemStack RULES = new ExItemStack(0, Material.WRITTEN_BOOK)
            .setDisplayName("§4§lRules")
            .setLore("§fClick to show the rules")
            .setMoveable(false)
            .setDropable(false);
    public static final ExItemStack SPEED = new ExItemStack(2, Material.FEATHER)
            .setDisplayName("§bSpeed")
            .setLore("§fHold in hand to get speed")
            .setMoveable(false)
            .setDropable(false);
    public static final ExItemStack GAMES_HUB = new ExItemStack(4, Material.NETHER_STAR)
            .setDisplayName("§6Games")
            .setLore("§fClick to open the game hub")
            .setMoveable(false)
            .setDropable(false);
    public static final ExItemStack SPAWN = new ExItemStack(6, Material.BEACON)
            .setDisplayName("§6Spawn")
            .setLore("§fClick to teleport to spawn")
            .setMoveable(false)
            .setDropable(false);
    public static final ExItemStack BUILD_SERVER = new ExItemStack(8, Material.STONE_PICKAXE)
            .setDisplayName("§3BUILD")
            .setLore("§fClick to switch mode")
            .setMoveable(false)
            .setDropable(false);

    public LobbyInventory() {
        Server.getInventoryEventManager()
                .addInteractListener(this, RULES, GAMES_HUB, SPAWN, BUILD_SERVER);
        Server.registerListener(this, BasicLobby.getPlugin());
    }

    @EventHandler
    public void onPlayerHeldItem(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        if (!Server.getUser(p).isService()) {
            if (p.getInventory().getItem(e.getNewSlot()) != null
                    && p.getInventory().getItem(e.getNewSlot()).getItemMeta() != null
                    && ExItemStack.getItem(p.getInventory().getItem(e.getNewSlot()), true)
                    .equals(SPEED)) {
                p.setWalkSpeed(0.8F);
                return;
            }

            p.setWalkSpeed(0.2F);
        }

    }

    @Override
    public void onUserInventoryInteract(UserInventoryInteractEvent e) {
        LobbyUser user = (LobbyUser) e.getUser();
        ExItemStack clickedItem = e.getClickedItem();
        if (SPAWN.equals(clickedItem)) {
            user.teleportSpawn();
            e.setCancelled(true);
        } else if (GAMES_HUB.equals(clickedItem)) {
            user.openGameHubInventory();
            e.setCancelled(true);
        } else if (BUILD_SERVER.equals(clickedItem)) {
            user.openBuildInventory();
            e.setCancelled(true);
        } else if (RULES.equals(clickedItem)) {
            user.sendPluginTDMessage(Plugin.LOBBY, "§sUse §v/rules");
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        User user = Server.getUser(e.getPlayer());

        if (user == null) {
            return;
        }

        if (user.isService()) {
            return;
        }

        if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            return;
        }

        if (e.isCancelled()) {
            return;
        }

        if (user.getInventory().getItem(TELEPORTER.getSlot()) != null) {
            return;
        }

        user.setItem(TELEPORTER);
    }
}
