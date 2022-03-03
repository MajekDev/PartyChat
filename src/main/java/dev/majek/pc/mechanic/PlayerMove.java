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
import dev.majek.pc.data.object.Bar;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handles the player move event.
 */
public class PlayerMove extends Mechanic {

  /**
   * This checks if the player moves after accepting a summon request.
   * If the player moves then they will not be teleported.
   *
   * @param event the event
   */
  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    // This checks to see if the player actually moved or just moved their head
    if (event.getTo().getBlockX() == event.getFrom().getBlockX() && event.getTo().getBlockY() ==
        event.getFrom().getBlockY() && event.getTo().getBlockZ() == event.getFrom().getBlockZ()) return;

    Player player = event.getPlayer();
    User user = PartyChat.dataHandler().getUser(player);
    if (user.isInParty() && user.isNoMove()) {
      PartyChat.messageHandler().sendMessage(player, "teleport-canceled");
      Bar bar = new Bar();
      bar.removePlayer(player);
      bar.removeBar();

      Party party = user.getParty();

      // This should never happen, but I want to know if it does
      if (party == null) {
        PartyChat.error("Error: PC-MOV_1 | The plugin is fine, but please report this error " +
            "code here: https://discord.gg/CGgvDUz");
        PartyChat.messageHandler().sendMessage(user, "error");
        return;
      }

      User leader = party.getLeader();
      if (leader.isOnline())
        PartyChat.messageHandler().sendMessage(leader, "teleport-canceled-leader");
      party.removePendingSummons(player);
      user.setNoMove(false);
    }
  }
}