package dev.majek.pc.mechanic;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import static dev.majek.pc.command.PartyCommand.sendMessage;

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

        User damager = PartyChat.getDataHandler().getUser(damagerPlayer);
        User attacked = PartyChat.getDataHandler().getUser(attackedPlayer);

        if (damager.isInParty() && attacked.isInParty() && damager.getParty() == attacked.getParty()) {
            Party party = damager.getParty();

            // This should never happen, but I want to know if it does
            if (party == null) {
                PartyChat.error("Error: PC-PVP_1 | The plugin is fine, but please report this error " +
                        "code here: https://discord.gg/CGgvDUz");
                sendMessage(damagerPlayer, "error");
                return;
            }

            // Stop event if party friendly fire is off
            if (!party.allowsFriendlyFire()) {
                sendMessage(damagerPlayer, "friendly-fire-not-allowed");
                if (PartyChat.getDataHandler().debug)
                    PartyChat.debug(attackedPlayer, "Damage prevented due to friendly fire being disabled.");
                event.setCancelled(true);
                event.getEntity().setFireTicks(-1);
            }
        }
    }

}
