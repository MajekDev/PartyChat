package me.majekdor.partychat.command.party;

import me.majekdor.partychat.command.CommandParty;
import me.majekdor.partychat.data.Party;
import org.bukkit.entity.Player;

public class PartyDeny extends CommandParty {

    public static void execute(Player player) {

        // Check if the player is in a party
        if (Party.inParty.containsKey(player)) {
            Party party = Party.getParty(player);

            // Check if the player has a pending summon request
            if (party.pendingSummons.contains(player)) {
                sendMessageWithPrefix(player, m.getString("teleport-denied-player"));
                sendMessageWithPrefix(party.leader, (m.getString("teleport-denied") + "")
                        .replace("%player%", player.getDisplayName()));
                party.pendingSummons.remove(player); return;
            }

            if (party.pendingJoinRequests.size() > 0) {
                Player requester = party.pendingJoinRequests.get(0);

                // Make sure the player who requested to join is still online
                if (!requester.isOnline()) {
                    sendMessageWithPrefix(player, m.getString("not-online")); return;
                }

                // Send messages and remove from list
                sendMessageWithPrefix(player, m.getString("join-denied"));
                sendMessageWithPrefix(party.leader, (m.getString("deny-join") + "")
                        .replace("%player%", player.getDisplayName()));
                party.pendingJoinRequests.remove(player); return;
            }

            // Player is in a party and /p deny has no use
            sendMessageWithPrefix(player, m.getString("in-party"));

            // Player is not in a party
        } else {
            Party party = null; // Check for invites
            for (Party check : Party.parties.values())
                if (check.pendingInvitations.contains(player)) {
                    party = check; break;
                }

            // Player has no pending invitations
            if (party == null) {
                sendMessageWithPrefix(player, m.getString("no-invites")); return;
            }

            // Send deny messages
            sendMessageWithPrefix(player, m.getString("you-decline"));
            for (Player member : party.members)
                sendMessageWithPrefix(member, (m.getString("decline-join") + "")
                        .replace("%player%", player.getDisplayName()));
            party.pendingInvitations.remove(player);
        }
    }
}
