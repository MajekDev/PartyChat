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
package dev.majek.pc.data.object;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.majek.pc.PartyChat;
import dev.majek.pc.data.storage.YamlConfig;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * A language setup in the config with a file ready.
 */
public class Language {

  private YamlConfig messagesConfig;
  private final String langID;


  public Language(String langID) {
    this.langID = langID;
    updateFile();
  }

  public void updateFile() {
    messagesConfig = new YamlConfig(PartyChat.core(), "Lang/" + langID + ".yml");
    messagesConfig.saveDefaultConfig();
    File en_US_FILE = new File(PartyChat.core().getDataFolder(), "Lang/" + langID + ".yml");
    //createFileIfNotExists(configManager,  en_US_FILE);
    if (langID.equalsIgnoreCase("en_US")) {
      try {
        ConfigUpdater.update(PartyChat.core(), "Lang/" + langID + ".yml", en_US_FILE, Collections.emptyList());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    messagesConfig.reloadConfig();
  }

  public YamlConfig getMessagesConfig() {
    return messagesConfig;
  }

  public String getLangID() {
    return langID;
  }

  @SuppressWarnings("all")
  public void createFileIfNotExists(YamlConfig YAMLConfig, File file) {
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