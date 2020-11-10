package me.majekdor.partychat.command.party;

import me.majekdor.partychat.command.CommandParty;
import me.majekdor.partychat.data.Party;
import org.bukkit.entity.Player;

public class PartyToggle extends CommandParty {

    public static void execute(Player player, String[] args) {

        // Check if the player is not in a party
        if (!Party.inParty(player)) {
            sendMessageWithPrefix(player, m.getString("not-in-party")); return;
        }

        Party party = Party.getParty(player);

        // Check if the player is not the party leader
        if (!player.getUniqueId().equals(party.leader)) {
            sendMessageWithPrefix(player, m.getString("not-leader")); return;
        }

        // Make sure the player specifies a toggle
        if (args.length == 1) {
            sendMessageWithPrefix(player, m.getString("choose-toggle")); return;
        }

        // Toggles
        if (args[1].equalsIgnoreCase("public")) {
            sendMessageWithPrefix(player, m.getString("toggle-public"));
            party.isPublic = true;
        }
        if (args[1].equalsIgnoreCase("private")) {
            sendMessageWithPrefix(player, m.getString("toggle-private"));
            party.isPublic = false;
        }
    }
}
