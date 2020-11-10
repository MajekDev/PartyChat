package me.majekdor.partychat.command.party;

import me.majekdor.partychat.command.CommandParty;
import me.majekdor.partychat.data.Party;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PartyPromote extends CommandParty {

    public static void execute(Player player, String[] args) {

        // Check if the player is not in a party
        if (!Party.inParty(player)) {
            sendMessageWithPrefix(player, m.getString("not-in-party"));
            return;
        }

        Party party = Party.getParty(player);

        // Check if the player is not the party leader
        if (!player.getUniqueId().equals(party.leader)) {
            sendMessageWithPrefix(player, m.getString("not-leader"));
            player.sendMessage(party.leader.toString() + "\n and \n" + player.getUniqueId().toString());
            return;
        }

        // Make sure the player specifies a new player
        if (args.length == 1) {
            sendMessageWithPrefix(player, m.getString("specify-player"));
            return;
        }
        execute(player, args[1]);
    }

    public static void execute(Player player, String newLeader) {
        Party party = Party.getParty(player);

        // Make sure the specified player is in the party
        Player target = Bukkit.getPlayerExact(newLeader);

        if (target == null) {
            sendMessageWithPrefix(player, m.getString("not-online")); return;
        }

        if (!party.members.contains(target.getUniqueId())) {
            sendMessageWithPrefix(player, m.getString("player-not-in-party"));
            return;
        }

        // Check if the player is trying to promote themself *sigh*
        if (player == target) {
            sendMessageWithPrefix(player, m.getString("promote-self"));
            return;
        }

        // Passed all of the checks
        party.leader = target.getUniqueId();
        sendMessageWithPrefix(target, (m.getString("you-promoted") + "")
                .replace("%player%", player.getDisplayName()));
        for (UUID memberUUID : party.members) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member == null) continue;
            if (member != target)
                sendMessageWithPrefix(member, (m.getString("new-leader") + "")
                        .replace("%player%", target.getDisplayName()));
        }
    }
}
