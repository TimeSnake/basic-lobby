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

import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public interface GameServerBasis {

    Material ONLINE = Material.GREEN_WOOL;
    Material STARTING = Material.YELLOW_WOOL;
    Material ONLINE_FULL = Material.YELLOW_WOOL;
    Material SERVICE = Material.RED_WOOL;
    Material OFFLINE = Material.GRAY_WOOL;
    Material IN_GAME = Material.ORANGE_WOOL;

    String SERVER_TITLE_COLOR = org.bukkit.ChatColor.GOLD + "";

    String SERVER_NAME_COLOR = ChatColor.DARK_GRAY + "";

    String ONLINE_TEXT = "§aOnline";
    String OFFLINE_TEXT = "§cOffline";
    String STARTING_TEXT = "§eStarting";
    String INGAME_TEXT = "§eIn-game";
    String SERVICE_TEXT = "§6Service Work";
    String PLAYER_TEXT = "§9Players:";
    String PASSWORD_TEXT = "§cThis server is password protected!";
    String KIT_TEXT = "§bKits";
    String MAP_TEXT = "§bMaps";
    String OLD_PVP_TEXT = "§cpre1.9 PvP (1.8 PvP)";
    String PLAYERS_PER_TEAM_TEXT = "§3Players per Team: §f";
    String TEAM_AMOUNT = "§3Teams: §f";
    String QUEUE_JOIN_TEXT = "§7Right click to join the queue";
    String SPECTATOR_JOIN_TEXT = "§7Left click to join as spectator";
    Component INGAME_OFFLINE_MESSAGE = Component.text("This server is offline or in-game", ExTextColor.WARNING);
    Component SERVICE_MESSAGE = Component.text("This server is in service-mode", ExTextColor.WARNING);
    Component FULL_MESSAGE = Component.text("This server is full", ExTextColor.WARNING);

    String getName();

    ExItemStack getItem();

    int getSlot();

    void destroy();
}
