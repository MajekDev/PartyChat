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
import org.bukkit.entity.Player;

/**
 * Handles <code>/party join</code>.
 */
public class PartyJoin extends PartyCommand {

  public PartyJoin() {
    super(
        "join", getSubCommandUsage("join"), getSubCommandDescription("join"),
        false, getSubCommandDisabled("join"), getSubCommandCooldown("join"),
        getSubCommandAliases("join")
    );
  }

  @Override
  public boolean execute(Player player, String[] args, boolean leftServer) {

    // Check if the player is already in a party
    if (PartyChat.dataHandler().getUser(player).isInParty()) {
      sendMessage(player, "in-party");
      return false;
    }

    // Player needs to specify a party to join
    if (args.length == 1) {
      sendMessage(player, "specify-party");
      return false;
    }
    return execute(player, args[1]);
  }

  public static boolean execute(Player player, String partyName) {
    // Try to find the party from the name
    Party findParty = null;
    for (Party check : PartyChat.partyHandler().getPartyMap().values())
      if (check.getRawName().equalsIgnoreCase(partyName)) {
        findParty = check;
        break;
      }

    // Make sure the specified party exists
    if (findParty == null) {
      PartyChat.messageHandler().sendMessage(player, "unknown-party");
      return false;
    }

    Party party = findParty;

    // Make sure the party is public
    if (!party.isPublic()) {
      PartyChat.messageHandler().sendMessage(player, "party-private");
      return false;
    }

    // Check if the player is blocked from joining the party
    if (party.getBlockedPlayers().contains(player)) {
      PartyChat.messageHandler().sendMessage(player, "join-wait");
      return false;
    }

    // Make sure the party isn't full
    int limit = PartyChat.dataHandler().getConfigInt(PartyChat.dataHandler().mainConfig, "max-party-size");
    if (limit != -1 && party.getSize() >= limit) {
      PartyChat.messageHandler().sendMessage(player, "full-party");
      return false;
    }

    Player leader = party.getLeader().getPlayer();

    // Make sure the leader is online
    if (leader == null) {
      PartyChat.messageHandler().sendMessage(player, "leader-offline");
      return false;
    }

    // Send messages
    for (String request : PartyChat.dataHandler().getConfigStringList(PartyChat
        .dataHandler().messages, "request-join"))
      PartyChat.messageHandler().sendFormattedMessage(leader, request.replace("%prefix%",
          PartyChat.dataHandler().getConfigString(PartyChat.dataHandler().messages, "prefix"))
          .replace("%player%", PartyChat.dataHandler().getUser(player).getNickname()));
    PartyChat.messageHandler().sendMessage(player, "request-sent");

    party.addPendingJoinRequest(player);
    party.addBlockedPlayer(player);

    // Remove them from the blocked list after a configured amount of time
    int blockTime = PartyChat.dataHandler().getConfigInt(mainConfig, "block-time");
    if (blockTime != -1)
      runTaskLater(blockTime, () -> party.removeBlockedPlayer(player));

    // Check after the expire time if the player still hasn't been accepted or declined
    int expireTime = PartyChat.dataHandler().getConfigInt(mainConfig, "join-expire-time");
    if (expireTime != -1) {
      runTaskLater(expireTime, () -> {
        if (party.getPendingJoinRequests().contains(player)) {
          party.removePendingJoinRequest(player);
          PartyChat.messageHandler().sendMessage(player, "expired-join");
        }
      });
    }
    return true;
  }
}