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
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.basic.lobby.user.LobbyUser;
import org.bukkit.Material;

import java.util.List;

public abstract class GameHub<GameInfo extends de.timesnake.library.game.GameInfo> implements UserInventoryClickListener {

    public static final ExItemStack BACK = new ExItemStack(Material.BLUE_BED, 1, "§cBack",
            List.of("§fClick to get back"));

    public static final Integer SERVER_SLOTS_START = 9;

    protected final GameInfo gameInfo;

    protected final ExInventory inventory;

    protected final ExItemStack item;

    public GameHub(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
        this.inventory = Server.createExInventory(54, this.gameInfo.getDisplayName());
        this.inventory.setItemStack(4, BACK);
        this.item = new ExItemStack(this.gameInfo.getItem());

        Server.getInventoryEventManager().addClickListener(this, this.item);
    }

    protected Integer getServerNumber(Integer slot) {
        return slot - SERVER_SLOTS_START;
    }

    public Integer getEmptySlot() {
        return this.inventory.getFirstEmptySlot(SERVER_SLOTS_START);
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent event) {
        LobbyUser user = (LobbyUser) event.getUser();
        user.playSoundItemClicked();
        this.openServersInventory(user);
        event.setCancelled(true);
    }

    public ExItemStack getInvItem() {
        return this.item;
    }

    public ExInventory getInventory() {
        return this.inventory;
    }

    public void openServersInventory(User user) {
        user.openInventory(this.inventory);
    }

    public org.bukkit.inventory.ItemStack getItem() {
        return this.item;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public void updateServer(GameServer<?> server) {
        this.inventory.setItemStack(server.getSlot(), server.getItem());
    }
}
