/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.basic.lobby.main.BasicLobby;
import de.timesnake.library.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.Action;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class LobbyInventory implements Listener {

  public static final ExItemStack TELEPORTER = new ExItemStack(5, Material.ENDER_PEARL)
      .setDisplayName("§1Teleporter")
      .setLore("§fThrow to teleport")
      .setMoveable(false)
      .setDropable(false);
  public static final ExItemStack RULES = new ExItemStack(0, Material.WRITTEN_BOOK)
      .setDisplayName("§4§lRules")
      .setLore("§fClick to show the rules")
      .setMoveable(false)
      .setDropable(false)
      .onClick(event -> event.getUser().sendPluginMessage(Plugin.LOBBY,
              Component.text("https://timesnake.de/rules", ExTextColor.PERSONAL)
                  .hoverEvent(HoverEvent.hoverEvent(Action.SHOW_TEXT,
                      Component.text("Click to open link")))
                  .clickEvent(ClickEvent.openUrl("https://timesnake.de/rules"))),
          true)
      .onInteract(event -> event.getUser().sendPluginMessage(Plugin.LOBBY,
          Component.text("https://timesnake.de/rules", ExTextColor.PERSONAL)
              .hoverEvent(HoverEvent.hoverEvent(Action.SHOW_TEXT,
                  Component.text("Click to open link")))
              .clickEvent(ClickEvent.openUrl("https://timesnake.de/rules"))), true);
  public static final ExItemStack SPEED = new ExItemStack(2, Material.FEATHER)
      .setDisplayName("§bSpeed")
      .setLore("§fHold in hand to get speed")
      .setMoveable(false)
      .setDropable(false);
  public static final ExItemStack GAMES_HUB = new ExItemStack(4, Material.NETHER_STAR)
      .setDisplayName("§6Games")
      .setLore("§fClick to open the game hub")
      .setMoveable(false)
      .setDropable(false)
      .onClick(event -> ((LobbyUser) event.getUser()).openGameHubInventory(), true)
      .onInteract(event -> ((LobbyUser) event.getUser()).openGameHubInventory(), true);
  public static final ExItemStack SPAWN = new ExItemStack(6, Material.BEACON)
      .setDisplayName("§6Spawn")
      .setLore("§fClick to teleport to spawn")
      .setMoveable(false)
      .setDropable(false)
      .onClick(event -> ((LobbyUser) event.getUser()).teleportSpawn(), true)
      .onInteract(event -> ((LobbyUser) event.getUser()).teleportSpawn(), true);
  public static final ExItemStack BUILD_SERVER = new ExItemStack(8, Material.STONE_PICKAXE)
      .setDisplayName("§3BUILD")
      .setLore("§fClick to switch mode")
      .setMoveable(false)
      .setDropable(false)
      .onClick(event -> ((LobbyUser) event.getUser()).openBuildInventory(), true)
      .onInteract(event -> ((LobbyUser) event.getUser()).openBuildInventory(), true);

  public LobbyInventory() {
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
