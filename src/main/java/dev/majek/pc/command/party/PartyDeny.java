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
import dev.majek.pc.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Handles <code>/party deny</code>.
 */
public class PartyDeny extends PartyCommand {

  public PartyDeny() {
    super(
        "deny", getSubCommandUsage("deny"), getSubCommandDescription("deny"),
        false, getSubCommandDisabled("deny"), getSubCommandCooldown("deny"),
        getSubCommandAliases("deny")
    );
  }

  @Override
  public boolean execute(Player player, String[] args, boolean leftServer) {

    User user = PartyChat.dataHandler().getUser(player);

    // Player is not in a party
    if (!user.isInParty()) {

      // Check for pending invitations
      Party party = null;
      Player inviter = null;
      for (Party check : PartyChat.partyHandler().getPartyMap().values())
        for (Pair<Player, Player> players : check.getPendingInvitations())
          if (players.getFirst() == player) {
            party = check; inviter = players.getSecond();
            break;
          }

      // Player has no pending invitations
      if (party == null) {
        sendMessage(player, "no-invites");
        return false;
      }

      // Send messages
      Player leader = party.getLeader().getPlayer();
      if (leader != null && leader.isOnline())
        sendMessageWithReplacement(leader, "decline-join", "%player%", user.getNickname());
      if (inviter != null && inviter.isOnline() && inviter != leader)
        sendMessageWithReplacement(inviter, "decline-join", "%player%", user.getNickname());
      sendMessage(player, "you-decline");

      party.removePendingInvitation(player);

      return true;
    }

    // Player is in a party
    else {
      Party party = user.getParty();
      // This should never happen, but I want to know if it does
      if (party == null) {
        PartyChat.error("Error: PC-DNY_1 | The plugin is fine, but please report this error " +
            "code here: https://discord.gg/CGgvDUz");
        sendMessage(player, "error");
        return false;
      }
      Player leader = party.getLeader().getPlayer();

      // Check if the player has a pending summon request
      if (party.getPendingSummons().contains(player)) {
        sendMessage(player, "teleport-denied-player");
        if (leader != null && leader.isOnline() && player != leader)
          sendMessageWithReplacement(leader, "teleport-denied",
              "%player%", user.getNickname());
        party.removePendingSummons(player);
        return true;
      }

      // Check if the player is a leader denying a join request
      else if (party.getPendingJoinRequests().size() > 0) {

        // Only leaders can deny join requests
        if (!player.getUniqueId().equals(party.getLeader().getPlayerID())) {
          sendMessage(player, "in-party");
          return false;
        }

        Player toDeny;
        // Check if the leader doesn't specify a player to accept
        if (args.length == 1) {
          if (party.getPendingJoinRequests().size() == 1) {
            toDeny = party.getPendingJoinRequests().get(0);
          } else {
            sendMessage(player, "specify-player");
            return false;
          }
        } else {
          toDeny = Bukkit.getPlayer(args[1]);
          if (!party.getPendingJoinRequests().contains(toDeny)) {
            sendMessage(player, "no-request");
            return false;
          }
        }

        // This should never happen, but I want to know if it does
        if (toDeny == null) {
          PartyChat.error("Error: PC-DNY_2 | The plugin is fine, but please report this error " +
              "code here: https://discord.gg/CGgvDUz");
          sendMessage(player, "error");
          return false;
        }

        // Send messages
        sendMessage(toDeny, "join-denied");
        sendMessageWithReplacement(player, "deny-join", "%player%", user.getNickname());

        party.removePendingJoinRequest(toDeny);

        return true;
      } else {
        sendMessage(player, "no-usage");
        return false;
      }
    }
  }
}