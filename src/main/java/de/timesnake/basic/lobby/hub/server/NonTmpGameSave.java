/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryClickListener;
import de.timesnake.basic.lobby.hub.game.GameHub;
import de.timesnake.basic.lobby.server.LobbyServer;
import de.timesnake.basic.lobby.user.LobbyUser;
import de.timesnake.channel.util.message.ChannelUserMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.game.NonTmpGameInfo;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NonTmpGameSave implements GameServerBasis, UserInventoryClickListener {

  private static final String START_TEXT = "§cClick to start the server";

  private final String name;
  private final int slot;
  private final String displayName;
  private final ExItemStack item;

  private final GameHub<NonTmpGameInfo> gameHub;

  private final boolean isPublic;
  private final UUID owner;
  private final String ownerName;

  public NonTmpGameSave(GameHub<NonTmpGameInfo> hub, String name, String displayName,
                        UUID owner, String ownerName, int slot, boolean isPublic) {
    this.gameHub = hub;
    this.name = name;
    this.slot = slot;
    this.displayName = displayName;
    this.isPublic = isPublic;
    this.owner = owner;
    this.ownerName = ownerName;
    this.item = new ExItemStack(slot, OFFLINE).setDisplayName("§6" + this.displayName);

    List<String> lore = new ArrayList<>();
    lore.add("");
    lore.add(OFFLINE_TEXT);
    lore.add("");
    lore.add(START_TEXT);
    lore.addAll(this.getServerOwnerLore());
    lore.addAll(this.getServerNameLore());

    this.item.setExLore(lore);

    this.gameHub.getInventory().setItemStack(this.slot, this.item);

    Server.getInventoryEventManager().addClickListener(this, this.item);
  }

  public List<String> getServerOwnerLore() {
    if (this.isPublic) {
      return List.of("", "§7public server");
    } else {
      return List.of("", "§fOwner: §7" + this.ownerName);
    }
  }

  public List<String> getServerNameLore() {
    return List.of("", SERVER_NAME_COLOR + this.name);
  }

  public String getName() {
    return name;
  }

  @Override
  public String getServerName() {
    return name;
  }

  public String getDisplayName() {
    return displayName;
  }

  @Override
  public ExItemStack getItem() {
    return item;
  }

  @Override
  public int getSlot() {
    return this.slot;
  }

  @Override
  public void destroy() {
    Server.getInventoryEventManager().removeClickListener(this);
  }

  public boolean isPublic() {
    return isPublic;
  }

  @Override
  public void onUserInventoryClick(UserInventoryClickEvent e) {
    LobbyUser user = (LobbyUser) e.getUser();

    e.setCancelled(true);

    if (this.owner != null && !user.getUniqueId().equals(this.owner)) {
      user.sendPluginMessage(LobbyServer.PLUGIN,
          Component.text("Only the owner can start the server", ExTextColor.WARNING));
      user.closeInventory();
      return;
    }

    if (this.isPublic) {
      Server.getChannel()
          .sendMessage(new ChannelUserMessage<>(user.getUniqueId(), MessageType.User.PROXY_COMMAND,
              "start public_game " + this.gameHub.getGameInfo().getName() + " " + this.name));
    } else {
      Server.getChannel()
          .sendMessage(new ChannelUserMessage<>(user.getUniqueId(), MessageType.User.PROXY_COMMAND,
              "start own_game " + this.gameHub.getGameInfo().getName() + " " + this.name));
    }
    user.closeInventory();
  }
}
