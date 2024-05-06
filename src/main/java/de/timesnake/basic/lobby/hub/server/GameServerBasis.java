/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.hub.server;

import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import org.bukkit.Material;

public interface GameServerBasis {

  Material ONLINE = Material.GREEN_WOOL;
  Material STARTING = Material.YELLOW_WOOL;
  Material ONLINE_FULL = Material.YELLOW_WOOL;
  Material SERVICE = Material.RED_WOOL;
  Material OFFLINE = Material.GRAY_WOOL;
  Material IN_GAME = Material.ORANGE_WOOL;

  String SERVER_NAME_COLOR = ChatColor.DARK_GRAY + "";

  String ONLINE_TEXT = "§aOnline";
  String OFFLINE_TEXT = "§cOffline";
  String STARTING_TEXT = "§eStarting";
  String IN_GAME_TEXT = "§eGame Running";
  String PRE_GAME_TEXT = "§eGame Starting";
  String POST_GAME_TEXT = "§eGame Ended";
  String SERVICE_TEXT = "§6Service Work";
  String PLAYER_TEXT = "§9Players:";
  String PASSWORD_TEXT = "§cThis server is password protected!";
  String KIT_TEXT = "§bKits";
  String MAP_TEXT = "§bMaps";
  String OLD_PVP_TEXT = "§cpre1.9 PvP (1.8 PvP)";
  String TEAM_AMOUNT = "§3Teams: §f";
  String QUEUE_JOIN_TEXT = "§7Right click to join the queue";
  String SPECTATOR_JOIN_TEXT = "§7Left click to join as spectator";
  String IN_GAME_OFFLINE_MESSAGE = "§wThis server is offline or in-game";
  String SERVICE_MESSAGE = "§wThis server is in service-mode";
  String FULL_MESSAGE = "§wThis server is full";

  String getServerName();

  ExItemStack getItem();

  int getSlot();

  void destroy();
}
