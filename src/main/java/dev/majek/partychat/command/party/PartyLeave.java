package dev.majek.partychat.command.party;

import dev.majek.partychat.PartyChat;
import dev.majek.partychat.command.CommandParty;
import dev.majek.partychat.data.Party;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class PartyLeave extends CommandParty {

    public static void execute(Player player, boolean leaveServer) {

        // Check if the player is not in a party
        if (!Party.inParty(player)) {
            if (player.isOnline()) { // This is so no errors are thrown when this is called for a player leaving the server
                sendMessageWithPrefix(player, m.getString("not-in-party")); return;
            }
        }

        Party party = Party.getParty(player);
        party.members.remove(player.getUniqueId()); party.size = party.size - 1;
        Party.partyMap.remove(player.getUniqueId());

        if (player.isOnline()) { // This is so no errors are thrown when this is called for a player leaving the server
            sendMessageWithPrefix(player, (m.getString("you-leave") + "")
                    .replace("%partyName%", party.name));
        }

        // Debug
        boolean partyDisbanded = party.size == 0;
        PartyChat.debug(player, leaveServer, partyDisbanded);

        if (partyDisbanded)
            return;

        for (UUID memberUUID : party.members) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member == null) continue;
            sendMessageWithPrefix(member, (m.getString("player-leave") + "")
                    .replace("%player%", player.getDisplayName()));
        }

        // Check if the player leaving is the party leader
        if (player.getUniqueId().equals(party.leader)) {
            Random random  = new Random(); // Assign a new random leader
            party.leader = party.members.get(random.nextInt(party.size));
            Player leader = Bukkit.getPlayer(party.leader);
            if (leader != null)
                sendMessageWithPrefix(Bukkit.getPlayer(party.leader), m.getString("you-leader"));
            for (UUID memberUUID : party.members) {
                Player member = Bukkit.getPlayer(memberUUID);
                if (member == null) continue;
                if (member.getUniqueId() == party.leader) continue;
                sendMessageWithPrefix(member, (m.getString("new-leader") + "")
                        .replace("%player%", Objects.requireNonNull(Bukkit.getOfflinePlayer(party.leader).getName())));
            }
        }

        if (c.getBoolean("party-save-on-update"))
            PartyChat.getDatabase().updateParty(party);
    }
}
