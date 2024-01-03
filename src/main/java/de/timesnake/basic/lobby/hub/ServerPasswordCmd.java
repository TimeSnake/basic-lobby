/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub;

import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.server.ServerInfo;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.UserChatCommandListener;
import de.timesnake.basic.bukkit.util.user.event.UserChatCommandEvent;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.library.extension.util.chat.Code;
import org.bukkit.event.EventHandler;

public class ServerPasswordCmd implements UserChatCommandListener {

  public static final Code PASSWORD_PERM = Plugin.LOBBY.createPermssionCode(
      "lobby.gamehub.password");

  private final ServerInfo server;

  public ServerPasswordCmd(ServerInfo server) {
    this.server = server;
  }

  @EventHandler
  public void onUserChatCommand(UserChatCommandEvent e) {
    User user = e.getUser();
    String password = e.getMessage();
    Sender sender = user.asSender(Plugin.LOBBY);

    sender.sendPluginTDMessage("§wPassword: §v" + "*".repeat(password.length()));

    if (e.getUser().hasPermission(PASSWORD_PERM, Plugin.LOBBY)) {
      sender.sendPluginTDMessage("§wUsed permission, instead of password");
    } else if (!password.equals(server.getPassword())) {
      sender.sendPluginTDMessage("§wWrong password, please select the server and try again");
      return;
    }

    sender.sendPluginTDMessage("§sSwitching to server §v" + this.server.getName());
    user.switchToServer(server.getPort());

    e.setCancelled(true);
  }
}
