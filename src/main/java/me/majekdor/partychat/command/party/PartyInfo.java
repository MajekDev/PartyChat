package me.majekdor.partychat.command.party;

import me.majekdor.partychat.PartyChat;
import me.majekdor.partychat.command.CommandParty;
import me.majekdor.partychat.data.Party;
import me.majekdor.partychat.data.Restrictions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class PartyInfo extends CommandParty {

    public static void execute(Player player) {

        // Check if the player is not in a party
        if (!Party.inParty(player)) {
            sendMessageWithPrefix(player, m.getString("not-in-party")); return;
        }

        // Get the party the player is in
        Party party = Party.getParty(player);

        // Check if the player is the only one in the party
        if (party.size == 1) {
            sendMessageWithPrefix(player, (m.getString("info-leader") + Bukkit.getOfflinePlayer(party.leader).getName())
                    .replace("%partyName%", party.name)); return;
        }

        // Build a string of party members
        StringBuilder members = new StringBuilder();
        for (UUID memberUUID : party.members) {
            OfflinePlayer member = Bukkit.getOfflinePlayer(memberUUID);
            if (member.getPlayer() != null && Restrictions.isVanished(member.getPlayer())) // Don't include vanished players
                if (PartyChat.instance.getConfig().getBoolean("hide-vanished-players")) continue;
            if (!memberUUID.equals(party.leader)) // Don't add to member string if player is leader
                members.append(member.getName()).append(", ");
        }

        // Clean up string
        String cleanMembers = members.toString().trim();
        cleanMembers = cleanMembers.substring(0, cleanMembers.length()-1);

        // Send player info message
        sendMessageWithPrefix(player, (m.getString("info-members") + cleanMembers)
                .replace("%player%", Objects.requireNonNull(Bukkit.getOfflinePlayer(party.leader).getName()))
                .replace("%partyName%", party.name));
    }
}
