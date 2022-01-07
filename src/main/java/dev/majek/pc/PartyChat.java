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
package dev.majek.pc;

import dev.majek.pc.api.PartyChatApi;
import dev.majek.pc.command.CommandHandler;
import dev.majek.pc.command.CommandManager;
import dev.majek.pc.data.*;
import dev.majek.pc.gui.GuiHandler;
import dev.majek.pc.hooks.DiscordSRVListener;
import dev.majek.pc.hooks.HeadDatabase;
import dev.majek.pc.hooks.PAPI;
import dev.majek.pc.event.MechanicHandler;
import dev.majek.pc.message.ChatUtils;
import dev.majek.pc.message.MessageHandler;
import dev.majek.pc.util.UpdateChecker;
import github.scarsz.discordsrv.DiscordSRV;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.security.auth.login.LoginException;
import java.util.logging.Level;

/**
 * Main plugin class. All handlers should be obtained from here.
 *
 * @author Majekdor
 */
public final class PartyChat extends JavaPlugin {

  //    <--- Handlers --->
  private static PartyChat core;
  private final DataHandler dataHandler;
  private final MechanicHandler mechanicHandler;
  private final CommandHandler commandHandler;
  private CommandManager commandManager;
  private final GuiHandler guiHandler;
  private final LanguageHandler languageHandler;
  private final PartyHandler partyHandler;
  private final MessageHandler messageHandler;
  private final PartyChatApi partyChatApi;

  //    <--- Hooks --->
  /**
   * True if PartyChat is hooked into Essentials.
   */
  public static boolean hasEssentials = false;
  /**
   * True if PartyChat is hooked into LiteBans.
   */
  public static boolean hasLiteBans = false;
  /**
   * True if PartyChat is hooked into Vault.
   */
  public static boolean hasVault = false;
  /**
   * True if PartyChat is hooked into PlaceholderAPI.
   */
  public static boolean hasPapi = false;
  /**
   * True if PartyChat is hooked into DiscordSRV.
   */
  public static boolean hasDiscordSRV = false;
  /**
   * True if PartyChat is hooked into HeadDatabase.
   */
  public static boolean hasHDB = false;

  //    <--- Other --->
  private static JDA jda = null;
  public static boolean hasUpdate = false;

  public PartyChat() throws Exception {
    // DO NOT CHANGE THE ORDER OF THESE
    core = this;
    this.dataHandler = new DataHandler();
    this.mechanicHandler = new MechanicHandler();
    this.commandHandler = new CommandHandler();
    this.languageHandler = new LanguageHandler();
    this.guiHandler = new GuiHandler();
    this.partyHandler = new PartyHandler();
    this.messageHandler = new MessageHandler();
    this.partyChatApi = new PartyChatApi();
  }

  /**
   * Plugin startup logic.
   */
  @Override
  public void onEnable() {
    this.dataHandler.logToFile("Beginning to load plugin...", "START");

    // Visual stuff
    PluginDescriptionFile pdf = this.getDescription();
    String fullVersion = this.getServer().getClass().getPackage().getName();
    String minecraftVersion = fullVersion.substring(fullVersion.lastIndexOf('.') + 1);
    Bukkit.getConsoleSender().sendMessage(ChatUtils.applyColorCodes("    &b____   &e___             "));
    Bukkit.getConsoleSender().sendMessage(ChatUtils.applyColorCodes("   &b(  _ \\ &e/ __)     &2PartyChat &9v" + pdf.getVersion()));
    Bukkit.getConsoleSender().sendMessage(ChatUtils.applyColorCodes("    &b)___/&e( (__      &7Detected Minecraft &9"  + minecraftVersion));
    Bukkit.getConsoleSender().sendMessage(ChatUtils.applyColorCodes("   &b(__)   &e\\___)     &7Last updated &912/20/2021 &7by &bMajekdor"));
    Bukkit.getConsoleSender().sendMessage(ChatUtils.applyColorCodes(""));

    // Register listeners and game mechanics
    this.mechanicHandler.registerMechanics();

    // Register commands
    try {
      this.commandManager = new CommandManager();
      this.commandManager.registerCommands();
    } catch (final Exception ex) {
      error("Failed to register commands:", ex);
    }

    // Get plugin hooks if the plugins are enabled
    if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") &&
        this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
      log("Hooking into PlaceholderAPI...");
      new PAPI(this).register();
      hasPapi = true;
    }
    if (this.getServer().getPluginManager().isPluginEnabled("LiteBans") &&
        this.getServer().getPluginManager().getPlugin("LiteBans") != null) {
      log("Hooking into LiteBans...");
      hasLiteBans = true;
    }
    if (this.getServer().getPluginManager().isPluginEnabled("Essentials") &&
        this.getServer().getPluginManager().getPlugin("Essentials") != null) {
      log("Hooking into Essentials...");
      hasEssentials = true;
    }
    if (this.getServer().getPluginManager().isPluginEnabled("Vault") &&
        this.getServer().getPluginManager().getPlugin("Vault") != null) {
      log("Hooking into Vault...");
      hasVault = true;
    }
    if (this.getServer().getPluginManager().isPluginEnabled("DiscordSRV") &&
        this.getServer().getPluginManager().getPlugin("DiscordSRV") != null) {
      log("Hooking into DiscordSRV...");
      hasDiscordSRV = true;
      DiscordSRV.api.subscribe(new DiscordSRVListener());
    }
    if (this.getServer().getPluginManager().isPluginEnabled("HeadDatabase") &&
        this.getServer().getPluginManager().getPlugin("HeadDatabase") != null) {
      log("Hooking into HeadDatabase...");
      hasHDB = true;
      this.mechanicHandler.registerMechanic(new HeadDatabase());
    }

    // Attempt to connect to Discord if enabled
    if (this.dataHandler.getConfigBoolean(this.dataHandler.mainConfig, "log-to-discord")) {
      try {
        jda = JDABuilder.createDefault(dataHandler().getConfigString(dataHandler().mainConfig,
            "discord-bot-token")).build();
        PartyChat.log("Successfully connected to Discord.");
      } catch (LoginException ex) {
        jda = null;
        error("There was an error hooking into Discord!", ex);
      }
    }

    // Check for PartyChat update
    /*
    final UpdateChecker updateChecker = new UpdateChecker(this, 79295);
    if (updateChecker.isBehindSpigot()) {
      hasUpdate = true;
      log("There is a new update available! " +
          "Download it here: https://www.spigotmc.org/resources/partychat.79295/");
    } else if (updateChecker.isAheadOfSpigot()) {
      log("Thank you for being a beta tester!");
    }
     */

    // Set message type we'll use based on server software and version
    this.messageHandler.sendFormattedMessage(Bukkit.getConsoleSender(), "[PCv4] Testing message type...");

    // Run post startup method from data handler
    Bukkit.getScheduler().scheduleSyncDelayedTask(this, this.dataHandler::postStartup, 40L);

    Bukkit.getScheduler().scheduleSyncDelayedTask(this, () ->
        log("Successfully loaded PartyChat version " + this.getDescription().getVersion()), 60L);
  }

  /**
   * Plugin shutdown logic.
   */
  @Override
  public void onDisable() {
    this.dataHandler.logToFile("Plugin is being disabled...", "STOP");
  }

  /**
   * Get main PartyChat class.
   *
   * @return plugin instance
   */
  public static @NotNull PartyChat core() {
    return core;
  }

  /**
   * Get PartyChat's data handler. This class deals with config files and data storage.
   *
   * @return data handler
   */
  public static @NotNull DataHandler dataHandler() {
    return core.dataHandler;
  }

  /**
   * Get PartyChat's mechanic handler. This class deals with events and other game mechanics.
   *
   * @return mechanic handler
   */
  public static @NotNull MechanicHandler mechanicHandler() {
    return core.mechanicHandler;
  }

  /**
   * Get PartyChat's command handler. This class stores the command map and registers commands.
   *
   * @return command handler
   */
  public static @NotNull CommandHandler commandHandler() {
    return core.commandHandler;
  }

  public static @NotNull CommandManager commandManager() {
    return core.commandManager;
  }

  /**
   * Get PartyChat's GUI handler. This class stores active GUIs and deals with inventory events.
   *
   * @return gui handler
   */
  public static @NotNull GuiHandler guiHandler(){
    return core.guiHandler;
  }

  /**
   * Get PartyChat's language handler. This class deals with the plugin's language configuration.
   *
   * @return language handler
   */
  public static @NotNull LanguageHandler languageHandler() {
    return core.languageHandler;
  }

  /**
   * Get PartyChat's party handler. This class deals with party saving and loading.
   *
   * @return party handler
   */
  public static @NotNull PartyHandler partyHandler() {
    return core.partyHandler;
  }

  /**
   * Get PartyChat's message handler. This class deals with all plugin messages and sending.
   *
   * @return message handler
   */
  public static @NotNull MessageHandler messageHandler() {
    return core.messageHandler;
  }

  /**
   * Get the PartyChat API. This class contains helpful API methods.
   *
   * @return plugin api
   */
  public static @NotNull PartyChatApi api() {
    return core.partyChatApi;
  }

  /**
   * Get the PartyChat JDA connection if it exists.
   *
   * @return JDA
   */
  public static @Nullable JDA jda() {
    return jda;
  }

  /**
   * Log an object to console.
   *
   * @param object the object to log
   */
  public static void log(@NotNull Object object) {
    core.getLogger().log(Level.INFO, object.toString());
    dataHandler().logToFile(object.toString(), "INFO");
  }

  /**
   * Log a message to the Discord channel defined in the config.yml
   *
   * @param message the message to log
   */
  public static void logToDiscord(@NotNull String message) {
    TextChannel channel = jda.getTextChannelById(dataHandler().getConfigString(dataHandler().mainConfig,
        "discord-logging-channel-id"));
    if (channel == null) {
      error("Unable to send message to Discord server!", new RuntimeException());
      return;
    }
    channel.sendMessage(message).queue();
  }

  /**
   * Log an error to the console.
   *
   * @param object the error to log
   */
  public static void error(@NotNull Object object) {
    core.getLogger().log(Level.SEVERE, object.toString());
    dataHandler().logToFile(object.toString(), "ERROR");
  }

  /**
   * Log an error and an exception to the console.
   *
   * @param message the error message
   * @param ex the exception
   */
  public static void error(@NotNull String message, @NotNull Exception ex) {
    StringBuilder error = new StringBuilder();
    error.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append('\n');
    for (StackTraceElement ste : ex.getStackTrace())
      error.append("    at ").append(ste.toString()).append('\n');
    error(message);
    error(error.toString());
  }

  /**
   * Send a debug message to the console and a player.
   *
   * @param player the player to send the message to
   * @param message the message to send
   */
  public static void debug(@Nullable Player player, @NotNull String message) {
    if (dataHandler().debug) {
      core.getLogger().log(Level.WARNING, message);
      if (player != null) {
        player.sendMessage("Plugin Debug: " + message);
      }
    }
  }
}
