package de.timesnake.basic.lobby.hub;

import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.server.ServerInfo;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.UserChatCommandListener;
import de.timesnake.basic.bukkit.util.user.event.UserChatCommandEvent;
import de.timesnake.basic.lobby.chat.Plugin;
import org.bukkit.event.EventHandler;

public class ServerPasswordCmd implements UserChatCommandListener {

    private final ServerInfo server;

    public ServerPasswordCmd(ServerInfo server) {
        this.server = server;
    }

    @EventHandler
    public void onUserChatCommand(UserChatCommandEvent e) {
        User user = e.getUser();
        String password = e.getMessage();
        Sender sender = user.asSender(Plugin.LOBBY);

        StringBuilder censoredPassword = new StringBuilder();
        for (char ignored : password.toCharArray()) {
            censoredPassword.append("*");
        }

        sender.sendPluginMessage(ChatColor.WARNING + "Password: " + ChatColor.VALUE + censoredPassword.toString());

        if (e.getUser().hasPermission("lobby.gamehub.password", 1206, Plugin.LOBBY)) {
            sender.sendPluginMessage(ChatColor.PERSONAL + "Used permission, instead of password");
        } else if (!password.equals(server.getPassword())) {
            sender.sendPluginMessage(ChatColor.WARNING + "Wrong password, please select the server and try again");
            return;
        }

        sender.sendPluginMessage(ChatColor.PERSONAL + "Switching to server " + ChatColor.VALUE + this.server.getName());
        user.switchToServer(server.getPort());

        e.setCancelled(true);
    }
}
