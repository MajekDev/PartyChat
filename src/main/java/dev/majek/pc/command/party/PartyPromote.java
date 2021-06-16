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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Handles <code>/party promote</code>.
 */
public class PartyPromote extends PartyCommand {

  public PartyPromote() {
    super(
        "promote", getSubCommandUsage("promote"), getSubCommandDescription("promote"),
        true, getSubCommandDisabled("promote"), getSubCommandCooldown("promote"),
        getSubCommandAliases("promote")
    );
  }

  @Override
  public boolean execute(Player player, String[] args, boolean leftServer) {
    User user = PartyChat.dataHandler().getUser(player);

    // Must be in a party to use
    if (!user.isInParty()) {
      sendMessage(player, "not-in-party");
      return false;
    }

    Party party = user.getParty();

    // This should never happen, but I want to know if it does
    if (party == null) {
      PartyChat.error("Error: PC-PRO_1 | The plugin is fine, but please report this error " +
          "code here: https://discord.gg/CGgvDUz");
      sendMessage(player, "error");
      return false;
    }

    if (args.length == 1) {
      sendMessage(player, "specify-player");
      return false;
    }

    return execute(player, args[1], false);
  }

  public static boolean execute(Player player, String newLeader, boolean fromGUI) {
    User user = PartyChat.dataHandler().getUser(player);
    Party party = user.getParty();

    // This should never happen, but I want to know if it does
    if (party == null) {
      PartyChat.error("Error: PC-PRO_2 | The plugin is fine, but please report this error " +
          "code here: https://discord.gg/CGgvDUz");
      PartyChat.messageHandler().sendMessage(player, "error");
      return false;
    }

    // Make sure the specified player is in the party
    Player target = Bukkit.getPlayerExact(newLeader);
    if (target == null) {
      if (!fromGUI)
        PartyChat.messageHandler().sendMessage(player, "not-online");
      return false;
    }
    if (!(party.getMembers().stream().map(User::getPlayer).collect(Collectors.toList()).contains(target))) {
      if (!fromGUI)
        PartyChat.messageHandler().sendMessage(player, "player-not-in-party");
      return false;
    }

    // Player is trying to promote themself :P
    if (player == target && !player.hasPermission("partychat.bypass")) {
      if (!fromGUI)
        PartyChat.messageHandler().sendMessage(player, "promote-self");
      return false;
    }

    // Promote player
    party.setLeader(PartyChat.dataHandler().getUser(target));
    PartyChat.messageHandler().sendMessageWithReplacement(target, "you-promoted",
        "%player%", user.getNickname());
    party.getMembers().stream().map(User::getPlayer).filter(Objects::nonNull).filter(p ->
        !p.getUniqueId().equals(party.getLeader().getPlayerID())).forEach(member ->
        PartyChat.messageHandler().sendMessageWithReplacement(member, "new-leader", "%player%",
            PartyChat.dataHandler().getUser(target).getNickname()));

    // Update the database if persistent parties is enabled
    if (PartyChat.dataHandler().persistentParties)
      PartyChat.partyHandler().saveParty(party);
    return true;
  }
}