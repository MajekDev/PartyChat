package dev.majek.pc.data.object;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.storage.YAMLConfig;
import dev.majek.pc.data.storage.ConfigUpdater;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * A language setup in the config with a file ready.
 */
public class Language {

    private YAMLConfig messagesConfig;
    private final String langID;


    public Language(String langID) {
        this.langID = langID;
        updateFile();
    }

    public void updateFile() {
        messagesConfig = new YAMLConfig(PartyChat.getCore(), null, "Lang/" + langID + ".yml");
        messagesConfig.saveDefaultConfig();
        File en_US_FILE = new File(PartyChat.getCore().getDataFolder(), "Lang/" + langID + ".yml");
        //createFileIfNotExists(configManager,  en_US_FILE);
        try {
            ConfigUpdater.update(PartyChat.getCore(), "Lang/" + langID + ".yml", en_US_FILE, Collections.emptyList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        messagesConfig.reloadConfig();
    }

    public YAMLConfig getMessagesConfig() {
        return messagesConfig;
    }

    public String getLangID() {
        return langID;
    }

    @SuppressWarnings("all")
    public void createFileIfNotExists(YAMLConfig YAMLConfig, File file) {
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
                //configManager.saveDefaultConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
