package me.majekdor.partychat.command.party;

import me.majekdor.partychat.PartyChat;
import me.majekdor.partychat.command.CommandParty;
import me.majekdor.partychat.data.Party;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class PartyLeave extends CommandParty {

    public static void execute(Player player, boolean leaveServer) {

        // Check if the player is not in a party
        if (!Party.inParty.containsKey(player.getUniqueId())) {
            if (player.isOnline()) { // This is so no errors are thrown when this is called for a player leaving the server
                sendMessageWithPrefix(player, m.getString("not-in-party")); return;
            }
        }

        Party party = Party.getParty(player);
        party.members.remove(player.getUniqueId()); party.size = party.size - 1;
        Party.inParty.remove(player.getUniqueId());

        if (player.isOnline()) { // This is so no errors are thrown when this is called for a player leaving the server
            sendMessageWithPrefix(player, (m.getString("you-leave") + "")
                    .replace("%partyName%", party.name));
        }

        // Debug
        boolean partyDisbanded = party.size == 0;
        PartyChat.debug(player, leaveServer, partyDisbanded);

        // Get rid of the party if there are no members left in it
        if (party.size == 0) {
            Party.parties.remove(party.name); return;
        }

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
    }
}
