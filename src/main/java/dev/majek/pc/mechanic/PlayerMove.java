package dev.majek.pc.mechanic;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.object.Bar;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import static dev.majek.pc.command.PartyCommand.sendMessage;

public class PlayerMove extends Mechanic {

    /**
     * This checks if the player moves after accepting a summon request.
     * If the player moves then they will not be teleported.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // This checks to see if the player actually moved or just moved their head
        if (event.getTo().getBlockX() == event.getFrom().getBlockX() && event.getTo().getBlockY() ==
                event.getFrom().getBlockY() && event.getTo().getBlockZ() == event.getFrom().getBlockZ()) return;

        Player player = event.getPlayer();
        User user = PartyChat.getDataHandler().getUser(player);
        if (user.isInParty() && user.isNoMove()) {
            sendMessage(player, "teleport-canceled");
            Bar bar = new Bar();
            bar.removePlayer(player);
            bar.removeBar();

            Party party = user.getParty();

            // This should never happen, but I want to know if it does
            if (party == null) {
                PartyChat.error("Error: PC-MOV_1 | The plugin is fine, but please report this error " +
                        "code here: https://discord.gg/CGgvDUz");
                sendMessage(user, "error");
                return;
            }

            User leader = party.getLeader();
            if (leader.isOnline())
                sendMessage(leader, "teleport-canceled-leader");
            party.removePendingSummons(player);
            user.setNoMove(false);
        }
    }
}
