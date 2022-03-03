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
package dev.majek.pc.data.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

/**
 * Used for YAML configuration files.
 */
public class YamlConfig {

  private final JavaPlugin plugin;
  private FileConfiguration dataConfig = null;
  private File configFile = null;
  private final String fileName;

  public YamlConfig(JavaPlugin instance, String fileName) {
    this.plugin = instance;
    this.fileName = fileName;
  }

  public void createFile(String message, String header) {
    reloadConfig();
    saveConfig();
    loadConfig(header);
    if (message != null) {
      this.plugin.getLogger().info(message);
    }
  }

  public void loadConfig(String header) {
    this.dataConfig.options().header(header);
    this.dataConfig.options().copyDefaults(true);
    saveConfig();
  }

  public void reloadConfig() {
    if (this.configFile == null)
      this.configFile = new File(this.plugin.getDataFolder(), this.fileName);
    this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);
    InputStream defaultStream = this.plugin.getResource(this.fileName);
    if (defaultStream != null) {
      YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
      this.dataConfig.setDefaults(defaultConfig);
    }
  }

  public FileConfiguration getConfig() {
    if (this.dataConfig == null)
      reloadConfig();
    return this.dataConfig;
  }

  public void saveConfig() {
    if (this.dataConfig == null || this.configFile == null)
      return;
    try {
      this.getConfig().save(this.configFile);
    } catch (IOException e) {
      this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, e);
    }
  }

  public void saveDefaultConfig() {
    if (this.configFile == null)
      this.configFile = new File(this.plugin.getDataFolder(), fileName);
    if (!this.configFile.exists())
      this.plugin.saveResource(fileName, false);
  }
}