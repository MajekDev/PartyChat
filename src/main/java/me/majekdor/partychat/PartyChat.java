package me.majekdor.partychat;

import me.majekdor.partychat.command.*;
import me.majekdor.partychat.data.ConfigUpdater;
import me.majekdor.partychat.data.DataManager;
import me.majekdor.partychat.data.Metrics;
import me.majekdor.partychat.data.Party;
import me.majekdor.partychat.gui.GuiHandler;
import me.majekdor.partychat.event.PlayerChat;
import me.majekdor.partychat.event.PlayerJoinLeave;
import me.majekdor.partychat.event.PlayerMove;
import me.majekdor.partychat.hooks.PlaceholderAPI;
import me.majekdor.partychat.sqlite.Database;
import me.majekdor.partychat.sqlite.SQLite;
import me.majekdor.partychat.util.Chat;
import me.majekdor.partychat.util.UpdateChecker;
import me.majekdor.partychat.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public final class PartyChat extends JavaPlugin {

    public static DataManager messageData;
    public static PartyChat instance;
    public static PartyChat getInstance() { return instance; }
    private final GuiHandler guiHandler;
    public static boolean hasUpdate = false;
    public static boolean debug;
    public static List<Player> serverStaff = new ArrayList<>();
    private static Database db;
    public static String minecraftVersion;
    public static boolean disableGuis;
    public static boolean hasEssentials = false;
    public static boolean hasLiteBans = false;
    public static boolean hasAdvancedBan = false;

    public PartyChat() {
        instance = this;
        this.guiHandler = new GuiHandler();
    }

    @Override
    public void onEnable() {
        // Begin loading plugin
        long start = System.currentTimeMillis();

        PluginDescriptionFile pdf = PartyChat.instance.getDescription();
        debug = this.getConfig().getBoolean("debug");
        String a = getServer().getClass().getPackage().getName();
        String mcversion = a.substring(a.lastIndexOf('.') + 1);
        Bukkit.getConsoleSender().sendMessage(Chat.colorize("    &b____   &e___             "));
        Bukkit.getConsoleSender().sendMessage(Chat.colorize("   &b(  _ \\ &e/ __)     &2PartyChat &9v" + pdf.getVersion()));
        Bukkit.getConsoleSender().sendMessage(Chat.colorize("    &b)___/&e( (__      &8Detected Minecraft &9"  + mcversion));
        Bukkit.getConsoleSender().sendMessage(Chat.colorize("   &b(__)   &e\\___)     &8Last updated &911/5/2020 &8by &bMajekdor"));
        Bukkit.getConsoleSender().sendMessage(Chat.colorize(""));

        // Check for plugin update
        Logger logger = this.getLogger();
        new UpdateChecker(this, 79295).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                logger.info("Plugin PartyChat is up to date.");
            } else {
                hasUpdate = true;
                Bukkit.getConsoleSender().sendMessage("");
                Bukkit.getConsoleSender().sendMessage(Chat.colorize("&bThere is a new update available for PartyChat!"));
                Bukkit.getConsoleSender().sendMessage(Chat.colorize("&bDownload it here: &ehttps://www.spigotmc.org/resources/partychat.79295/"));
                Bukkit.getConsoleSender().sendMessage("");
            }
        });

        // Get minecraft version
        String[] versionSplit = mcversion.split("_");
        minecraftVersion = versionSplit[1];

        // Disable guis for versions below 1.12.2
        if (Integer.parseInt(minecraftVersion) < 13)
            disableGuis = true;
        else disableGuis = this.getConfig().getBoolean("disable-guis");
        if (disableGuis) Bukkit.getConsoleSender().sendMessage("[PartyChat] GUIs have been disabled. You can change " +
                "this in the config if your server version is 1.13 or above.");

        // Update config and messages files
        refreshConfigs();

        // Hook into soft dependencies if they exist
        goFishing();

        // Load parties if saved
        if (this.getConfig().getBoolean("persistent-parties")) {
            db = new SQLite(this);
            db.load();
            db.getPartyNames();
            db.getParties();
            db.clearTable();
            Bukkit.getConsoleSender().sendMessage("[PartyChat] Loading saved parties from config...");
        }

        // Register commands, tab completers, and listeners here
        Objects.requireNonNull(this.getCommand("normalchat")).setExecutor(new CommandNormalChat());
        Objects.requireNonNull(this.getCommand("normalchat")).setTabCompleter(new CommandNormalChat());
        Objects.requireNonNull(this.getCommand("party")).setExecutor(new CommandParty());
        Objects.requireNonNull(this.getCommand("party")).setTabCompleter(new CommandParty());
        Objects.requireNonNull(this.getCommand("partychat")).setExecutor(new CommandPartyChat());
        Objects.requireNonNull(this.getCommand("partychat")).setTabCompleter(new CommandPartyChat());
        Objects.requireNonNull(this.getCommand("pcreload")).setExecutor(new CommandReload());
        Objects.requireNonNull(this.getCommand("spy")).setExecutor(new CommandPartySpy());
        this.getServer().getPluginManager().registerEvents(new PlayerMove(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerJoinLeave(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerChat(), this);
        guiHandler.registerHandler();

        // Metrics
        int pluginId = 7667; new Metrics(this, pluginId);

        // Plugin successfully loaded
        Bukkit.getConsoleSender().sendMessage("[PartyChat] Successfully loaded PartyChat version "
                        +  pdf.getVersion() + " in " + (System.currentTimeMillis() - start) + "ms");

        // Prompt to use Paperclip - it's just better :P
        if (Bukkit.getVersion().contains("Spigot")) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                    Bukkit.getConsoleSender().sendMessage("Psst... you're using Spigot, you should really try Paper =D" +
                            "\nhttps://papermc.io/downloads"), 60L); // 3 second delay
        }
    }

    @Override
    public void onDisable() {
        // Save parties if enabled
        if (this.getConfig().getBoolean("persistent-parties")) {
            for (Party party : Party.partyMap.values()) {
                if (this.getConfig().getBoolean("party-save-on-update"))
                    db.updateParty(party);
                else
                    db.addParty(party.name, party.leader,
                            Utils.serializeMembers(party.members), party.size, party.isPublic);
            }
            Bukkit.getConsoleSender().sendMessage("[PartyChat] Saving active parties to config...");
        }
        // Plugin shutdown logic
        Bukkit.getConsoleSender().sendMessage(Chat.colorize("&f[&bParty&eChat&f] &cDisbanding all parties for the night..."));
    }

    public static GuiHandler getGuiHandler() {
        return instance.guiHandler;
    }

    public static Database getDatabase() {
        return db;
    }

    /**
     * Reload both config files (messages.yml and config.yml)
     */
    public static void refreshConfigs() {
        // Update config and messages files
        Bukkit.getConsoleSender().sendMessage(Chat.colorize("[PartyChat] Loading configuration..."));
        instance.saveDefaultConfig();
        File configFile = new File(instance.getDataFolder(), "config.yml"); String[] foo = new String[0];
        try {
            ConfigUpdater.update(instance, "config.yml", configFile, Arrays.asList(foo));
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance.reloadConfig();
        messageData = new DataManager(instance, null, "messages.yml");
        messageData.saveDefaultConfig();
        File messagesFile = new File(instance.getDataFolder(), "messages.yml");
        try {
            ConfigUpdater.update(instance, "messages.yml", messagesFile, Arrays.asList(foo));
        } catch (IOException e) {
            e.printStackTrace();
        }
        messageData.reloadConfig();
    }

    /**
     * Hook into soft dependencies for placeholder, ban, and mute support
     */
    public void goFishing() { // Get hooks lol
        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") &&
                this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Hooking into PlaceholderAPI...");
            new PlaceholderAPI(this).register();
        }
        if (this.getServer().getPluginManager().isPluginEnabled("LiteBans") &&
                this.getServer().getPluginManager().getPlugin("LiteBans") != null) {
            getLogger().info("Hooking into LiteBans...");
            hasLiteBans = true;
        }
        if (this.getServer().getPluginManager().isPluginEnabled("AdvancedBan") &&
                this.getServer().getPluginManager().getPlugin("AdvancedBan") != null) {
            getLogger().info("Hooking into AdvancedBans...");
            hasAdvancedBan = true;
        }
        if (this.getServer().getPluginManager().isPluginEnabled("Essentials") &&
                this.getServer().getPluginManager().getPlugin("Essentials") != null) {
            getLogger().info("Hooking into Essentials...");
            hasEssentials = true;
        }
    }

    /**
     * Used for debugging messages sent to partychat or normal chat
     *
     * @param player    the user executing the command, who to send the debug message to
     * @param fromClass the class where the message is being sent
     * @param partyChat the player's partychat setting
     * @param messageTo the location the chat will be sent, either party or chat
     */
    public static void debug(Player player, String fromClass, boolean partyChat, String messageTo) {
        if (debug) {
            player.sendMessage(Chat.format("&8[&2Debug&8]&f " + fromClass + " | partyChat:" + partyChat + " | Message -> " + messageTo));
            Bukkit.getConsoleSender().sendMessage(Chat.format("&8[&2Debug&8]&f " + fromClass + " | partyChat:" + partyChat + " | Message -> " + messageTo));
        }
    }

    /**
     * Used for debugging persistent parties and party disbands
     *
     * @param player            the player leaving the party
     * @param leaveServer       true if they left the server false if they executed the command
     * @param partyDisbanded    whether or not the party was disbanded
     */
    public static void debug(Player player, boolean leaveServer, boolean partyDisbanded) {
        if (debug) {
            player.sendMessage(Chat.format("&8[&2Debug&8]&f Player leave ? " + leaveServer + " | Party disbanded ? " + partyDisbanded));
            Bukkit.getConsoleSender().sendMessage(Chat.format("&8[&2Debug&8]&f Player leave ? " + leaveServer + " | Party disbanded ? " + partyDisbanded));
        }
    }
}
