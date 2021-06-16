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
import dev.majek.pc.api.PartyLeaveEvent;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Random;

/**
 * Handles <code>/party leave</code>.
 */
public class PartyLeave extends PartyCommand {

  public PartyLeave() {
    super(
        "leave", getSubCommandUsage("leave"), getSubCommandDescription("leave"),
        false, getSubCommandDisabled("leave"), getSubCommandCooldown("leave"),
        getSubCommandAliases("leave")
    );
  }

  @Override
  public boolean execute(Player player, String[] args, boolean leftServer) {
    return execute(PartyChat.dataHandler().getUser(player), leftServer);
  }

  public static synchronized boolean execute(User user, boolean leftServer) {
    Player player = user.getPlayer();

    // Player can only leave a party if they're in one
    if (!user.isInParty() && !leftServer) {
      PartyChat.messageHandler().sendMessage(player, "not-in-party");
      return false;
    }

    Party party = user.getParty();

    // This should never happen, but I want to know if it does
    if (party == null || player == null) {
      PartyChat.error("Error: PC-LEV_1 | The plugin is fine, but please report this error " +
          "code here: https://discord.gg/CGgvDUz");
      PartyChat.messageHandler().sendMessage(player, "error");
      return false;
    }

    boolean partyDisbanded = party.getSize() == 1;
    User newLeader = null;
    if (!partyDisbanded)
      newLeader = party.getMembers().get(new Random().nextInt(party.getSize()));

    // Call party leave event
    PartyLeaveEvent event = new PartyLeaveEvent(player, party, newLeader);
    PartyChat.core().getServer().getPluginManager().callEvent(event);
    if (event.isCancelled())
      return true;

    if (!leftServer)
      PartyChat.messageHandler().sendMessageWithReplacement(player, "you-leave", "%partyName%", party.getName());

    party.removeMember(user);
    if (partyDisbanded)
      PartyChat.partyHandler().removeFromPartyMap(user.getPartyID());
    user.setPartyID(null);
    user.setInParty(false);
    user.setPartyOnly(false);
    user.setPartyChatToggle(false);

    if (!partyDisbanded) {
      party.getMembers().stream().map(User::getPlayer).filter(Objects::nonNull).forEach(member ->
          PartyChat.messageHandler().sendMessageWithReplacement(member, "player-leave", "%player%", user.getNickname()));

      // Check if the player who left was the leader
      if (user.equals(party.getLeader())) {
        party.setLeader(event.getNewLeader());
        Player leader = party.getLeader().getPlayer();
        if (leader != null)
          PartyChat.messageHandler().sendMessage(leader, "you-leader");
        party.getMembers().stream().map(User::getPlayer).filter(Objects::nonNull).filter(p ->
            !user.equals(party.getLeader())).forEach(member -> PartyChat.messageHandler()
            .sendMessageWithReplacement(member, "new-leader", "%player%",
                party.getLeader().getUsername()));
      }
    }

    // Update the database if persistent parties is enabled
    if (PartyChat.dataHandler().persistentParties)
      if (partyDisbanded)
        PartyChat.partyHandler().deleteParty(party);
      else
        PartyChat.partyHandler().saveParty(party);
    return true;
  }
}