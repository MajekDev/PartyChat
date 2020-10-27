package me.majekdor.partychat.command.party;

import me.majekdor.partychat.command.CommandParty;
import me.majekdor.partychat.data.Party;
import org.bukkit.entity.Player;

import java.util.Random;

public class PartyLeave extends CommandParty {

    public static void execute(Player player) {

        // Check if the player is not in a party
        if (!Party.inParty.containsKey(player)) {
            if (player.isOnline()) { // This is so no errors are thrown when this is called for a player leaving the server
                sendMessageWithPrefix(player, m.getString("not-in-party")); return;
            }
        }

        Party party = Party.getParty(player);
        party.members.remove(player); party.size -= 1;
        Party.inParty.remove(player);

        if (player.isOnline()) { // This is so no errors are thrown when this is called for a player leaving the server
            sendMessageWithPrefix(player, (m.getString("you-leave") + "")
                    .replace("%partyName%", party.name));
        }

        // Get rid of the party if there are no members left in it
        if (party.size == 0) {
            Party.parties.remove(party.name); return;
        }

        for (Player member : party.members)
            sendMessageWithPrefix(member, (m.getString("player-leave") + "")
                    .replace("%player%", player.getDisplayName()));

        // Check if the player leaving is the party leader
        if (player == party.leader) {
            Random random  = new Random(); // Assign a new random leader
            party.leader = party.members.get(random.nextInt(party.size));
            sendMessageWithPrefix(party.leader, m.getString("you-leader"));
            for (Player member : party.members) {
                if (member == party.leader) continue;
                sendMessageWithPrefix(member, (m.getString("new-leader") + "")
                        .replace("%player%", party.leader.getDisplayName()));
            }
        }
    }
}
