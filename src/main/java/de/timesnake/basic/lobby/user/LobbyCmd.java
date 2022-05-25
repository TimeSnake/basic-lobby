package de.timesnake.basic.lobby.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;

import java.util.List;

public class LobbyCmd implements CommandListener {

    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (sender.isPlayer(false)) {
            if (sender.hasPermission("lobby.build", 1201)) {
                LobbyUser user = (LobbyUser) sender.getUser();
                if (args.isLengthEquals(0, false)) {
                    user.switchMode();
                } else if (args.isLengthEquals(1, true) && args.get(0).isPlayerName(true)) {
                    LobbyUser user1 = (LobbyUser) args.get(0).toUser();
                    user1.switchMode();
                    sender.sendPluginMessage(ChatColor.VALUE + user1.getChatName() + ChatColor.PERSONAL + " switched " +
                            "mode!");
                }
            }
        } else if (sender.isConsole(false) && args.isLengthEquals(1, true) && args.get(0).isPlayerName(true)) {
            LobbyUser user = (LobbyUser) args.get(0).toUser();
            user.switchMode();
            sender.sendPluginMessage(ChatColor.VALUE + user.getChatName() + ChatColor.PERSONAL + " switched mode!");
        }
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (args.isLengthEquals(1, false)) {
            return Server.getCommandManager().getTabCompleter().getPlayerNames();
        }
        return List.of();
    }
}
