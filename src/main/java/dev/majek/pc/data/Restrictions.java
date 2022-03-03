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
package dev.majek.pc.data;

import dev.majek.pc.PartyChat;
import dev.majek.pc.hooks.Essentials;
import dev.majek.pc.hooks.LiteBans;
import dev.majek.pc.hooks.Vanilla;
import org.bukkit.entity.Player;

/**
 * Handles checking if a player is banned, muted, vanished, etc.
 */
public class Restrictions {

  /**
   * Check if a player is muted by any of our hooked plugins
   *
   * @param player the player to check
   * @return true if muted
   */
  @SuppressWarnings("ConstantConditions")
  public static boolean isMuted(Player player) {
    boolean muted = false;
    if (PartyChat.hasEssentials)
      muted = Essentials.isEssentialsMuted(player.getUniqueId());
    if (PartyChat.hasLiteBans)
      muted = LiteBans.isLiteBansMuted(player.getUniqueId(), player.getAddress().getHostString());
    //if (PartyChat.hasAdvancedBan)
    //muted = AdvanceBan.isAdvanceBanMuted(player.getUniqueId());
    return muted;
  }

  /**
   * Check if a player is banned by any of our hooked plugins or by vanilla command
   *
   * @param player the player to check
   * @return true if banned
   */
  @SuppressWarnings("ConstantConditions")
  public static boolean isBanned(Player player) {
    boolean banned = false;
    if (PartyChat.hasEssentials)
      banned = Essentials.isEssentialsBanned(player.getUniqueId());
    if (PartyChat.hasLiteBans)
      banned = LiteBans.isLiteBansBanned(player.getUniqueId(), player.getAddress().getHostString());
    //if (PartyChat.hasAdvancedBan)
    //banned = AdvanceBan.isAdvanceBanBanned(player.getUniqueId());
    return banned;
  }

  /**
   * Check if a player is vanished
   *
   * @param player the player to check
   * @return true if vanished
   */
  public static boolean isVanished(Player player) {
    return Vanilla.isVanished(player) || (PartyChat.hasEssentials && Essentials.isEssentialsVanished(player.getUniqueId()));
  }

  public static boolean containsCensoredWord(String string) {
    boolean contains = false;
    for (String censorWord : PartyChat.dataHandler().censorWords) {
      if (string.contains(censorWord)) {
        contains = true;
        break;
      }
    }
    return contains;
  }
}