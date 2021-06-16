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
    supportedLanguages.addAll(PartyChat.dataHandler().mainConfig.getStringList("supported-languages"));
    for (String lang : supportedLanguages)
      langMap.put(lang, new Language(lang));
    // Add en_US if they deleted the supported-languages section
    if (supportedLanguages.isEmpty())
      langMap.put("en_US", new Language("en_US"));
    String userDefinedLang = PartyChat.dataHandler().getConfigString(PartyChat
        .dataHandler().mainConfig, "language");
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