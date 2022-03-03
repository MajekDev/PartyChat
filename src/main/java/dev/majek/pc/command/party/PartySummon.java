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
package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.util.Utils;
import org.bukkit.entity.Player;

/**
 * Handles <code>/party summon</code>.
 */
public class PartySummon extends PartyCommand {

  public PartySummon() {
    super(
        "summon", getSubCommandUsage("summon"), getSubCommandDescription("summon"),
        true, getSubCommandDisabled("summon"), getSubCommandCooldown("summon"),
        getSubCommandAliases("summon")
    );
  }

  @Override
  public boolean execute(Player player, String[] args, boolean leftServer) {
    User user = PartyChat.dataHandler().getUser(player);
    Party party = user.getParty();

    // Check if the player is already in a party
    if (!PartyChat.dataHandler().getUser(player).isInParty()) {
      sendMessage(player, "not-in-party");
      return false;
    }

    // This should never happen, but I want to know if it does
    if (party == null) {
      PartyChat.error("Error: PC-SUM_1 | The plugin is fine, but please report this error " +
          "code here: https://discord.gg/CGgvDUz");
      sendMessage(player, "error");
      return false;
    }

    // Make sure the leader is in a safe location
    if (!Utils.isSafe(player.getLocation())) {
      sendMessage(player, "location-unsafe");
      return false;
    }

    // Send summons to all members
    for (User member : party.getMembers()) {
      if (!member.isOnline())
        continue;
      if (member.getPlayer() == player)
        continue;
      for (String string : PartyChat.dataHandler().getConfigStringList(PartyChat
          .dataHandler().messages, "summon-request")) {
        sendFormattedMessage(member.getPlayer(), string.replace("%prefix%", PartyChat.dataHandler()
            .getConfigString(PartyChat.dataHandler().messages, "prefix"))
            .replace("%player%", user.getNickname()));
      }
      party.addPendingSummons(member.getPlayer());
      int timeout = PartyChat.dataHandler().getConfigInt(mainConfig, "summon-expire-time");
      if (timeout != -1) {
        runTaskLater(timeout, () ->  {
          if (party.getPendingSummons().contains(member.getPlayer())) {
            party.removePendingSummons(member.getPlayer());
            sendMessage(member.getPlayer(), "teleport-timeout");
          }
        });
      }
    }
    sendMessage(player, "summon-sent");
    return true;
  }
}