/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.chat;

public class Plugin extends de.timesnake.basic.bukkit.util.chat.Plugin {

  public static final Plugin LOBBY = new Plugin("Lobby", "BLY");

  protected Plugin(String name, String code) {
    super(name, code);
  }
}
