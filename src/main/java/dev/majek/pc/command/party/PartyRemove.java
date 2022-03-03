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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Handles <code>/party remove</code>.
 */
public class PartyRemove extends PartyCommand {

  public PartyRemove() {
    super(
        "remove", getSubCommandUsage("remove"), getSubCommandDescription("remove"),
        true, getSubCommandDisabled("remove"), getSubCommandCooldown("remove"),
        getSubCommandAliases("remove")
    );
  }

  @Override
  public boolean execute(Player player, String[] args, boolean leftServer) {
    User user = PartyChat.dataHandler().getUser(player);
    Party party = user.getParty();

    // Make sure the player is in a party
    if (!user.isInParty()) {
      sendMessage(player, "not-in-party");
      return false;
    }

    // This should never happen, but I want to know if it does
    if (party == null) {
      PartyChat.error("Error: PC-REM_1 | The plugin is fine, but please report this error " +
          "code here: https://discord.gg/CGgvDUz");
      sendMessage(player, "error");
      return false;
    }

    // Must specify a player to remove
    if (args.length == 1) {
      sendMessage(player, "specify-player");
      return false;
    }

    return execute(player, args[1]);
  }

  public static boolean execute(Player player, String toRemove) {
    User user = PartyChat.dataHandler().getUser(player);
    Party party = user.getParty();

    // This should never happen, but I want to know if it does
    if (party == null) {
      PartyChat.error("Error: PC-REM_2 | The plugin is fine, but please report this error " +
          "code here: https://discord.gg/CGgvDUz");
      PartyChat.messageHandler().sendMessage(player, "error");
      return false;
    }

    // Make sure the user is in the party
    List<User> users = party.getMembers().stream().filter(member -> member.getUsername()
        .equalsIgnoreCase(toRemove)).collect(Collectors.toList());
    if (users.isEmpty()) {
      PartyChat.messageHandler().sendMessage(player, "player-not-in-party");
      return false;
    }
    User target = users.get(0);

    // Player is trying to remove themself
    if (user == target) {
      PartyChat.messageHandler().sendMessage(player, "remove-self");
      return false;
    }

    // Player is trying to remove leader
    if (target.equals(party.getLeader())) {
      PartyChat.messageHandler().sendMessage(player, "remove-leader");
      return false;
    }

    party.removeMember(target);
    target.setPartyID(null);
    target.setInParty(false);
    target.setPartyOnly(false);
    target.setPartyChatToggle(false);

    if (target.isOnline() && target.getPlayer() != null)
      PartyChat.messageHandler().sendMessageWithReplacement(target.getPlayer(), "you-removed",
          "%player%", user.getNickname());

    party.getMembers().stream().map(User::getPlayer).filter(Objects::nonNull).forEach(member ->
        PartyChat.messageHandler().sendMessageWithReplacement(member, "player-removed", "%player%",
            target.getUsername()));

    // Update the database if persistent parties is enabled
    if (PartyChat.dataHandler().persistentParties)
      PartyChat.partyHandler().saveParty(party);
    return true;
  }
}