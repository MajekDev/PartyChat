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
import litebans.api.Database;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * Handles integration with LiteBans.
 */
public class LiteBans {

  public static boolean muted;

  /**
   * Check if the player is muted by LiteBans
   *
   * @param uuid player's unique id
   * @param ipAddress player's ip address
   * @return true if muted
   */
  public static boolean isLiteBansMuted(UUID uuid, String ipAddress) {
    try {
      muted = false;
      if (PartyChat.hasLiteBans) {
        Bukkit.getScheduler().runTaskAsynchronously(PartyChat.core(), () ->
            muted = Database.get().isPlayerMuted(uuid, ipAddress));
      }
      return muted;
    } catch (Exception ex) {
      PartyChat.error( "Error checking if player is muted by LiteBans:");
      ex.printStackTrace();
      return false;
    }
  }

  public static boolean banned;

  /**
   * Check if the player is banned by LiteBans
   *
   * @param uuid player's unique id
   * @param ipAddress player's ip address
   * @return true if banned
   */
  public static boolean isLiteBansBanned(UUID uuid, String ipAddress) {
    try {
      banned = false;
      if (PartyChat.hasLiteBans) {
        Bukkit.getScheduler().runTaskAsynchronously(PartyChat.core(), () ->
            banned = Database.get().isPlayerBanned(uuid, ipAddress));
      }
      return banned;
    } catch (Exception ex) {
      PartyChat.error("Error checking if player is banned by LiteBans:");
      ex.printStackTrace();
      return false;
    }
  }
}