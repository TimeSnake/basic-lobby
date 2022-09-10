package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.basic.lobby.user.LobbyUser;
import de.timesnake.channel.util.message.ChannelUserMessage;
import de.timesnake.channel.util.message.MessageType;

import java.util.ArrayList;
import java.util.List;

public class UnloadedNonTmpGameServer implements GameServerBasis, UserInventoryClickListener {

    private static final String START_TEXT = "§cClick to start the server";

    private final String name;
    private final ExItemStack item;

    private final NonTmpGameHub gameHub;
    private final boolean isPublic;

    public UnloadedNonTmpGameServer(NonTmpGameHub hub, String name, int slot, boolean isPublic) {
        this.gameHub = hub;
        this.name = name;
        this.isPublic = isPublic;
        this.item = new ExItemStack(slot, OFFLINE, SERVER_TITLE_COLOR + this.name);

        List<String> lore = new ArrayList<>();
        lore.add("");

        lore.add(OFFLINE_TEXT);
        lore.add("");
        lore.add(START_TEXT);

        lore.addAll(this.getServerNameLore());

        this.item.setExLore(lore);

        if (this.isPublic) {
            this.gameHub.getInventory().setItemStack(this.item);
        }

        Server.getInventoryEventManager().addClickListener(this, this.item);
    }

    public List<String> getServerNameLore() {
        return List.of("", SERVER_NAME_COLOR + this.name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ExItemStack getItem() {
        return item;
    }

    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent e) {
        LobbyUser user = (LobbyUser) e.getUser();

        if (this.isPublic) {
            Server.getChannel().sendMessage(new ChannelUserMessage<>(user.getUniqueId(), MessageType.User.PROXY_COMMAND,
                    "start public_game " + this.gameHub.getGameInfo().getName() + " " + this.name));
        } else {
            Server.getChannel().sendMessage(new ChannelUserMessage<>(user.getUniqueId(), MessageType.User.PROXY_COMMAND,
                    "start own_game " + this.gameHub.getGameInfo().getName() + " " + this.name));
        }
        user.closeInventory();
    }
}
