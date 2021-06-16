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
package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.chat.ChatUtils;
import org.bukkit.entity.Player;

/**
 * Handles <code>/party version</code>.
 */
public class PartyVersion extends PartyCommand {

  public PartyVersion() {
    super(
        "version", getSubCommandUsage("version"), getSubCommandDescription("version"),
        false, getSubCommandDisabled("version"), getSubCommandCooldown("version"),
        getSubCommandAliases("version")
    );
  }

  @Override
  public boolean execute(Player player, String[] args, boolean leftServer) {
    return execute(player);
  }

  public static boolean execute(Player player) {
    for (String message : PartyChat.dataHandler().getConfigStringList(PartyChat.dataHandler().messages, "party-info"))
      PartyChat.messageHandler().sendFormattedMessage(player, ChatUtils.applyColorCodes(message.replace("%prefix%",
          PartyChat.dataHandler().getConfigString(PartyChat.dataHandler().messages, "prefix"))
          .replace("%version%", PartyChat.core().getDescription().getVersion())));
    return true;
  }
}