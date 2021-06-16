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
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.bukkit.entity.Player;

/**
 * Handles <code>/party disband</code>.
 */
public class PartyDisband extends PartyCommand {

  public PartyDisband() {
    super(
        "disband", getSubCommandUsage("disband"), getSubCommandDescription("disband"),
        true, getSubCommandDisabled("disband"), getSubCommandCooldown("disband"),
        getSubCommandAliases("disband")
    );
  }

  @Override
  public boolean execute(Player player, String[] args, boolean leftServer) {

    Party party = PartyChat.partyHandler().getParty(PartyChat.dataHandler().getUser(player));

    // This should never happen, but I want to know if it does
    if (party == null) {
      PartyChat.error("Error: PC-DIS_1 | The plugin is fine, but please report this error " +
          "code here: https://discord.gg/CGgvDUz");
      sendMessage(player, "error");
      return false;
    }

    // Disband the party
    for (User user : party.getMembers()) {
      user.setPartyID(null);
      user.setInParty(false);
      user.setPartyOnly(false);
      user.setPartyChatToggle(false);
      Player member = user.getPlayer();
      if (member != null && member.isOnline())
        sendMessageWithReplacement(member, "party-disbanded", "%partyName%", party.getName());
    }
    PartyChat.partyHandler().deleteParty(party);
    PartyChat.partyHandler().removeFromPartyMap(party.getId());

    return true;
  }
}