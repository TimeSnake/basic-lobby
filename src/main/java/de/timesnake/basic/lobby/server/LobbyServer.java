/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.lobby.build.Build;
import de.timesnake.basic.lobby.hub.GamesMenu;
import de.timesnake.basic.lobby.user.LobbyInventory;
import de.timesnake.library.chat.Plugin;
import de.timesnake.library.waitinggames.WaitingGameManager;

public class LobbyServer extends Server {

  public static final Plugin PLUGIN = new Plugin("Lobby", "BLY");

  public static void msgHelp() {
    server.broadcastInfoMessage();
  }

  public static ExWorld getLobbyWorld() {
    return server.getLobbyWorld();
  }

  public static Build getBuild() {
    return server.getBuild();
  }

  public static GamesMenu getGamesMenu() {
    return server.getGamesMenu();
  }

  public static LobbyInventory getLobbyInventory() {
    return server.getLobbyInventory();
  }

  public static Sideboard getLobbySideboard() {
    return server.getLobbySideboard();
  }

  public static WaitingGameManager getWaitingGameManager() {
    return server.getWaitingGameManager();
  }

  private static final LobbyServerManager server = LobbyServerManager.getInstance();

}
