package me.majekdor.partychat.command.party;

import me.majekdor.partychat.command.CommandParty;
import me.majekdor.partychat.data.Party;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PartyDisband extends CommandParty {

    public static void execute(Player player) {

        // Check if the player is not in a party
        if (!Party.inParty(player)) {
            sendMessageWithPrefix(player, m.getString("not-in-party")); return;
        }

        Party party = Party.getParty(player);

        // Check if the player is not the party leader
        if (!player.getUniqueId().equals(party.leader)) {
            sendMessageWithPrefix(player, m.getString("not-leader")); return;
        }

        // Remove everyone from the party and delete the party
        for (UUID memberUUID : party.members) {
            OfflinePlayer member = Bukkit.getOfflinePlayer(memberUUID);
            Party.partyMap.remove(memberUUID);
            if (member.getPlayer() != null)
                sendMessageWithPrefix(member.getPlayer(), (m.getString("party-disbanded") + "")
                    .replace("%partyName%", party.name));
        }
    }
}
