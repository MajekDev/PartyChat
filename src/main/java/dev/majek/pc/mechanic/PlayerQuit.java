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
package dev.majek.pc.mechanic;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.party.PartyLeave;
import dev.majek.pc.data.Restrictions;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the player quit event.
 */
public class PlayerQuit extends Mechanic {

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    PartyChat.debug(null, "Calling #onPlayerQuit");
    User user = PartyChat.dataHandler().getUser(event.getPlayer().getUniqueId());
    if (user == null) {
      PartyChat.debug(null, "null user, returning...");
      return;
    }

    if (
        user.isInParty() && user.getParty() != null && user.isLeader()
            && !PartyChat.dataHandler().persistentParties
            && PartyChat.dataHandler().getConfigBoolean(PartyChat.dataHandler().mainConfig, "disband-on-leader-leave")
    ) {
      disbandParty(user.getParty());
    }
    else if (user.isInParty() && !PartyChat.dataHandler().getConfigBoolean(PartyChat.dataHandler().mainConfig, "persistent-parties")) {
      PartyLeave.execute(user, null, true);
      PartyChat.debug(null, "Executing PartyLeave#execute");
    }

    // Remove the player from the party if they left the server due to a ban
    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PartyChat.core(), () -> {
      if (Restrictions.isBanned(event.getPlayer()))
        PartyChat.commandHandler().getCommand("leave").execute(user.getPlayer(), new String[0], true);
    }, 20L); // Give hooked plugins time to figure their shit out
  }

  private void disbandParty(@NotNull Party party) {
    // Disband the party
    for (User user : party.getMembers()) {
      user.setPartyID(null);
      user.setInParty(false);
      user.setPartyOnly(false);
      user.setPartyChatToggle(false);
      Player member = user.getPlayer();
      if (member != null && member.isOnline())
        PartyChat.messageHandler().sendMessageWithReplacement(member, "party-disbanded", "%partyName%", party.getName());
    }
    PartyChat.partyHandler().deleteParty(party);
    PartyChat.partyHandler().removeFromPartyMap(party.getId());
  }
}