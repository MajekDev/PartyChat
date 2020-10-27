package me.majekdor.partychat.command.party;

import me.majekdor.partychat.command.CommandParty;
import me.majekdor.partychat.data.Party;
import org.bukkit.entity.Player;

public class PartyDisband extends CommandParty {

    public static void execute(Player player) {

        // Check if the player is not in a party
        if (!Party.inParty.containsKey(player)) {
            sendMessageWithPrefix(player, m.getString("not-in-party")); return;
        }

        Party party = Party.getParty(player);

        // Check if the player is not the party leader
        if (player != party.leader) {
            sendMessageWithPrefix(player, m.getString("not-leader")); return;
        }

        // Remove everyone from the party and delete the party
        for (Player member : party.members) {
            Party.inParty.remove(member); Party.parties.remove(party.name);
            sendMessageWithPrefix(member, (m.getString("party-disbanded") + "")
                    .replace("%partyName%", party.name));
        }
    }
}
