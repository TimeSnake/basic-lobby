package de.timesnake.basic.lobby.main;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.basic.lobby.server.LobbyServerManager;
import de.timesnake.basic.lobby.user.LobbyCmd;
import org.bukkit.plugin.java.JavaPlugin;

public class BasicLobby extends JavaPlugin {


    private static BasicLobby plugin;

    @Override
    public void onLoad() {
        ServerManager.setInstance(new LobbyServerManager());
    }

    @Override
    public void onEnable() {
        plugin = this;

        Server.getCommandManager().addCommand(this, "build", new LobbyCmd(), Plugin.LOBBY);
        Server.registerListener(LobbyServerManager.getInstance(), this);

        LobbyServerManager.getInstance().onLobbyEnable();

    }

    public static BasicLobby getPlugin() {
        return plugin;
    }
}
