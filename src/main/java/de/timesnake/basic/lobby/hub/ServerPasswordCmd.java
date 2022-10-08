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

package de.timesnake.basic.lobby.hub;

import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.server.ServerInfo;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.UserChatCommandListener;
import de.timesnake.basic.bukkit.util.user.event.UserChatCommandEvent;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Code;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;

public class ServerPasswordCmd implements UserChatCommandListener {

    public static final Code.Permission PASSWORD_PERM = Plugin.LOBBY.createPermssionCode("pwd", "lobby.gamehub.password");

    private final ServerInfo server;

    public ServerPasswordCmd(ServerInfo server) {
        this.server = server;
    }

    @EventHandler
    public void onUserChatCommand(UserChatCommandEvent e) {
        User user = e.getUser();
        String password = e.getMessage();
        Sender sender = user.asSender(Plugin.LOBBY);

        sender.sendPluginMessage(Component.text("Password: ", ExTextColor.WARNING)
                .append(Component.text("*".repeat(password.length()), ExTextColor.VALUE)));

        if (e.getUser().hasPermission(PASSWORD_PERM, Plugin.LOBBY)) {
            sender.sendPluginMessage(Component.text("Used permission, instead of password", ExTextColor.PERSONAL));
        } else if (!password.equals(server.getPassword())) {
            sender.sendPluginMessage(Component.text("Wrong password, please select the server and try again", ExTextColor.WARNING));
            return;
        }

        sender.sendPluginMessage(Component.text("Switching to server ", ExTextColor.PERSONAL)
                .append(Component.text(this.server.getName(), ExTextColor.VALUE)));
        user.switchToServer(server.getPort());

        e.setCancelled(true);
    }
}
