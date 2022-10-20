/*
 * This file is part of PartyChat, licensed under the MIT License.
 *
 * Copyright (c) 2020-2022 Majekdor
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
import dev.majek.pc.data.*;
import dev.majek.pc.gui.GuiHandler;
import dev.majek.pc.hooks.DiscordSRVListener;
import dev.majek.pc.hooks.HeadDatabase;
import dev.majek.pc.hooks.PAPI;
import dev.majek.pc.mechanic.MechanicHandler;
import dev.majek.pc.chat.ChatUtils;
import dev.majek.pc.chat.MessageHandler;
import dev.majek.pc.util.Logging;
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
 * @author Majekdor
 */
public final class PartyChat extends JavaPlugin {

  //    <--- Handlers --->
  private static PartyChat instance;
  private final DataHandler dataHandler;
  private final MechanicHandler mechanicHandler;
  private final CommandHandler commandHandler;
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

  public PartyChat() {
    // DO NOT CHANGE THE ORDER OF THESE
    instance = this;
    this.dataHandler = new DataHandler();
    this.mechanicHandler = new MechanicHandler();
    this.commandHandler = new CommandHandler();
    this.languageHandler = new LanguageHandler();
    this.guiHandler = new GuiHandler();
    this.partyHandler = new PartyHandler();
    this.messageHandler = new MessageHandler();
    this.partyChatApi = new PartyChatApi();
  }

  @Override
  public void onEnable() {
    instance.getLogger().addHandler(new Logging());

    dataHandler().logToFile("Beginning to load plugin...", "START");

    // Visual stuff
    PluginDescriptionFile pdf = PartyChat.instance.getDescription();
    String fullVersion = getServer().getClass().getPackage().getName();
    String minecraftVersion = fullVersion.substring(fullVersion.lastIndexOf('.') + 1);
    Bukkit.getConsoleSender().sendMessage(ChatUtils.applyColorCodes("    &b____   &e___             "));
    Bukkit.getConsoleSender().sendMessage(ChatUtils.applyColorCodes("   &b(  _ \\ &e/ __)     &2PartyChat &9v" + pdf.getVersion()));
    Bukkit.getConsoleSender().sendMessage(ChatUtils.applyColorCodes("    &b)___/&e( (__      &7Detected Minecraft &9"  + minecraftVersion));
    Bukkit.getConsoleSender().sendMessage(ChatUtils.applyColorCodes("   &b(__)   &e\\___)     &7Last updated &99/4/2022 &7by &bMajekdor"));
    Bukkit.getConsoleSender().sendMessage(ChatUtils.applyColorCodes(""));

    // Register listeners and game mechanics
    mechanicHandler().registerMechanics();

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
      mechanicHandler.registerMechanic(new HeadDatabase());
    }

    // Attempt to connect to Discord if enabled
    if (dataHandler().getConfigBoolean(dataHandler().mainConfig, "log-to-discord")) {
      try {
        jda = JDABuilder.createDefault(dataHandler().getConfigString(dataHandler().mainConfig,
            "discord-bot-token")).build();
        PartyChat.log("Successfully connected to Discord.");
      } catch (LoginException ex) {
        jda = null;
        PartyChat.logError(ex, "There was an error hooking into Discord!");
      }
    }

    // Check for PartyChat update
    UpdateChecker updateChecker = new UpdateChecker(this, 79295);
    if (updateChecker.isBehindSpigot()) {
      hasUpdate = true;
      log("There is a new update available! " +
          "Download it here: https://www.spigotmc.org/resources/partychat.79295/");
    } else if (updateChecker.isAheadOfSpigot()) {
      log("Ooh a beta tester. Thank you!");
    }

    // Set message type we'll use based on server software and version
    messageHandler().sendFormattedMessage(Bukkit.getConsoleSender(), "[PCv4] Testing message type...");

    // Run post startup method from data handler
    Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> dataHandler().postStartup(), 80L);

    Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () ->
        log("Successfully loaded PartyChat version " + pdf.getVersion()), 100L);
  }

  @Override
  public void onDisable() {
    dataHandler().logToFile("Plugin is being disabled...", "STOP");
  }

  /**
   * Get main PartyChat class.
   * @return PartyChat
   */
  public static PartyChat core() {
    return instance;
  }

  /**
   * Get PartyChat's data handler. This class deals with config files and data storage.
   * @return DataHandler
   */
  public static DataHandler dataHandler() {
    return instance.dataHandler;
  }

  /**
   * Get PartyChat's mechanic handler. This class deals with events and other game mechanics.
   * @return MechanicHandler.
   */
  public static MechanicHandler mechanicHandler() {
    return instance.mechanicHandler;
  }

  /**
   * Get PartyChat's command handler. This class stores the command map and registers commands.
   * @return CommandHandler.
   */
  public static CommandHandler commandHandler() {
    return instance.commandHandler;
  }

  /**
   * Get PartyChat's GUI handler. This class stores active GUIs and deals with inventory events.
   * @return GuiHandler
   */
  public static GuiHandler guiHandler(){
    return instance.guiHandler;
  }

  /**
   * Get PartyChat's language handler. This class deals with the plugin's language configuration.
   * @return LanguageHandler
   */
  public static LanguageHandler languageHandler() {
    return instance.languageHandler;
  }

  /**
   * Get PartyChat's party handler. This class deals with party saving and loading.
   * @return PartyHandler
   */
  public static PartyHandler partyHandler() {
    return instance.partyHandler;
  }

  /**
   * Get PartyChat's message handler. This class deals with all plugin messages and sending.
   * @return MessageHandler
   */
  public static MessageHandler messageHandler() {
    return instance.messageHandler;
  }

  /**
   * Get the PartyChat API. This class contains helpful API methods.
   * @return PartyChatAPI
   */
  public static PartyChatApi api() {
    return instance.partyChatApi;
  }

  /**
   * Get the PartyChat JDA connection if it exists.
   * @return JDA
   */
  @Nullable
  public static JDA jda() {
    return jda;
  }

  /**
   * Log an object to console.
   * @param object The object to log.
   */
  public static void log(@NotNull Object object) {
    instance.getLogger().log(Level.INFO, object.toString());
  }

  /**
   * Log a message to the Discord channel defined in the config.yml
   * @param message The message to log.
   */
  public static void logToDiscord(@NotNull String message) {
    TextChannel channel = jda.getTextChannelById(dataHandler().getConfigString(dataHandler().mainConfig,
        "discord-logging-channel-id"));
    if (channel == null)
      throw new RuntimeException("Unable to send message to Discord server!");
    channel.sendMessage(message).queue();
  }

  /**
   * Log an error to console.
   * @param object The error to log.
   */
  public static void error(@NotNull Object object) {
    instance.getLogger().log(Level.SEVERE, object.toString());
  }

  /**
   * Send a debug message to console and a player.
   * @param player The player to send the message to.
   * @param string The message to send.
   */
  public static void debug(@Nullable Player player, @NotNull String string) {
    if (dataHandler().debug) {
      instance.getLogger().log(Level.FINE, "Internal Debug: " + string);
      if (player != null) {
        player.sendMessage("Internal Debug: " + string);
      }
    }
  }

  /**
   * Log an exception to console and the plugin's log.
   *
   * @param ex the exception
   * @param message the message to go with the exception
   */
  public static void logError(@NotNull Throwable ex, @Nullable String message) {
    final StringBuilder error = new StringBuilder();
    error.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append('\n');
    for (StackTraceElement ste : ex.getStackTrace()) {
      error.append("    at ").append(ste.toString()).append('\n');
    }
    String errorString = error.toString();
    if (message != null) {
      PartyChat.error(message);
    }
    PartyChat.error(errorString);
  }
}