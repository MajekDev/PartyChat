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
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;

/**
 * Handles integration with Vault.
 */
public class Vault {

  public static String getPlayerDisplayName(Player player) {
    Chat vaultChat = PartyChat.core().getServer().getServicesManager().load(Chat.class);
    if (vaultChat == null) {
      PartyChat.error("Couldn't hook into vault!");
      return null;
    }
    if (PartyChat.core().getConfig().getBoolean("use-vault-chat"))
      return vaultChat.getPlayerPrefix(player) + (PartyChat.dataHandler().useDisplayNames
          ? player.getDisplayName() : player.getName()) + vaultChat.getPlayerSuffix(player);
    else
      return PartyChat.dataHandler().useDisplayNames ? player.getDisplayName() : player.getName();
  }
}