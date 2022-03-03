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
package dev.majek.pc.hooks;

import dev.majek.pc.PartyChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.UUID;

/**
 * Handles miscellaneous vanilla hooks.
 */
public class Vanilla {

  /**
   * Check if a player is vanished
   *
   * @param player the player to check
   * @return true if vanished
   */
  public static boolean isVanished(Player player) {
    try {

      for (MetadataValue meta: player.getMetadata("vanished")) {
        if (meta.asBoolean())
          return true;
      }

      return false;
    } catch (Exception ex) {
      PartyChat.error("Error checking if player is vanished:");
      ex.printStackTrace();
    }
    return false;
  }

  /**
   * Check if a player is vanilla banned
   *
   * @param uuid player's unique id
   * @return true if banned
   */
  public static boolean isVanillaBanned(UUID uuid) {
    return Bukkit.getOfflinePlayer(uuid).isBanned();
  }
}