/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryClickListener;
import de.timesnake.basic.lobby.server.LobbyServer;
import de.timesnake.channel.util.message.ChannelUserMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.library.basic.util.Availability;
import de.timesnake.library.chat.Code;
import de.timesnake.library.game.GameInfo;
import de.timesnake.library.game.NonTmpGameInfo;
import org.bukkit.Material;

public class CreationRequestManager implements UserInventoryClickListener {

  public static final Code CREATION_PERM = LobbyServer.PLUGIN.createPermssionCode(
      "lobby.gamehub.creation_request");

  private final ExItemStack item = new ExItemStack(Material.BLUE_WOOL)
      .setDisplayName("§9Request new server")
      .setLore("", "§7Click to request a new server", "", "§cPunishable in case of abuse")
      .setMoveable(false).setDropable(false).immutable();

  private final GameHub<?> hub;

  public CreationRequestManager(GameHub<?> hub) {
    Server.getInventoryEventManager().addClickListener(this, this.item);
    this.hub = hub;
  }

  public ExItemStack getItem() {
    return item;
  }

  @Override
  public void onUserInventoryClick(UserInventoryClickEvent e) {
    User user = e.getUser();

    e.setCancelled(true);

    if (!user.hasPermission(CREATION_PERM, LobbyServer.PLUGIN)) {
      return;
    }

    StringBuilder settings = new StringBuilder();

    GameInfo info = this.hub.getGameInfo();

    if (info.getMapAvailability().equals(Availability.REQUIRED)) {
      settings.append(" maps");
    }

    if (info.getKitAvailability().equals(Availability.REQUIRED)) {
      settings.append(" kits");
    }

    if (this.hub.getGameInfo() instanceof NonTmpGameInfo nonTmpInfo) {

      if (nonTmpInfo.isOwnable()) {
        // TODO own games
      } else {
        Server.getChannel().sendMessage(
            new ChannelUserMessage<>(user.getUniqueId(), MessageType.User.PROXY_COMMAND,
                "start game " + info.getName() + settings));
      }
    } else {
      // TODO tmp games
    }
  }
}
