package me.majekdor.partychat.command.party;

import me.majekdor.partychat.command.CommandParty;
import me.majekdor.partychat.data.Party;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PartyRemove extends CommandParty {

    public static void execute(Player player, String[] args) {

        // Check if the player is not in a party
        if (!Party.inParty.containsKey(player)) {
            sendMessageWithPrefix(player, m.getString("not-in-party")); return;
        }

        Party party = Party.getParty(player);

        // Check if the player is not the party leader
        if (player != party.leader) {
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
        Player target = Bukkit.getPlayerExact(remove);
        if (target == null || !party.members.contains(target)) {
            sendMessageWithPrefix(player, m.getString("player-not-in-party")); return;
        }

        // Check if the player is trying to promote themself *sigh*
        if (player == target) {
            sendMessageWithPrefix(player, m.getString("remove-self")); return;
        }

        // Passed all checks
        party.members.remove(target); party.size -= 1; Party.inParty.remove(target);
        sendMessageWithPrefix(target, (m.getString("you-removed") + "")
                .replace("%player%", player.getDisplayName()));
        for (Player member : party.members)
            sendMessageWithPrefix(member, (m.getString("player-removed") + "")
                    .replace("%player%", target.getDisplayName()));
    }
}
