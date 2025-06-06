/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lobby.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandManager;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserJoinEvent;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.user.scoreboard.SideboardBuilder;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.ExWorldOption;
import de.timesnake.basic.lobby.build.Build;
import de.timesnake.basic.lobby.hub.GamesMenu;
import de.timesnake.basic.lobby.main.BasicLobby;
import de.timesnake.basic.lobby.user.JailManager;
import de.timesnake.basic.lobby.user.LobbyInventory;
import de.timesnake.basic.lobby.user.LobbyUser;
import de.timesnake.basic.lobby.user.UserManager;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.chat.Plugin;
import de.timesnake.library.network.NetworkVariables;
import de.timesnake.library.pets.PetManager;
import de.timesnake.library.plot.plots.PlotManager;
import de.timesnake.library.shop.ShopManager;
import de.timesnake.library.waitinggames.WaitingGameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameRule;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class LobbyServerManager extends ServerManager implements ChannelListener, Listener {

  public static LobbyServerManager getInstance() {
    return (LobbyServerManager) ServerManager.getInstance();
  }

  private LobbyInventory lobbyInventory;
  private Build build;
  private GamesMenu gamesMenu;
  private Sideboard sideboard;
  private ExWorld lobbyWorld;
  private JailManager jailManager;
  private UserManager userManager;
  private WaitingGameManager waitingGameManager;
  private PlotManager plotManager;
  private PetManager petManager;
  private ShopManager shopManager;

  @Override
  public LobbyUser loadUser(Player player) {
    return new LobbyUser(player);
  }

  public void onLobbyEnable() {
    this.lobbyInventory = new LobbyInventory();
    this.build = new Build();
    this.gamesMenu = new GamesMenu();

    this.userManager = new UserManager();

    this.getWorldManager().setCacheWorldSpawns(true);

    this.lobbyWorld = Server.getWorld("world");

    this.jailManager = new JailManager();

    int spawnX = this.lobbyWorld.getSpawnLocation().getChunk().getX();
    int spawnZ = this.lobbyWorld.getSpawnLocation().getChunk().getZ();

    for (int x = -2; x < 2; x++) {
      for (int z = -2; z < 2; z++) {
        this.lobbyWorld.getChunkAt(spawnX + x, spawnZ + z).setForceLoaded(true);
      }
    }

    this.lobbyWorld.setOption(ExWorldOption.ENABLE_ENTITY_EXPLOSION, false);
    this.lobbyWorld.setOption(ExWorldOption.ALLOW_FLUID_FLOW, false);
    this.lobbyWorld.setOption(ExWorldOption.ENABLE_PLAYER_DAMAGE, false);
    this.lobbyWorld.setOption(ExWorldOption.CHANGE_FOOD, false);
    this.lobbyWorld.setOption(ExWorldOption.BLOCK_BURN_UP, false);
    this.lobbyWorld.setOption(ExWorldOption.ALLOW_ENTITY_BLOCK_BREAK, false);
    this.lobbyWorld.setOption(ExWorldOption.ALLOW_DROP_PICK_ITEM, false);
    this.lobbyWorld.setOption(ExWorldOption.ALLOW_BLOCK_BREAK, false);
    this.lobbyWorld.setOption(ExWorldOption.ALLOW_BLOCK_PLACE, false);
    this.lobbyWorld.setOption(ExWorldOption.ALLOW_PLACE_IN_BLOCK, false);
    this.lobbyWorld.setOption(ExWorldOption.ALLOW_FLINT_AND_STEEL, false);
    this.lobbyWorld.setOption(ExWorldOption.ALLOW_LIGHT_UP_INTERACTION, false);
    this.lobbyWorld.setExceptService(true);
    this.lobbyWorld.setPVP(true);
    this.lobbyWorld.setGameRule(GameRule.KEEP_INVENTORY, true);
    this.lobbyWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
    this.lobbyWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

    Thread chatInfoThread = new Thread(new ChatInfoRepeater());
    chatInfoThread.start();

    this.sideboard = Server.getScoreboardManager().registerSideboard(new SideboardBuilder()
        .name("lobby")
        .title("§6§lLobby")
        .setScore(7, "§1Online:")
        .setScore(6, "§6" + Server.getNetwork().getPlayerAmount())
        .setScore(5, "§r§f§f---------------§r")
        .setScore(4, "§1" + Server.getNetwork().getVariables()
            .getValue(NetworkVariables.COINS_NAME) + ": ")
        // user time coins
        .setScore(2, "§f§f---------------§r")
        .setScore(1, "§8Server: ")
        .setScore(0, "§7" + Server.getName()));
    this.waitingGameManager = new WaitingGameManager();
    this.plotManager = new PlotManager(BasicLobby.getPlugin()) {
      @Override
      public void onBuildingUserLeave(User user) {
        super.onBuildingUserLeave(user);
        ((LobbyUser) user).joinLobby(false);
      }
    };

    this.petManager = new PetManager(BasicLobby.getPlugin());
    this.shopManager = new ShopManager(BasicLobby.getPlugin(), this.petManager, 6, 7);

    Server.getWorldManager().getWorldBorderManager().setCustomBorders(false);
    Server.getWorldManager().getWorldBorderManager().allowEnderpearlThrouBorder(false);

    Server.registerListener(this.lobbyInventory, BasicLobby.getPlugin());

    new ServerUpdater();
  }

  public void onLobbyDisable() {
    this.waitingGameManager.onDisable();
    this.petManager.onDisable();
  }

  @Override
  protected CommandManager initCommandManager() {
    return new de.timesnake.basic.bukkit.core.chat.CommandManager() {
      @Override
      public boolean onCommand(@NotNull CommandSender cmdSender, Command cmd,
                               @NotNull String label, String[] args) {
        if (cmdSender instanceof Player player) {
          if (((LobbyUser) Server.getUser(player)).isJailed()) {
            return false;
          }
        }
        return super.onCommand(cmdSender, cmd, label, args);
      }
    };
  }

  public void broadcastInfoMessage() {
    NetworkVariables variables = Server.getNetwork().getVariables();
    switch (new Random().nextInt(5)) {
      case 0 -> Server.broadcastTDMessage(Plugin.INFO, "§pDo you need help? Use §v/support");
      case 1 -> {
        if (variables.getValue(NetworkVariables.PATREON_LINK) != null) {
          Server.broadcastClickableMessage(Plugin.INFO,
              Component.text("Want to support the server? Donate via ", ExTextColor.PUBLIC)
                  .append(Component.text("Patreon", ExTextColor.PUBLIC,
                      TextDecoration.UNDERLINED)),
              variables.getValue(NetworkVariables.PATREON_LINK),
              Component.text("Click to open the link"),
              ClickEvent.Action.OPEN_URL);
        }
      }
      case 2 -> {
        if (variables.getValue(NetworkVariables.DISCORD_LINK) != null) {
          Server.broadcastClickableMessage(Plugin.INFO,
              Component.text("Join our ", ExTextColor.PUBLIC)
                  .append(Component.text("discord", ExTextColor.PUBLIC,
                      TextDecoration.UNDERLINED))
                  .append(Component.text(" and meet our community", ExTextColor.PUBLIC)),
              variables.getValue(NetworkVariables.DISCORD_LINK),
              Component.text("Click to open the link"),
              ClickEvent.Action.OPEN_URL);
        }
      }
      case 3 -> {
        if (variables.getValue(NetworkVariables.WEBSITE_LINK) != null) {
          Server.broadcastClickableMessage(Plugin.INFO,
              Component.text("Visit our ", ExTextColor.PUBLIC)
                  .append(Component.text("website", ExTextColor.PUBLIC,
                      TextDecoration.UNDERLINED))
                  .append(Component.text(" to find out more about the server",
                      ExTextColor.PUBLIC)),
              variables.getValue(NetworkVariables.WEBSITE_LINK),
              Component.text("Click to open the link", ExTextColor.PUBLIC),
              ClickEvent.Action.OPEN_URL);
        }
      }
      case 4 -> Server.broadcastTDMessage(Plugin.INFO,
          "§wTrampling on turtle eggs is forbidden!");
    }
    Server.broadcastNote(Instrument.PLING, Note.natural(1, Note.Tone.C));
  }

  public ExWorld getLobbyWorld() {
    return lobbyWorld;
  }

  public JailManager getJailManager() {
    return jailManager;
  }

  public Build getBuild() {
    return build;
  }

  public GamesMenu getGamesMenu() {
    return gamesMenu;
  }

  public LobbyInventory getLobbyInventory() {
    return lobbyInventory;
  }

  public Sideboard getLobbySideboard() {
    return sideboard;
  }

  public WaitingGameManager getWaitingGameManager() {
    return waitingGameManager;
  }

  @EventHandler
  public void onUserJoin(UserJoinEvent e) {
    LobbyUser user = (LobbyUser) e.getUser();
    if (user.isService()) {
      user.sendPluginTDMessage(LobbyServer.PLUGIN, "§sSwitched to lobby-mode!");
      user.setStatus(Status.User.ONLINE);
    }

    if (user.isJailed()) {
      this.getJailManager().jailUser(user);
      return;
    }

    if (Server.getNetwork().getVariables().getValue(NetworkVariables.PRIVACY_POLICY_LINK) != null
        && !user.agreedPrivacyPolicy()) {
      user.unlockAll();
      user.clearInventory();
      user.resetPlayerProperties();
    } else {
      user.joinLobby(true);
    }
  }

  private class ChatInfoRepeater implements Runnable {

    public void run() {
      Server.runTaskTimerSynchrony(LobbyServerManager.this::broadcastInfoMessage, 0,
          20 * 60 * 3,
          BasicLobby.getPlugin());
    }
  }
}
