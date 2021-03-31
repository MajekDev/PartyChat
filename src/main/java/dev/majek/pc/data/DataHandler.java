package dev.majek.pc.data;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyChatCommand;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.User;
import dev.majek.pc.data.storage.ConfigUpdater;
import dev.majek.pc.data.object.Language;
import dev.majek.pc.data.storage.YAMLConfig;
import dev.majek.pc.mechanic.Mechanic;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * This class handles all plugin data storage, config file access, and the main config file.
 */
public class DataHandler extends Mechanic {

    public PartyChat instance;
    public int minecraftVersion;
    public MessageType messageType = null;

    // Data
    private final Map<UUID, User> userMap;

    // Configuration
    public FileConfiguration mainConfig;
    public FileConfiguration messages;
    public FileConfiguration commandConfig;
    public boolean debug;
    public boolean disableGuis;
    public boolean persistentParties;
    public boolean blockInappropriateNames;
    public boolean blockInappropriateChat;
    public List<String> censorWords;

    public DataHandler() {
        instance = PartyChat.getCore();
        this.userMap = new HashMap<>();
        this.censorWords = new ArrayList<>();
        wipeOldPlugin();
        updateMainConfig();
    }

    /**
     * This will replace the old config files if they still exist.
     */
    @SuppressWarnings("all")
    public void wipeOldPlugin() {
        File langFolder = new File(instance.getDataFolder() + "/Lang");
        if (!langFolder.exists()) {
            instance.getLogger().log(Level.SEVERE, "PCv3 data folder found! Archiving old config files and " +
                    "creating new data folder... ");
            File oldConfig = new File(instance.getDataFolder(), "config.yml");
            if (oldConfig.exists())
                oldConfig.renameTo(new File(instance.getDataFolder(), "config-old.yml"));
            File oldMessages = new File(instance.getDataFolder(), "messages.yml");
            if (oldMessages.exists())
                oldMessages.renameTo(new File(instance.getDataFolder(), "messages-old.yml"));
            File oldDatabase = new File(instance.getDataFolder(), "parties.db");
            if (oldDatabase.exists())
                oldDatabase.renameTo(new File(instance.getDataFolder(), "database-old.db"));
        }
    }

    /**
     * Initialize the main config file before doing anything else.
     */
    public void updateMainConfig() {
        // Initialize main config
        PartyChat.getCore().saveDefaultConfig();
        File configFile = new File(instance.getDataFolder(), "config.yml");
        try {
            ConfigUpdater.update(instance, "config.yml", configFile, Collections.emptyList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        PartyChat.getCore().reloadConfig();
        mainConfig = PartyChat.getCore().getConfig();
    }

    /**
     * Runs on plugin startup. Get config options and initialize bStats metrics.
     */
    @Override
    public void onStartup() {
        // Minecraft version shenanigans
        String fullVersion = PartyChat.getCore().getServer().getClass().getPackage().getName();
        String substring = fullVersion.substring(fullVersion.lastIndexOf('.') + 1);
        String[] versionSplit = substring.split("_");
        minecraftVersion = Integer.parseInt(versionSplit[1]);

        // Load commands config file
        YAMLConfig commands = new YAMLConfig(PartyChat.getCore(), null, "commands.yml");
        commands.saveDefaultConfig();
        File commandsFile = new File(PartyChat.getCore().getDataFolder(), "commands.yml");
        try {
            ConfigUpdater.update(PartyChat.getCore(), "commands.yml", commandsFile, Collections.emptyList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        commands.reloadConfig();
        commandConfig = commands.getConfig();

        // Set global values defined in main config
        debug = getConfigBoolean(mainConfig, "debug");
        disableGuis = getConfigBoolean(mainConfig, "disable-guis");
        disableGuis = !(minecraftVersion >= 13); // They just don't work in 12 and below
        if (disableGuis)
            PartyChat.log("GUIs have been disabled due to the server's Minecraft version.");
        persistentParties = getConfigBoolean(mainConfig, "persistent-parties");
        blockInappropriateNames = getConfigBoolean(mainConfig, "block-inappropriate-names");
        blockInappropriateChat = getConfigBoolean(mainConfig, "block-inappropriate-names");

        // Censored words stuff
        if (blockInappropriateNames || blockInappropriateChat) {
            censorWords.addAll(getConfigStringList(mainConfig, "blocked-words"));
            if (!(getConfigString(mainConfig, "blocked-words-file").equalsIgnoreCase(""))) {
                File censorFile = new File(PartyChat.getCore().getDataFolder(),
                        getConfigString(mainConfig, "blocked-words-file"));
                if (!censorFile.exists()) {
                    try {
                        InputStream stream = PartyChat.getCore().getResource("censor-words.txt");
                        FileUtils.copyInputStreamToFile(stream, censorFile);
                    } catch (IOException e) {
                        PartyChat.error("Error creating censor-words.txt file.");
                        e.printStackTrace();
                    }
                }
                try (Stream<String> stream = Files.lines(Paths.get(censorFile.toURI()), StandardCharsets.UTF_8)) {
                    stream.forEach(word -> censorWords.add(word));
                } catch (IOException e) {
                    PartyChat.error("Error loading censor words from file: "
                            + getConfigString(mainConfig, "blocked-words-file"));
                    e.printStackTrace();
                }
            }
        }

        // Plugin metrics
        new Metrics(PartyChat.getCore(), 7667);

        PartyChat.log("Finished updating config and lang files.");
    }

    public void postStartup() {
        // Set the message config file based on language from main config
        messages = PartyChat.getLanguageHandler().getLanguage().getMessagesConfig().getConfig();
    }

    /**
     * Reload config files to recognize changes. This is called on /pc reload
     */
    public void reloadConfigs() {
        PartyChat.getCore().reloadConfig();
        mainConfig = PartyChat.getCore().getConfig();

        YAMLConfig commands = new YAMLConfig(PartyChat.getCore(), null, "commands.yml");
        commands.saveDefaultConfig();
        File commandsFile = new File(PartyChat.getCore().getDataFolder(), "commands.yml");
        //createFileIfNotExists(configManager,  en_US_FILE);
        try {
            ConfigUpdater.update(PartyChat.getCore(), "commands.yml", commandsFile, Collections.emptyList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        commands.reloadConfig();
        commandConfig = commands.getConfig();

        // Set global values defined in main config
        debug = getConfigBoolean(mainConfig, "debug");
        disableGuis = getConfigBoolean(mainConfig, "disable-guis");
        disableGuis = !(minecraftVersion >= 13); // They just don't work in 12 and below
        if (disableGuis)
            PartyChat.log("GUIs have been disabled due to the server's Minecraft version.");
        persistentParties = getConfigBoolean(mainConfig, "persistent-parties");

        // Update language if necessary
        if (!PartyChat.getLanguageHandler().getLanguage().getLangID().equals(getConfigString(mainConfig,
                "language"))) {
            setMessages(getConfigString(mainConfig, "language"));
        }
        PartyChat.getLanguageHandler().getLanguage().getMessagesConfig().reloadConfig();
        messages = PartyChat.getLanguageHandler().getLanguage().getMessagesConfig().getConfig();
        PartyCommand.refreshMainConfig();
        PartyChatCommand.refreshMainConfig();
        PartyChat.getCommandHandler().refreshCommands();
        PartyChat.log("Config and lang files were reloaded.");
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        updatePerms(event.getPlayer());
    }

    /**
     * Update a players staff permissions.
     * @param player The player to update.
     */
    public void updatePerms(Player player) {
        User user = getUser(player);
        if (player.hasPermission("partychat.admin") && !user.isStaff()) {
            user.setStaff(true);
            user.setSpyToggle(getConfigBoolean(mainConfig, "auto-spy"));
        } else if (!player.hasPermission("partychat.admin")) {
            user.setStaff(false);
            user.setSpyToggle(false);
        }
    }

    /**
     * Get today's plugin log file.
     * @return Today's log file.
     */
    public File getTodaysLog() {
        return new File(PartyChat.getCore().getDataFolder(), "Logs/" +
                java.time.LocalDate.now().toString() + ".txt");
    }

    /**
     * Log a message to today's log file.
     * @param message Message to log.
     * @param level Log level.
     */
    @SuppressWarnings("all")
    public void logToFile(String message, String level) {
        try {
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            message = "[" + formatter.format(date) + " " + level + "] " + message;
            String fileName = java.time.LocalDate.now().toString();
            File logDir = new File(PartyChat.getCore().getDataFolder(), "Logs/");
            File logFile = new File(PartyChat.getCore().getDataFolder(), "Logs/" + fileName + ".txt");
            if (!logDir.exists())
                logDir.mkdirs();
            if (!logFile.exists())
                logFile.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(logFile, true));
            writer.println(message);
            writer.flush();
            writer.close();
            File[] logFiles = logDir.listFiles();
            long oldestDate = Long.MAX_VALUE;
            File oldestFile = null;
            if (logFiles != null && logFiles.length > 7) {
                for (File f : logFiles)
                    if (f.lastModified() < oldestDate) {
                        oldestDate = f.lastModified();
                        oldestFile = f;
                    }
                if (oldestFile != null) {
                    oldestFile.delete();
                    PartyChat.log("Deleting 1 week+ old file " + oldestFile.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a boolean value from a config file.
     * @param config The config file to get the value from.
     * @param path The path to get the value from.
     * @return Boolean value.
     */
    public boolean getConfigBoolean(FileConfiguration config, String path) {
        try {
            return config.getBoolean(path);
        } catch (NullPointerException ex) {
            String filepath = config == PartyChat.getCore().getConfig() ? "config.yml" :
                    PartyChat.getLanguageHandler().getLanguage().getLangID() + ".yml";
            throw new NullPointerException("Error finding value for path " + path + " in "
                    + filepath + ", did you delete something?");
        }
    }

    /**
     * Get a String value from a config file.
     * @param config The config file to get the value from.
     * @param path The path to get the value from.
     * @return String value.
     */
    public String getConfigString(FileConfiguration config, String path) {
        try {
            return config.getString(path);
        } catch (NullPointerException ex) {
            String filepath = config == PartyChat.getCore().getConfig() ? "config.yml" :
                    PartyChat.getLanguageHandler().getLanguage().getLangID() + ".yml";
            throw new NullPointerException("Error finding value for path " + path + " in "
                    + filepath + ", did you delete something?");
        }
    }

    /**
     * Get an int value from a config file.
     * @param config The config file to get the value from.
     * @param path The path to get the value from.
     * @return Integer value.
     */
    public int getConfigInt(FileConfiguration config, String path) {
        try {
            return config.getInt(path);
        } catch (NullPointerException ex) {
            String filepath = config == PartyChat.getCore().getConfig() ? "config.yml" :
                    PartyChat.getLanguageHandler().getLanguage().getLangID() + ".yml";
            throw new NullPointerException("Error finding value for path " + path + " in "
                    + filepath + ", did you delete something?");
        }
    }

    /**
     * Get a list of strings from a config file.
     * @param config The config file to get the value from.
     * @param path The path to get the value from.
     * @return List of strings.
     */
    public List<String> getConfigStringList(FileConfiguration config, String path) {
        try {
            return config.getStringList(path);
        } catch (NullPointerException ex) {
            String filepath = config == PartyChat.getCore().getConfig() ? "config.yml" :
                    PartyChat.getLanguageHandler().getLanguage().getLangID() + ".yml";
            throw new NullPointerException("Error finding value for path " + path + " in "
                    + filepath + ", did you delete something?");
        }
    }

    /**
     * Set the {@link DataHandler#messages} config file to a different language.
     * If this method fails it's probably because the language ID you're trying to set
     * isn't present in the main config file's supported-languages section.
     * @param lang The language ID to set the plugin language to.
     */
    public void setMessages(String lang) {
        Language language = PartyChat.getLanguageHandler().getLangMap().get(lang);
        if (language != null) {
            PartyChat.getLanguageHandler().setLanguage(language);
            messages = PartyChat.getLanguageHandler().getLanguage().getMessagesConfig().getConfig();
            PartyChat.log("Language set to " + language.getLangID() + ".");
        }
    }

    public User getUser(Player player) {
        return userMap.get(player.getUniqueId());
    }

    public User getUser(UUID uuid) {
        return userMap.get(uuid);
    }

    public Map<UUID, User> getUserMap() {
        return userMap;
    }

    public void addToUserMap(User user) {
        userMap.put(user.getPlayerID(), user);
    }

    public void removeFromUserMap(User user) {
        userMap.remove(user.getPlayerID());
    }

    public enum MessageType {
        COMPONENT,
        BASECOMPONENT,
        RAW
    }
}
