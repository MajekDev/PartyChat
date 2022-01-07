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
package dev.majek.pc.event;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Handles the player pvp event.
 */
public class PvPEvent extends Mechanic {

  @EventHandler(ignoreCancelled = true)
  public void onPlayerPvP(EntityDamageByEntityEvent event) {
    // Ignore if it's not a player being damaged
    if (!(event.getEntity() instanceof Player))
      return;

    // Handle damage from Citizens NPCs
    if (event.getDamager().hasMetadata("NPC") || event.getEntity().hasMetadata("NPC"))
      return;

    // Handle damage from projectiles
    Player damagerPlayer = event.getDamager() instanceof Player ? (Player) event.getDamager() : null;
    if (damagerPlayer == null && event.getDamager() instanceof Projectile) {
      Projectile p = (Projectile) event.getDamager();
      damagerPlayer = p.getShooter() instanceof Player ? (Player) p.getShooter() : null;
    }

    // Ignore if the player is damaging themselves or an entity shot a projectile at them
    if (damagerPlayer == null || damagerPlayer == event.getEntity() || damagerPlayer.hasMetadata("NPC"))
      return;
    Player attackedPlayer = (Player) event.getEntity();

    User damager = PartyChat.dataHandler().getUser(damagerPlayer);
    User attacked = PartyChat.dataHandler().getUser(attackedPlayer);

    if (damager.isInParty() && attacked.isInParty() && damager.getParty() == attacked.getParty()) {
      Party party = damager.getParty();

      // This should never happen, but I want to know if it does
      if (party == null) {
        PartyChat.error("Error: PC-PVP_1 | The plugin is fine, but please report this error " +
            "code here: https://discord.gg/CGgvDUz");
        PartyChat.messageHandler().sendMessage(damagerPlayer, "error");
        return;
      }

      // Stop event if party friendly fire is off
      if (!party.allowsFriendlyFire()) {
        PartyChat.messageHandler().sendMessage(damagerPlayer, "friendly-fire-not-allowed");
        if (PartyChat.dataHandler().debug)
          PartyChat.debug(attackedPlayer, "Damage prevented due to friendly fire being disabled.");
        event.setCancelled(true);
        event.getEntity().setFireTicks(-1);
      }
    }
  }
}