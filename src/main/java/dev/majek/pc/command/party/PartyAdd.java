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
 * Handles <code>/party add</code>.
 */
public class PartyAdd extends PartyCommand {

  public PartyAdd() {
    super(
        "add", getSubCommandUsage("add"), getSubCommandDescription("add"),
        getConfigBoolean("only-leader-can-add"), getSubCommandDisabled("add"),
        getSubCommandCooldown("add"), getSubCommandAliases("add")
    );
  }

  @Override
  public boolean execute(Player player, String[] args, boolean leftServer) {

    // Make sure the player is actually in a party
    if (!PartyChat.dataHandler().getUser(player).isInParty()) {
      sendMessage(player, "not-in-party");
      return false;
    }

    // Make sure the player specifies the player they wish to invite
    if (args.length == 1) {
      sendMessage(player, "specify-player");
      return false;
    }

    return execute(player, args[1]);
  }

  public static boolean execute(Player player, String name) {

    User user = PartyChat.dataHandler().getUser(player);
    Party party = user.getParty();

    // This should never happen, but I want to know if it does
    if (party == null) {
      PartyChat.error("Error: PC-ADD_1 | The plugin is fine, but please report this error " +
          "code here: https://discord.gg/CGgvDUz");
      PartyChat.messageHandler().sendMessage(player, "error");
      return false;
    }

    // Try to get specified player to invite
    Player invited = Bukkit.getPlayer(name);
    if (invited == null) {
      PartyChat.messageHandler().sendMessage(player, "not-online");
      return false;
    }

    // Player did /party add <their name>
    if (invited == player) {
      PartyChat.messageHandler().sendMessage(player, "add-self");
      return false;
    }

    User invitedUser = PartyChat.dataHandler().getUser(invited);

    // Check if the player is trying to invite someone who is already in the party
    for (User partyMember : party.getMembers()) {
      Player member = partyMember.getPlayer();
      if (member == invited) {
        PartyChat.messageHandler().sendMessage(player, "player-in-party");
        return false;
      }
    }

    // Make sure the party isn't full
    int limit = PartyChat.dataHandler().getConfigInt(PartyChat.dataHandler().mainConfig, "max-party-size");
    if (limit != -1 && party.getSize() >= limit) {
      PartyChat.messageHandler().sendMessage(player, "full-party");
      return false;
    }

    // Passed all checks, send messages
    for (String message : PartyChat.dataHandler().getConfigStringList(PartyChat
        .dataHandler().messages, "invite-message")) {
      PartyChat.messageHandler().sendFormattedMessage(invited, message.replace("%prefix%",
          PartyChat.dataHandler().getConfigString(PartyChat.dataHandler().messages, "prefix"))
          .replace("%partyName%", party.getRawName()).replace("%player%", user.getNickname()));
    }
    PartyChat.messageHandler().sendMessageWithReplacement(player, "invite-sent",
        "%player%", invitedUser.getNickname());
    Player leader = party.getLeader().getPlayer();
    if (leader != null && leader.isOnline() && player != leader)
      PartyChat.messageHandler().sendMessageWithReplacement(leader, "invite-sent",
          "%player%", invitedUser.getNickname());
    party.addPendingInvitation(invited, player);

    // Check after the expire time if the player still hasn't accepted or declined
    int expireTime = PartyChat.dataHandler().getConfigInt(mainConfig, "invite-expire-time");
    if (expireTime != -1) {
      runTaskLater(expireTime, () -> {
        for (Pair<Player, Player> players : party.getPendingInvitations())
          if (players.getFirst() == invited) {
            party.removePendingInvitation(invited);
            PartyChat.messageHandler().sendMessage(invited, "expired-invite");
          }
      });
    }
    return true;
  }
}