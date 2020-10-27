package me.majekdor.partychat;

import me.majekdor.partychat.command.CommandParty;
import me.majekdor.partychat.command.CommandPartyChat;
import me.majekdor.partychat.command.CommandPartySpy;
import me.majekdor.partychat.command.CommandReload;
import me.majekdor.partychat.data.ConfigUpdater;
import me.majekdor.partychat.data.DataManager;
import me.majekdor.partychat.data.Metrics;
import me.majekdor.partychat.gui.GuiHandler;
import me.majekdor.partychat.listener.PlayerChat;
import me.majekdor.partychat.listener.PlayerJoinLeave;
import me.majekdor.partychat.listener.PlayerMove;
import me.majekdor.partychat.util.Chat;
import me.majekdor.partychat.util.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public final class PartyChat extends JavaPlugin {

    public static DataManager messageData;
    public static PartyChat instance;
    public static PartyChat getInstance() { return instance; }
    private final GuiHandler guiHandler;
    public static boolean hasUpdate = false;
    public static boolean debug;
    public static List<Player> serverStaff = new ArrayList<>();

    public PartyChat() {
        instance = this;
        this.guiHandler = new GuiHandler();
    }

    @Override
    public void onEnable() {
        // Begin loading plugin
        PluginDescriptionFile pdf = PartyChat.instance.getDescription();
        debug = this.getConfig().getBoolean("debug");
        String a = getServer().getClass().getPackage().getName();
        String mcversion = a.substring(a.lastIndexOf('.') + 1);
        Bukkit.getConsoleSender().sendMessage(Chat.colorize("    &b____   &e___             "));
        Bukkit.getConsoleSender().sendMessage(Chat.colorize("   &b(  _ \\ &e/ __)     &2PartyChat &9v" + pdf.getVersion()));
        Bukkit.getConsoleSender().sendMessage(Chat.colorize("    &b)___/&e( (__      &8Detected Minecraft &9"  + mcversion));
        Bukkit.getConsoleSender().sendMessage(Chat.colorize("   &b(__)   &e\\___)     &8Last updated &910/15/2020 &8by &bMajekdor"));
        Bukkit.getConsoleSender().sendMessage(Chat.colorize(""));
        Bukkit.getConsoleSender().sendMessage(Chat.colorize("[PartyChat] Loading configuration..."));

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

        // Update config and messages files
        this.saveDefaultConfig();
        File configFile = new File(getDataFolder(), "config.yml"); String[] foo = new String[0];
        try {
            ConfigUpdater.update(instance, "config.yml", configFile, Arrays.asList(foo));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.reloadConfig();
        messageData = new DataManager(instance, null, "messages.yml");
        messageData.saveDefaultConfig();
        File messagesFile = new File(getDataFolder(), "messages.yml");
        try {
            ConfigUpdater.update(instance, "messages.yml", messagesFile, Arrays.asList(foo));
        } catch (IOException e) {
            e.printStackTrace();
        }
        messageData.reloadConfig();

        // Register commands, tab completers, and listeners here
        Objects.requireNonNull(this.getCommand("party")).setExecutor(new CommandParty());
        Objects.requireNonNull(this.getCommand("party")).setTabCompleter(new CommandParty());
        Objects.requireNonNull(this.getCommand("partychat")).setExecutor(new CommandPartyChat());
        Objects.requireNonNull(this.getCommand("pcreload")).setExecutor(new CommandReload());
        Objects.requireNonNull(this.getCommand("spy")).setExecutor(new CommandPartySpy());
        this.getServer().getPluginManager().registerEvents(new PlayerMove(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerJoinLeave(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerChat(), this);
        guiHandler.registerHandler();

        // Metrics
        int pluginId = 7667; new Metrics(this, pluginId);

        // Plugin successfully loaded
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                Bukkit.getConsoleSender().sendMessage("[PartyChat] Successfully loaded PartyChat version "
                        +  pdf.getVersion()), 60L); // 3 second delay
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getConsoleSender().sendMessage(Chat.colorize("&f[&bParty&eChat&f] &cDisbanding all parties for the night..."));
    }

    public static GuiHandler getGuiHandler() {
        return instance.guiHandler;
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
}
