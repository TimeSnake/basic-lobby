/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.user;

import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Completion;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.library.chat.Code;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;

public class LobbyCmd implements CommandListener {

  private final Code perm = Plugin.LOBBY.createPermssionCode("lobby.build");

  @Override
  public void onCommand(Sender sender, PluginCommand cmd, Arguments<Argument> args) {
    if (sender.isPlayer(false)) {
      sender.hasPermissionElseExit(this.perm);

      LobbyUser user = (LobbyUser) sender.getUser();
      if (args.isLengthEquals(0, false)) {
        user.switchMode();
      } else if (args.isLengthEquals(1, true)
          && args.get(0).isPlayerName(true)) {
        LobbyUser user1 = (LobbyUser) args.get(0).toUser();
        user1.switchMode();
        sender.sendPluginTDMessage(user1.getTDChatName() + "§s switched mode!");
      }
    } else if (sender.isConsole(false)
        && args.isLengthEquals(1, true)
        && args.get(0).isPlayerName(true)) {
      LobbyUser user = (LobbyUser) args.get(0).toUser();
      user.switchMode();
      sender.sendPluginTDMessage(user.getTDChatName() + "§s switched mode!");
    }
  }

  @Override
  public Completion getTabCompletion() {
    return new Completion(this.perm)
        .addArgument(Completion.ofPlayerNames());
  }

  @Override
  public String getPermission() {
    return this.perm.getPermission();
  }
}
