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
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.chat.Plugin;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;

import java.util.List;

public class LobbyCmd implements CommandListener {

    private Code.Permission buildPerm;

    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (sender.isPlayer(false)) {
            if (sender.hasPermission(this.buildPerm)) {
                LobbyUser user = (LobbyUser) sender.getUser();
                if (args.isLengthEquals(0, false)) {
                    user.switchMode();
                } else if (args.isLengthEquals(1, true) && args.get(0).isPlayerName(true)) {
                    LobbyUser user1 = (LobbyUser) args.get(0).toUser();
                    user1.switchMode();
                    sender.sendPluginMessage(user1.getChatNameComponent()
                            .append(Component.text(" switched mode!", ExTextColor.PERSONAL)));
                }
            }
        } else if (sender.isConsole(false) && args.isLengthEquals(1, true) && args.get(0).isPlayerName(true)) {
            LobbyUser user = (LobbyUser) args.get(0).toUser();
            user.switchMode();
            sender.sendPluginMessage(user.getChatNameComponent()
                    .append(Component.text(" switched mode!", ExTextColor.PERSONAL)));
        }
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (args.isLengthEquals(1, false)) {
            return Server.getCommandManager().getTabCompleter().getPlayerNames();
        }
        return List.of();
    }

    @Override
    public void loadCodes(Plugin plugin) {
        this.buildPerm = plugin.createPermssionCode("lbl", "lobby.build");
    }
}
