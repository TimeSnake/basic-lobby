/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub;

import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.UserChatCommandListener;
import de.timesnake.basic.bukkit.util.user.event.UserChatCommandEvent;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.library.chat.Code;
import org.bukkit.event.EventHandler;

public class ServerPasswordCmd implements UserChatCommandListener {

  public static final Code PASSWORD_PERM = Plugin.LOBBY.createPermssionCode(
      "lobby.gamehub.password");

  private final String serverName;
  private final String password;

  public ServerPasswordCmd(String serverName, String password) {
    this.serverName = serverName;
    this.password = password;
  }

  @EventHandler
  public void onUserChatCommand(UserChatCommandEvent e) {
    User user = e.getUser();
    String password = e.getMessage();
    Sender sender = user.asSender(Plugin.LOBBY);

    sender.sendPluginTDMessage("§wPassword: §v" + "*".repeat(password.length()));

    if (e.getUser().hasPermission(PASSWORD_PERM, Plugin.LOBBY)) {
      sender.sendPluginTDMessage("§wUsed permission, instead of password");
    } else if (!password.equals(this.password)) {
      sender.sendPluginTDMessage("§wWrong password, please select the server and try again");
      return;
    }

    sender.sendPluginTDMessage("§sSwitching to server §v" + this.serverName);
    user.switchToServer(this.serverName);

    e.setCancelled(true);
  }
}
