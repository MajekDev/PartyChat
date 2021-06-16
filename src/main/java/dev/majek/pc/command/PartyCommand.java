/*
 * This file is part of PartyChat, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 Majekdor
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.majek.pc.command;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.party.PartyVersion;
import dev.majek.pc.data.object.Cooldown;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.Restrictions;
import dev.majek.pc.data.object.User;
import dev.majek.pc.gui.GuiInParty;
import dev.majek.pc.gui.GuiNoParty;
import dev.majek.pc.chat.MessageHandler;
import dev.majek.pc.util.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all /party commands and subcommands.
 */
public abstract class PartyCommand extends MessageHandler implements CommandExecutor, TabCompleter {

  private final String name;
  private final String usage;
  private final String description;
  private final boolean requiresLeader;
  private int cooldown;
  private boolean disabled;
  private final List<String> aliases;

  /**
   * PartyChat's main config file.
   */
  public static FileConfiguration mainConfig = PartyChat.dataHandler().mainConfig;
  /**
   * Map in a map for cooldown storage.
   */
  public static Map<Player, Map<PartyCommand, Cooldown>> cooldownMap = new HashMap<>();

  protected PartyCommand(String name, String usage, String description, boolean requiresLeader, boolean disabled,
                         int cooldown, List<String> aliases) {
    this.name = name;
    this.usage = usage;
    this.description = description;
    this.requiresLeader = requiresLeader;
    this.cooldown = cooldown;
    this.disabled = disabled;
    this.aliases = new ArrayList<>(aliases);
  }

  public abstract boolean execute(Player player, String[] args, boolean leftServer);

  /**
   * Refresh the main PartyChat config object used in this class and subclasses.
   */
  public static void reload() {
    PartyChat.core().reloadConfig();
    mainConfig = PartyChat.dataHandler().mainConfig;
  }

  /**
   * Handle all of the /party command and subcommands.
   * @param sender The command's sender. In this method we only accept a Player.
   * @param command The command itself.
   * @param label The command's label.
   * @param args The command arguments.
   * @return True/False just to make Bukkit happy.
   */
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
    String commandString = "/" + command.getName() + " " + String.join(" ", args);
    try {
      if (command.getName().equalsIgnoreCase("party")) {

        PartyChat.dataHandler().logToFile("Sender " + sender.getName()
            + " executed command " + commandString, "COMMAND");

        // Don't allow use from console
        if (!(sender instanceof Player)) {
          sendMessage(sender, "no-console"); return true;
        }
        Player player = (Player) sender;
        User user = PartyChat.dataHandler().getUser(player);

        // Check if the admins wants to use permissions
        // Ignore this whole section if they have admin perms
        if (PartyChat.dataHandler().getConfigBoolean(mainConfig, "use-permissions")
            && !player.hasPermission("partychat.admin"))
          if (!player.hasPermission("partychat.use")) {
            sendMessage(player, "no-permission"); return true;
          }

        if (args.length > 0) {

          // Get the specified subcommand
          PartyCommand partyCommand = PartyChat.commandHandler().getCommand(args[0].toLowerCase());
          if (partyCommand == null) {
            sendMessage(player, "unknown-command");
            return true;
          }

          // Make sure the player cann use the command
          if (!partyCommand.canUse(player))
            return true;

          // Execute command
          boolean executedFully = partyCommand.execute(player, args, false);

          // Put the command on cooldown if it completely executed
          if (executedFully) {
            Map<PartyCommand, Cooldown> newMap = new HashMap<>();
            newMap.put(partyCommand, new Cooldown(partyCommand));
            cooldownMap.put(player, newMap);
          }
        } else {
          if (!PartyChat.dataHandler().disableGuis) {
            if (user.isInParty())
              new GuiInParty().openGui(player);
            else
              new GuiNoParty().openGui(player);
          } else {
            PartyVersion.execute(player);
          }
        }
      }
    } catch (Exception ex) {
      StringBuilder error = new StringBuilder();
      error.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append('\n');
      for (StackTraceElement ste : ex.getStackTrace())
        error.append("    at ").append(ste.toString()).append('\n');
      String errorString = error.toString();
      if (sender.hasPermission("partychat.admin"))
        sendMessageWithReplacement(sender, "command-error-staff", "%command%", commandString);
      else
        sendMessageWithReplacement(sender, "command-error", "%command%", commandString);
      PartyChat.error("There was an error executing command " + commandString);
      PartyChat.error(errorString);
      ex.printStackTrace();
    }
    return false;
  }

  /**
   * Tab completion for /party command and subcommands.
   * @param sender The command's sender.
   * @param command The command itself.
   * @param alias The command's alias.
   * @param args The command arguments.
   * @return A list of strings to show the sender.
   * @throws IllegalArgumentException Allows for returning {@link Collections#emptyList()}.
   */
  @Override
  @SuppressWarnings("ConstantConditions")
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                    @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
    Player player = (Player) sender;
    User user = PartyChat.dataHandler().getUser(player);
    if (args.length == 1) {
      // If you're not in a party or you're the party leader all subcommands will be available to tab complete
      // If you're just a party member then leader commands won't be shown
      return (user.isLeader() || player.hasPermission("partychat.bypass")) ? TabCompleterBase
          .filterStartingWith(args[0], PartyChat.commandHandler().getLeaderCommandsAndAliases())
          : TabCompleterBase.filterStartingWith(args[0], PartyChat
          .commandHandler().getAllCommandsAndAliases());
    } else if (args.length > 3) {
      return Collections.emptyList();
    } else {
      PartyCommand partyCommand = PartyChat.commandHandler().getCommand(args[0]);
      if (partyCommand == null)
        return Collections.emptyList();
      switch (partyCommand.getName()) {
        case "accept":
        case "deny":
          return (user.isLeader() || player.hasPermission("partychat.bypass")) ? TabCompleterBase
              .filterStartingWith(args[1], PartyChat.partyHandler()
                  .getParty(player).getPendingInvitations().stream().map(Pair::getFirst)
                  .map(Player::getName)) : Collections.emptyList();
        case "help":
          return TabCompleterBase.filterStartingWith(args[1], Arrays.asList("1", "2"));
        case "toggle":
          if (args.length == 2)
            return (user.isLeader() || player.hasPermission("partychat.bypass")) ? TabCompleterBase
                .filterStartingWith(args[1], Arrays.asList("public", "friendly-fire"))
                : Collections.emptyList();
          if (args.length == 3)
            if (args[1].equalsIgnoreCase("public"))
              return (user.isLeader() || player.hasPermission("partychat.bypass")) ?
                  TabCompleterBase.filterStartingWith(args[2], Arrays
                      .asList("true", "false")) : Collections.emptyList();
            else
              return (user.isLeader() || player.hasPermission("partychat.bypass")) ?
                  TabCompleterBase.filterStartingWith(args[2], Arrays
                      .asList("allow", "deny")) : Collections.emptyList();
        case "add":
          return TabCompleterBase.getOnlinePlayers(args[1]).stream().filter(person -> person != null &&
              !Restrictions.isVanished(Bukkit.getPlayerExact(person))).collect(Collectors.toList());
        case "promote":
        case "remove":
          return ((user.isLeader() || player.hasPermission("partychat.bypass")) && user.isInParty())
              ? TabCompleterBase.filterStartingWith(args[1], PartyChat.partyHandler()
              .getParty(user).getMembers().stream().map(User::getUsername)) : Collections.emptyList();
        case "join":
          return TabCompleterBase.filterStartingWith(args[1], PartyChat.partyHandler().getPartyMap()
              .values().stream().distinct().filter(Party::isPublic).map(Party::getRawName));
        default:
          return Collections.emptyList();
      }
    }
  }

  /**
   * Check if a player can use a certain subcommand. This will check to see if the command requires the player
   * be the party leader, if the command is still on cooldown, and if the command is disabled.
   * @param player The player running the subcommand.
   * @return Whether or not the player may use the subcommand.
   */
  public boolean canUse(Player player) {
    User user = PartyChat.dataHandler().getUser(player);
    if (this.requiresLeader() && !user.isLeader() && !player.hasPermission("partychat.bypass")) {
      if (!user.isInParty())
        sendMessage(player, "not-in-party");
      else
        sendMessage(player, "not-leader");
      return false;
    } else if (cooldownMap.get(player) != null && cooldownMap.get(player).containsKey(this)
        && !cooldownMap.get(player).get(this).isFinished() && !player.hasPermission("partychat.bypass")) {
      sendMessageWithReplacement(player, "cooldown", "%time%", TimeInterval
          .formatTime(cooldownMap.get(player).get(this)
              .getTimeRemaining() * 1000L, false));
      return false;
    } else if (this.isDisabled()) {
      sendMessage(player, "command-disabled");
      return false;
    } else
      return true;
  }

  public static void runTaskLater(int delay, Runnable task) {
    try {
      Bukkit.getScheduler().runTaskLaterAsynchronously(PartyChat.core(), task, delay * 20L);
    } catch (Exception ex) {
      PartyChat.error("Error running async task. Please consider restarting your server and using /pc bugreport");
    }
  }

  public String getName() {
    return name;
  }

  public String getUsage() {
    return usage;
  }

  public String getDescription() {
    return description;
  }

  public boolean requiresLeader() {
    return requiresLeader;
  }

  public boolean anyoneCanUse() {
    return !requiresLeader;
  }

  public int getCooldown() {
    return cooldown;
  }

  public void setCooldown(int cooldown) {
    this.cooldown = cooldown;
  }

  public boolean isDisabled() {
    return disabled;
  }

  public boolean isEnabled() {
    return !disabled;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  public List<String> getAliases() {
    return aliases;
  }

  public static boolean getConfigBoolean(String path) {
    return PartyChat.dataHandler().getConfigBoolean(PartyChat.dataHandler().mainConfig, path);
  }

  public static String getSubCommandUsage(String command) {
    return PartyChat.dataHandler().commandConfig.getString("party-subcommands." + command + ".usage");
  }

  public static String getSubCommandDescription(String command) {
    return PartyChat.dataHandler().commandConfig.getString("party-subcommands." + command + ".description");
  }

  public static boolean getSubCommandDisabled(String command) {
    return PartyChat.dataHandler().commandConfig.getBoolean("party-subcommands." + command + ".disabled");
  }

  public static int getSubCommandCooldown(String command) {
    return PartyChat.dataHandler().commandConfig.getInt("party-subcommands." + command + ".cooldown");
  }

  public static List<String> getSubCommandAliases(String command) {
    return PartyChat.dataHandler().commandConfig.getStringList("party-subcommands." + command + ".aliases")
        .stream().filter(alias -> !alias.equalsIgnoreCase("")).collect(Collectors.toList());
  }
}