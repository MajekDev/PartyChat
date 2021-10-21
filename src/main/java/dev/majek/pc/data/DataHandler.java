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
package dev.majek.pc.data;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.majek.pc.PartyChat;
import dev.majek.pc.data.legacy.Database;
import dev.majek.pc.data.legacy.SQLite;
import dev.majek.pc.data.object.User;
import dev.majek.pc.data.object.Language;
import dev.majek.pc.data.storage.YamlConfig;
import dev.majek.pc.mechanic.Mechanic;
import org.apache.commons.io.FileUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles all plugin data storage, config file access, and the main config file.
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
  public FileConfiguration guiConfig;
  public boolean debug;
  public boolean disableGuis;
  public boolean persistentParties;
  public boolean blockInappropriateNames;
  public boolean blockInappropriateChat;
  public boolean useVault;
  public boolean useDisplayNames;
  public List<String> censorWords;

  public boolean migrated = true;

  public DataHandler() {
    instance = PartyChat.core();
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
    if (!langFolder.exists() && instance.getDataFolder().exists()) {
      instance.getLogger().log(Level.SEVERE, "PCv3 data folder found! Archiving old config files and " +
          "creating new data folder... ");
      migrated = false;
      File oldConfig = new File(instance.getDataFolder(), "config.yml");
      if (oldConfig.exists())
        oldConfig.renameTo(new File(instance.getDataFolder(), "config-old.yml"));
      File oldMessages = new File(instance.getDataFolder(), "messages.yml");
      if (oldMessages.exists())
        oldMessages.renameTo(new File(instance.getDataFolder(), "messages-old.yml"));
    }
  }

  /**
   * Initialize the main config file before doing anything else.
   */
  public void updateMainConfig() {
    // Initialize main config
    PartyChat.core().saveDefaultConfig();
    File configFile = new File(instance.getDataFolder(), "config.yml");
    try {
      ConfigUpdater.update(instance, "config.yml", configFile, Collections.emptyList());
    } catch (IOException e) {
      e.printStackTrace();
    }
    PartyChat.core().reloadConfig();
    mainConfig = PartyChat.core().getConfig();
  }

  /**
   * Runs on plugin startup. Get config options and initialize bStats metrics.
   */
  @Override
  public void onStartup() {
    // Minecraft version shenanigans
    String fullVersion = PartyChat.core().getServer().getClass().getPackage().getName();
    String substring = fullVersion.substring(fullVersion.lastIndexOf('.') + 1);
    String[] versionSplit = substring.split("_");
    minecraftVersion = Integer.parseInt(versionSplit[1]);

    reload(false);

    // Migrate database parties to json parties if necessary
    if (!migrated) {
      File oldDatabase = new File(instance.getDataFolder(), "parties.db");
      if (oldDatabase.exists()) {
        migrated = false;
        Database database = new SQLite(PartyChat.core());
        database.load();
        database.getPartyNames();
        database.getParties();
        oldDatabase.renameTo(new File(instance.getDataFolder(), "database-old.db"));
      }
    }

    // Plugin metrics
    new Metrics(PartyChat.core(), 7667);

    PartyChat.log("Finished updating config and lang files.");
  }

  /**
   * Set message config file a little after startup.
   */
  public void postStartup() {
    // Set the message config file based on language from main config
    messages = PartyChat.languageHandler().getLanguage().getMessagesConfig().getConfig();
  }

  public void reload() {
    reload(true);
  }

  /**
   * Reload config files to recognize changes. This is called on /pc reload
   */
  public void reload(boolean updateLang) {
    PartyChat.core().reloadConfig();
    mainConfig = PartyChat.core().getConfig();

    // Load commands config file
    YamlConfig commands = new YamlConfig(PartyChat.core(), "commands.yml");
    commands.saveDefaultConfig();
    File commandsFile = new File(PartyChat.core().getDataFolder(), "commands.yml");
    try {
      ConfigUpdater.update(PartyChat.core(), "commands.yml", commandsFile, Collections.emptyList());
    } catch (IOException e) {
      e.printStackTrace();
    }
    commands.reloadConfig();
    commandConfig = commands.getConfig();

    // Load gui config file
    YamlConfig guis = new YamlConfig(PartyChat.core(), "guis.yml");
    guis.saveDefaultConfig();
    File guisFile = new File(PartyChat.core().getDataFolder(), "guis.yml");
    try {
      ConfigUpdater.update(PartyChat.core(), "guis.yml", guisFile, Collections.emptyList());
    } catch (IOException e) {
      e.printStackTrace();
    }
    guis.reloadConfig();
    guiConfig = guis.getConfig();

    // Register gui toggles
    PartyChat.guiHandler().registerToggles();

    // Set global values defined in main config
    debug = getConfigBoolean(mainConfig, "debug");
    disableGuis = getConfigBoolean(mainConfig, "disable-guis");
    if (minecraftVersion < 13) {
      disableGuis = true;
      PartyChat.log("GUIs have been disabled due to the server's Minecraft version.");
    }
    if (PartyChat.hasVault)
      useVault = getConfigBoolean(mainConfig, "use-vault-names");
    persistentParties = getConfigBoolean(mainConfig, "persistent-parties");
    blockInappropriateNames = getConfigBoolean(mainConfig, "block-inappropriate-names");
    blockInappropriateChat = getConfigBoolean(mainConfig, "block-inappropriate-names");
    useDisplayNames = getConfigBoolean(mainConfig, "use-displaynames");

    // Censored words stuff
    censorWords.clear();
    if (blockInappropriateNames || blockInappropriateChat) {
      censorWords.addAll(getConfigStringList(mainConfig, "blocked-words"));
      if (!(getConfigString(mainConfig, "blocked-words-file").equalsIgnoreCase(""))) {
        File censorFile = new File(PartyChat.core().getDataFolder(),
            getConfigString(mainConfig, "blocked-words-file"));
        if (!censorFile.exists()) {
          try {
            InputStream stream = PartyChat.core().getResource("censor-words.txt");
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

    // Update language if necessary
    if (updateLang) {
      if (!PartyChat.languageHandler().getLanguage().getLangID().equals(getConfigString(mainConfig,
          "language"))) {
        setMessages(getConfigString(mainConfig, "language"));
      }
      PartyChat.languageHandler().getLanguage().getMessagesConfig().reloadConfig();
      messages = PartyChat.languageHandler().getLanguage().getMessagesConfig().getConfig();
      PartyChat.log("Config and lang files were reloaded.");
    }
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
    return new File(PartyChat.core().getDataFolder(), "Logs/" +
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
      File logDir = new File(PartyChat.core().getDataFolder(), "Logs/");
      File logFile = new File(PartyChat.core().getDataFolder(), "Logs/" + fileName + ".txt");
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
      String filepath = config == PartyChat.core().getConfig() ? "config.yml" :
          PartyChat.languageHandler().getLanguage().getLangID() + ".yml";
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
      String filepath = config == PartyChat.core().getConfig() ? "config.yml" :
          PartyChat.languageHandler().getLanguage().getLangID() + ".yml";
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
      String filepath = config == PartyChat.core().getConfig() ? "config.yml" :
          PartyChat.languageHandler().getLanguage().getLangID() + ".yml";
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
      String filepath = config == PartyChat.core().getConfig() ? "config.yml" :
          PartyChat.languageHandler().getLanguage().getLangID() + ".yml";
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
    Language language = PartyChat.languageHandler().getLangMap().get(lang);
    if (language != null) {
      PartyChat.languageHandler().setLanguage(language);
      messages = PartyChat.languageHandler().getLanguage().getMessagesConfig().getConfig();
      PartyChat.log("Language set to " + language.getLangID() + ".");
    }
  }

  /**
   * Get a PartyChat user from a name. This may be null.
   * @param name The user's name.
   * @return User from name.
   */
  @Nullable
  public User getUser(@NotNull String name) {
    List<User> users = userMap.values().stream().filter(user -> user.getUsername()
            .equalsIgnoreCase(name)).collect(Collectors.toList());
    return users.isEmpty() ? null : users.get(0);
  }

  /**
   * Get PartyChat user from map.
   * @param player The player to get a user from.
   * @return User from player.
   */
  public User getUser(@NotNull Player player) {
    return userMap.get(player.getUniqueId());
  }

  /**
   * Get PartyChat user from map. May be null if uuid hasn't joined or been in a party.
   * @param uuid The unique id to get a user from.
   * @return User from unique id.
   */
  @Nullable
  public User getUser(@Nullable UUID uuid) {
    if (uuid == null)
      return null;
    else
      return userMap.get(uuid);
  }

  /**
   * Get PartyChat's user map. Unique ids are tied to {@link User}s.
   * @return UserMap
   */
  public Map<UUID, User> getUserMap() {
    return userMap;
  }

  /**
   * Add a user to the user map.
   * @param user User to add.
   */
  public void addToUserMap(User user) {
    userMap.put(user.getPlayerID(), user);
  }

  /**
   * Remove a user from the user map.
   * @param user User to remove.
   */
  public void removeFromUserMap(User user) {
    userMap.remove(user.getPlayerID());
  }

  public enum MessageType {
    COMPONENT,
    BASECOMPONENT,
    RAW
  }
}