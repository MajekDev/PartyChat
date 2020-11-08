package me.majekdor.partychat.bukkit.command.party;

import me.majekdor.partychat.bukkit.command.CommandParty;
import me.majekdor.partychat.bukkit.data.Party;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class PartyRemove extends CommandParty {

    public static void execute(Player player, String[] args) {

        // Check if the player is not in a party
        if (!Party.inParty.containsKey(player.getUniqueId())) {
            sendMessageWithPrefix(player, m.getString("not-in-party")); return;
        }

        Party party = Party.getParty(player);

        // Check if the player is not the party leader
        if (!player.getUniqueId().equals(party.leader)) {
            sendMessageWithPrefix(player, m.getString("not-leader")); return;
        }

        // Make sure the player specifies a new player
        if (args.length == 1) {
            sendMessageWithPrefix(player, m.getString("specify-player")); return;
        }
        execute(player, args[1]);
    }

    public static void execute(Player player, String remove) {
        Party party = Party.getParty(player);

        // Make sure the specified player is in the party
        Player targetOnline = Bukkit.getPlayerExact(remove);
        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(remove);
        boolean online = true;
        if (targetOnline == null)
            online = false;

        if (!party.members.contains(targetOffline.getUniqueId())) {
            sendMessageWithPrefix(player, m.getString("player-not-in-party")); return;
        }

        // Check if the player is trying to promote themself *sigh*
        if (player.getUniqueId() == targetOffline.getUniqueId()) {
            sendMessageWithPrefix(player, m.getString("remove-self")); return;
        }

        // Passed all checks
        party.members.remove(targetOffline.getUniqueId()); party.size -= 1; Party.inParty.remove(targetOffline.getUniqueId());
        if (online)
            sendMessageWithPrefix(targetOnline, (m.getString("you-removed") + "")
                    .replace("%player%", player.getDisplayName()));
        for (UUID memberUUID : party.members) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member == null) continue;
            sendMessageWithPrefix(member, (m.getString("player-removed") + "")
                    .replace("%player%", Objects.requireNonNull(targetOffline.getName())));
        }
    }
}
