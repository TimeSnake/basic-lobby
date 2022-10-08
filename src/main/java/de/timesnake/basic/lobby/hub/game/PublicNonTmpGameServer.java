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

import de.timesnake.database.util.server.DbNonTmpGameServer;
import de.timesnake.library.game.NonTmpGameInfo;

import java.util.LinkedList;
import java.util.List;

public class PublicNonTmpGameServer extends NonTmpGameServer {

    public PublicNonTmpGameServer(GameHub<NonTmpGameInfo> hubGame, DbNonTmpGameServer server, String displayName, int slot) {
        super(hubGame, server, displayName, slot);
    }

    @Override
    public List<String> getPasswordLore() {
        List<String> lore = new LinkedList<>(super.getPasswordLore());
        lore.addAll(List.of("", "§7public server"));
        return lore;
    }
}
