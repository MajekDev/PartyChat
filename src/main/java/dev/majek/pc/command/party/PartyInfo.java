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
import org.bukkit.entity.Player;

/**
 * Handles <code>/party info</code>.
 */
public class PartyInfo extends PartyCommand {

  public PartyInfo() {
    super(
        "info", getSubCommandUsage("info"), getSubCommandDescription("info"),
        false, getSubCommandDisabled("info"), getSubCommandCooldown("info"),
        getSubCommandAliases("info")
    );
  }

  @Override
  public boolean execute(Player player, String[] args, boolean leftServer) {

    User user = PartyChat.dataHandler().getUser(player);

    // Make sure player is in a party
    if (!PartyChat.dataHandler().getUser(player).isInParty()) {
      sendMessage(player, "not-in-party");
      return false;
    }

    Party party = user.getParty();

    // This should never happen, but I want to know if it does
    if (party == null) {
      PartyChat.error("Error: PC-INF_1 | The plugin is fine, but please report this error " +
          "code here: https://discord.gg/CGgvDUz");
      sendMessage(player, "error");
      return false;
    }

    // If the player is in the party by themself -> leader
    if (party.getSize() == 1) {
      sendMessageWithEverything(player, "info-leader", "%partyName%", party.getName(),
          "", "", party.getLeader().getUsername());
      return true;
    }

    // Build member list string
    StringBuilder memberList = new StringBuilder();
    party.getMembers().stream().filter(member -> !member.equals(party.getLeader())).map(User::getUsername)
        .forEach(name -> memberList.append(name).append(", "));
    String cleanList = memberList.toString().trim().substring(0, memberList.toString().length() - 2);

    // Send message
    sendMessageWithEverything(player, "info-members", "%partyName%", party.getName(),
        "%player%", party.getLeader().getUsername(), cleanList);
    return true;
  }
}