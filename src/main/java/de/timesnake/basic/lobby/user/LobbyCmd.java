/*
 * Copyright (C) 2023 timesnake
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
import java.util.List;
import net.kyori.adventure.text.Component;

public class LobbyCmd implements CommandListener {

    private Code buildPerm;

    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd,
            Arguments<Argument> args) {
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
        } else if (sender.isConsole(false) && args.isLengthEquals(1, true) && args.get(0)
                .isPlayerName(true)) {
            LobbyUser user = (LobbyUser) args.get(0).toUser();
            user.switchMode();
            sender.sendPluginMessage(user.getChatNameComponent()
                    .append(Component.text(" switched mode!", ExTextColor.PERSONAL)));
        }
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd,
            Arguments<Argument> args) {
        if (args.isLengthEquals(1, false)) {
            return Server.getCommandManager().getTabCompleter().getPlayerNames();
        }
        return List.of();
    }

    @Override
    public void loadCodes(Plugin plugin) {
        this.buildPerm = plugin.createPermssionCode("lobby.build");
    }
}
