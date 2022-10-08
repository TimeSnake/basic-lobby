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

package de.timesnake.basic.lobby.hub.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.basic.lobby.chat.Plugin;
import de.timesnake.channel.util.message.ChannelUserMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.object.Type;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.game.GameInfo;
import de.timesnake.library.game.NonTmpGameInfo;
import org.bukkit.Material;

public class CreationRequestManager implements UserInventoryClickListener {

    public static final Code.Permission CREATION_PERM = Plugin.LOBBY.createPermssionCode("ghc", "lobby.gamehub.creation_request");

    private final ExItemStack item = new ExItemStack(Material.BLUE_WOOL).setDisplayName("§9Request new server")
            .setLore("", "§7Click to request a new server").setMoveable(false).setDropable(false).immutable();

    private final GameHub<?> hub;

    public CreationRequestManager(GameHub<?> hub) {
        Server.getInventoryEventManager().addClickListener(this, this.item);
        this.hub = hub;
    }

    public ExItemStack getItem() {
        return item;
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent e) {
        User user = e.getUser();

        e.setCancelled(true);

        if (!user.hasPermission(CREATION_PERM, Plugin.LOBBY)) {
            return;
        }

        StringBuilder settings = new StringBuilder();

        GameInfo info = this.hub.getGameInfo();

        if (info.getMapAvailability().equals(Type.Availability.REQUIRED)) {
            settings.append(" maps");
        }

        if (info.getKitAvailability().equals(Type.Availability.REQUIRED)) {
            settings.append(" kits");
        }

        if (this.hub.getGameInfo() instanceof NonTmpGameInfo nonTmpInfo) {

            if (nonTmpInfo.isOwnable()) {
                // TODO own games
            } else {
                Server.getChannel().sendMessage(new ChannelUserMessage<>(user.getUniqueId(), MessageType.User.PROXY_COMMAND,
                        "start game " + info.getName() + settings));
            }
        } else {
            // TODO tmp games
        }
    }
}
