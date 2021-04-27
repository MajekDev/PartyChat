package dev.majek.pc;

import dev.majek.pc.api.PartyChatAPI;
import dev.majek.pc.command.CommandHandler;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.*;
import dev.majek.pc.gui.GuiHandler;
import dev.majek.pc.hooks.PlaceholderAPI;
import dev.majek.pc.mechanic.MechanicHandler;
import dev.majek.pc.util.Chat;
import dev.majek.pc.util.UpdateChecker;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Objects;
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
    private final PartyChatAPI partyChatAPI;

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
     * AdvancedBan has presented a lot of issues and is currently deprecated. PartyChat will not hook into it.
     */
    @Deprecated
    public static boolean hasAdvancedBan;
    /**
     * True if PartyChat is hooked into Vault.
     */
    public static boolean hasVault = false;

    //    <--- Other --->
    private static JDA jda = null;
    public static boolean hasUpdate = false;
    private static String showcaseMessage = "";

    public PartyChat() {
        // DO NOT CHANGE THE ORDER OF THESE
        instance = this;
        this.dataHandler = new DataHandler();
        this.mechanicHandler = new MechanicHandler();
        this.commandHandler = new CommandHandler();
        this.languageHandler = new LanguageHandler();
        this.guiHandler = new GuiHandler();
        this.partyHandler = new PartyHandler();
        this.partyChatAPI = new PartyChatAPI();
    }

    @Override
    public void onEnable() {
        getDataHandler().logToFile("Beginning to load plugin...", "START");

        // Visual stuff
        PluginDescriptionFile pdf = PartyChat.instance.getDescription();
        String fullVersion = getServer().getClass().getPackage().getName();
        String minecraftVersion = fullVersion.substring(fullVersion.lastIndexOf('.') + 1);
        Bukkit.getConsoleSender().sendMessage(Chat.applyColorCodes("    &b____   &e___             "));
        Bukkit.getConsoleSender().sendMessage(Chat.applyColorCodes("   &b(  _ \\ &e/ __)     &2PartyChat &9v" + pdf.getVersion()));
        Bukkit.getConsoleSender().sendMessage(Chat.applyColorCodes("    &b)___/&e( (__      &7Detected Minecraft &9"  + minecraftVersion));
        Bukkit.getConsoleSender().sendMessage(Chat.applyColorCodes("   &b(__)   &e\\___)     &7Last updated &94/26/2021 &7by &bMajekdor"));
        Bukkit.getConsoleSender().sendMessage(Chat.applyColorCodes(""));

        // Register listeners and game mechanics
        getMechanicHandler().registerMechanics();

        // Get plugin hooks if the plugins are enabled
        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") &&
                this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            log("Hooking into PlaceholderAPI...");
            new PlaceholderAPI(this).register();
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

        // Attempt to connect to Discord if enabled
        if (getDataHandler().getConfigBoolean(getDataHandler().mainConfig, "log-to-discord")) {
            try {
                jda = JDABuilder.createDefault(getDataHandler().getConfigString(getDataHandler().mainConfig,
                        "discord-bot-token")).build();
                PartyChat.log("Successfully connected to Discord.");
            } catch (LoginException ex) {
                jda = null;
                StringBuilder error = new StringBuilder();
                error.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append('\n');
                for (StackTraceElement ste : ex.getStackTrace())
                    error.append("    at ").append(ste.toString()).append('\n');
                String errorString = error.toString();
                PartyChat.error("There was an error hooking into Discord!");
                PartyChat.error(errorString);
                ex.printStackTrace();
            }
        }

        // Get PartyChat showcase message from majek.dev
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://majek.dev/pluginresponse").get().build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(Objects.requireNonNull(response.body()).string());
            showcaseMessage = json.get("partychat").toString();
        } catch (ParseException | IOException ignored) { }

        // Send showcase message to console if there is one
        if (showcaseMessage.length() != 0)
            PartyChat.log(ChatColor.BLUE + showcaseMessage);

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
        PartyCommand.sendFormattedMessage(Bukkit.getConsoleSender(), "[PCv4] Testing message type...");

        // Run post startup method from data handler
        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> getDataHandler().postStartup(), 40L);

        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () ->
                log("Successfully loaded PartyChat version " + pdf.getVersion()), 60L);
    }

    @Override
    public void onDisable() {
        getDataHandler().logToFile("Plugin is being disabled...", "STOP");
    }

    /**
     * Get main PartyChat class.
     * @return PartyChat
     */
    public static PartyChat getCore() {
        return instance;
    }

    /**
     * Get PartyChat's data handler. This class deals with config files and data storage.
     * @return DataHandler
     */
    public static DataHandler getDataHandler() {
        return instance.dataHandler;
    }

    /**
     * Get PartyChat's mechanic handler. This class deals with events and other game mechanics.
     * @return MechanicHandler.
     */
    public static MechanicHandler getMechanicHandler() {
        return instance.mechanicHandler;
    }

    /**
     * Get PartyChat's command handler. This class stores the command map and registers commands.
     * @return CommandHandler.
     */
    public static CommandHandler getCommandHandler() {
        return instance.commandHandler;
    }

    /**
     * Get PartyChat's GUI handler. This class stores active GUIs and deals with inventory events.
     * @return GuiHandler
     */
    public static GuiHandler getGuiHandler(){
        return instance.guiHandler;
    }

    /**
     * Get PartyChat's language handler. This class deals with the plugin's language configuration.
     * @return LanguageHandler
     */
    public static LanguageHandler getLanguageHandler() {
        return instance.languageHandler;
    }

    /**
     * Get PartyChat's party handler. This class deals with party saving and loading.
     * @return PartyHandler
     */
    public static PartyHandler getPartyHandler() {
        return instance.partyHandler;
    }

    /**
     * Get the PartyChat API. This class contains helpful API methods.
     * @return PartyChatAPI
     */
    public static PartyChatAPI getAPI() {
        return instance.partyChatAPI;
    }

    /**
     * Get the PartyChat JDA connection if it exists.
     * @return JDA
     */
    @Nullable
    public static JDA getJDA() {
        return jda;
    }

    /**
     * Get the showcase message for PartyChat if there is one.
     * @return Showcase Message
     */
    public static String getShowcaseMessage() {
        return showcaseMessage;
    }

    /**
     * Log an object to console.
     * @param object The object to log.
     */
    public static void log(Object object) {
        instance.getLogger().log(Level.INFO, object.toString());
        getDataHandler().logToFile(object.toString(), "INFO");
    }

    /**
     * Log a message to the Discord channel defined in the config.yml
     * @param message The message to log.
     */
    public static void logToDiscord(String message) {
        TextChannel channel = jda.getTextChannelById(getDataHandler().getConfigString(getDataHandler().mainConfig,
                "discord-logging-channel-id"));
        if (channel == null)
            throw new RuntimeException("Unable to send message to Discord server!");
        channel.sendMessage(message).queue();
    }

    /**
     * Log an error to console.
     * @param object The error to log.
     */
    public static void error(Object object) {
        instance.getLogger().log(Level.SEVERE, object.toString());
        getDataHandler().logToFile(object.toString(), "ERROR");
    }

    /**
     * Send a debug message to console and a player.
     * @param player The player to send the message to.
     * @param string The message to send.
     */
    public static void debug(Player player, String string) {
        if (getDataHandler().debug) {
            instance.getLogger().log(Level.WARNING, "Internal Debug: " + string);
            player.sendMessage("Internal Debug: " + string);
        }
    }
}