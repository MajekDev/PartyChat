package dev.majek.pc.data;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.object.Language;
import dev.majek.pc.mechanic.Mechanic;

import java.util.*;

/**
 * Handles all plugin languages and their config files.
 */
public class LanguageHandler extends Mechanic {

    private Language language;
    public List<String> supportedLanguages;
    public Map<String, Language> langMap;

    public LanguageHandler() {
        this.langMap = new HashMap<>();
        this.supportedLanguages = new ArrayList<>();
    }

    @Override
    public void onStartup() {
        supportedLanguages.addAll(PartyChat.getDataHandler().mainConfig.getStringList("supported-languages"));
        for (String lang : supportedLanguages)
            langMap.put(lang, new Language(lang));
        // Add en_US if they deleted the supported-languages section
        if (supportedLanguages.isEmpty())
            langMap.put("en_US", new Language("en_US"));
        String userDefinedLang = PartyChat.getDataHandler().getConfigString(PartyChat
                .getDataHandler().mainConfig, "language");
        this.language = langMap.get(userDefinedLang);
        if (language == null) {
            language = langMap.get("en_US");
            PartyChat.error("Unknown language defined in config.yml: " + userDefinedLang);
            PartyChat.log("Defaulting to language: en_US");
        }
        if (!language.getLangID().equals("en_US"))
            PartyChat.log("Language set to " + language.getLangID() + ".");
    }

    /**
     * Get the current plugin {@link Language}.
     * @return Language.
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Set the plugin {@link Language}.
     * @param language The new language.
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * Get the plugin's map of {@link Language}s.
     * @return LangMap
     */
    public Map<String, Language> getLangMap() {
        return langMap;
    }
}
