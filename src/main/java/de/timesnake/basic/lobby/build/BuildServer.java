package de.timesnake.basic.lobby.build;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.server.ServerInfo;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.basic.lobby.hub.ServerPasswordCmd;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.database.util.server.DbBuildServer;
import de.timesnake.library.basic.util.Status;
import org.bukkit.Material;

import java.util.Collections;

public class BuildServer extends ServerInfo implements UserInventoryClickListener, ChannelListener {

    private static final Material ONLINE = Material.GREEN_WOOL;
    private static final Material OFFLINE = Material.GRAY_WOOL;

    private final ExItemStack item;

    private final Build build;

    private final String task;

    public BuildServer(DbBuildServer server, int slot, Build build) {
        super(server);
        this.build = build;

        this.task = server.getTask();

        this.item = new ExItemStack(slot, OFFLINE, "§6" + server.getName()).setLore("§b" + task);

        Server.getInventoryEventManager().addClickListener(this, this.item);
        Server.getChannel().addListener(this, () -> Collections.singleton(server.getPort()));

        this.updateItem();
        Server.printText(Plugin.LOBBY, "Loaded build server " + super.name + " successfully", "Build");
    }

    private void updateItem() {
        if (this.onlinePlayers != null && this.onlinePlayers != 0) {
            this.item.setAmount(this.onlinePlayers);
        } else {
            this.item.setAmount(1);
        }

        if (this.status == null) {
            return;
        }
        if (this.status.equals(Status.Server.ONLINE) || this.status.equals(Status.Server.SERVICE)) {
            this.item.setType(ONLINE);
            this.item.setLore("", "§9Players:" + " §f" + this.onlinePlayers + " §8/ §f" + this.maxPlayers,
                    "§b" + this.task);
        } else if (this.status.equals(Status.Server.OFFLINE)) {
            this.item.setType(OFFLINE);
            this.item.setLore("", "§cOffline", "§b" + this.task);
        }

        this.build.getInventory().setItemStack(this.item);
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent e) {
        User user = e.getUser();
        Sender sender = user.asSender(Plugin.LOBBY);

        if (this.password != null) {
            user.closeInventory();
            sender.sendPluginMessage(ChatColor.PERSONAL + "Enter the server-password:");
            Server.getUserEventManager().addUserChatCommand(user, new ServerPasswordCmd(this));
        } else {
            if (this.status.equals(Status.Server.ONLINE) || this.status.equals(Status.Server.SERVICE)) {
                user.switchToServer(this.name);
                sender.sendPluginMessage(ChatColor.PERSONAL + "Switching to build-server " + ChatColor.VALUE + this.name);
            } else {
                sender.sendPluginMessage(ChatColor.WARNING + "This server is offline or in-game");
            }
        }
        e.setCancelled(true);
    }


    @ChannelHandler(type = ListenerType.SERVER, filtered = true)
    public void onServerMessage(ChannelServerMessage<?> msg) {
        super.setOnlinePlayers(database.getOnlinePlayers());
        this.maxPlayers = database.getMaxPlayers();
        this.status = database.getStatus();
        this.password = database.getPassword();

        this.updateItem();
    }

    public ExItemStack getItem() {
        return this.item;
    }
}
