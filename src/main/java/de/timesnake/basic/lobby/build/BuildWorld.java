/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.build;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.channel.util.message.ChannelUserMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.library.basic.util.chat.ExTextColor;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class BuildWorld {

    private static final Material ONLINE = Material.GREEN_WOOL;
    private static final Material OFFLINE = Material.GRAY_WOOL;


    private final String name;
    private final ExItemStack item;

    private String serverName;

    public BuildWorld(String name, String shortWorldName) {
        this.name = name;
        this.item = new ExItemStack(OFFLINE, "§6" + shortWorldName).setLore("", "§cUnloaded", "§7Click to load");
    }

    public String getName() {
        return name;
    }

    public boolean update(String serverName) {
        if (Objects.equals(this.serverName, serverName)) {
            return false;
        }

        this.serverName = serverName;

        if (this.serverName != null) {
            this.item.setType(ONLINE);
            this.item.setLore("", "§2Loaded", "§7Click to teleport", "§8" + this.serverName);
        } else {
            this.item.setType(OFFLINE);
            this.item.setLore("", "§cUnloaded", "§7Click to load");
        }

        return true;
    }

    public boolean removeIfServer(String serverName) {
        if (serverName.equals(this.serverName)) {
            this.update(null);
            return true;
        }
        return false;
    }

    public void moveUser(User user) {
        Sender sender = user.asSender(Plugin.LOBBY);

        if (this.isLoaded()) {
            user.switchToServer(this.serverName);
            sender.sendPluginMessage(Component.text("Switching to build-server ", ExTextColor.PERSONAL)
                    .append(Component.text(this.serverName, ExTextColor.VALUE)));
        } else {
            Server.getChannel().sendMessage(new ChannelUserMessage<>(user.getUniqueId(), MessageType.User.PROXY_COMMAND,
                    "build " + this.name));
            user.closeInventory();
        }
    }

    public boolean isLoaded() {
        return this.serverName != null;
    }


    public ExItemStack getItem() {
        return this.item;
    }

}
