package dev.majek.partychat.event;

import dev.majek.partychat.PartyChat;
import dev.majek.partychat.command.CommandParty;
import dev.majek.partychat.data.Party;
import dev.majek.partychat.util.Bar;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMove implements Listener {

    public FileConfiguration m = PartyChat.messageData.getConfig();
    public Bar bar;

    /**
     * This checks if the player moves after accepting a summon request.
     * If the player moves then they will not be teleported.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        // This checks to see if the player actually moved or just moved their head
        if (e.getTo().getBlockX() == e.getFrom().getBlockX() && e.getTo().getBlockY() == e.getFrom().getBlockY()
                && e.getTo().getBlockZ() == e.getFrom().getBlockZ()) return;

        Player player = e.getPlayer();Party party = Party.getParty(player);
        if (Party.noMove.contains(player)) {
            CommandParty.sendMessageWithPrefix(player, m.getString("teleport-canceled"));
            Player leader = Bukkit.getPlayer(party.leader);
            if (leader != null)
                CommandParty.sendMessageWithPrefix(leader, (m.getString("teleport-canceled-leader") + "")
                        .replace("%player%", player.getDisplayName()));
            bar = new Bar(); bar.removePlayer(player); bar.removeBar();
            party.pendingSummons.remove(player);
            Party.noMove.remove(player);
        }
    }
}
