package me.majekdor.partychat.command.party;

import me.majekdor.partychat.command.CommandParty;
import me.majekdor.partychat.data.Party;
import org.bukkit.entity.Player;

public class PartyInfo extends CommandParty {

    public static void execute(Player player) {

        // Check if the player is not in a party
        if (!(Party.inParty.containsKey(player))) {
            sendMessageWithPrefix(player, m.getString("not-in-party")); return;
        }

        // Get the party the player is in
        Party party = Party.getParty(player);

        // Check if the player is the only one in the party
        if (party.size == 1) {
            sendMessageWithPrefix(player, (m.getString("info-leader") + party.leader.getDisplayName())
                    .replace("%partyName%", party.name)); return;
        }

        // Build a string of party members
        StringBuilder members = new StringBuilder();
        for (Player member : party.members) {
            if (!Party.isLeader(party, member)) // Don't add to member string if player is leader
                members.append(member.getDisplayName()).append(", ");
        }

        // Clean up string
        String cleanMembers = members.toString().trim();
        cleanMembers = cleanMembers.substring(0, cleanMembers.length()-1);

        // Send player info message
        sendMessageWithPrefix(player, (m.getString("info-members") + cleanMembers)
                .replace("%player%", party.leader.getDisplayName())
                .replace("%partyName%", party.name));
    }
}
