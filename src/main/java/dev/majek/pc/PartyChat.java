package dev.majek.pc;

import dev.majek.pc.api.PartyChatAPI;
import dev.majek.pc.command.CommandHandler;
import dev.majek.pc.data.*;
import dev.majek.pc.gui.GuiHandler;
import dev.majek.pc.hooks.PlaceholderAPI;
import dev.majek.pc.mechanic.MechanicHandler;
import dev.majek.pc.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class PartyChat extends JavaPlugin {

    // Class reference
    public static PartyChat instance;
    private final DataHandler dataHandler;
    private final MechanicHandler mechanicHandler;
    private final CommandHandler commandHandler;
    private final GuiHandler guiHandler;
    private final LanguageHandler languageHandler;
    private final PartyHandler partyHandler;
    private final PartyChatAPI partyChatAPI;

    // Hooks
    public static boolean hasEssentials = false;
    public static boolean hasLiteBans = false;
    public static boolean hasAdvancedBan = false;

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
        Bukkit.getConsoleSender().sendMessage(Chat.applyColorCodes("   &b(__)   &e\\___)     &7Last updated &912/29/2020 &7by &bMajekdor"));
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
        if (this.getServer().getPluginManager().isPluginEnabled("AdvancedBan") &&
                this.getServer().getPluginManager().getPlugin("AdvancedBan") != null) {
            log("Hooking into AdvancedBans...");
            hasAdvancedBan = true;
        }
        if (this.getServer().getPluginManager().isPluginEnabled("Essentials") &&
                this.getServer().getPluginManager().getPlugin("Essentials") != null) {
            log("Hooking into Essentials...");
            hasEssentials = true;
        }

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
     * Log an object to console.
     * @param object The object to log.
     */
    public static void log(Object object) {
        instance.getLogger().log(Level.INFO, object.toString());
        getDataHandler().logToFile(object.toString(), "INFO");
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