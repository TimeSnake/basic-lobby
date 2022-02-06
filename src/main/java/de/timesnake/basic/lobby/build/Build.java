package de.timesnake.basic.lobby.build;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.object.Type;
import de.timesnake.database.util.server.DbBuildServer;

import java.util.HashMap;

public class Build {


    private final ExInventory inventory = Server.createExInventory(54, "Build-Servers");

    private final HashMap<Integer, BuildServer> servers = new HashMap<>();

    public Build() {
        int i = 0;
        for (DbBuildServer server : Database.getServers().getServers(Type.Server.BUILD)) {
            BuildServer build = new BuildServer(server, i, this);
            this.servers.put(i, build);
            this.inventory.setItemStack(i, build.getItem());
            i++;
        }
        Server.printText(Plugin.LOBBY, "Loaded build servers successfully", "Build");
    }

    public ExInventory getInventory() {
        return inventory;
    }
}
