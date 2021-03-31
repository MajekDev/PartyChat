package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PartyDeny extends PartyCommand {

    public PartyDeny() {
        super(
                "deny", getSubCommandUsage("deny"), getSubCommandDescription("deny"),
                false, getSubCommandDisabled("deny"), getSubCommandCooldown("deny"),
                getSubCommandAliases("deny")
        );
    }

    @Override
    public boolean execute(Player player, String[] args, boolean leftServer) {

        User user = PartyChat.getDataHandler().getUser(player);

        // Player is not in a party
        if (!user.isInParty()) {

            // Check for pending invitations
            Party party = null;
            Player inviter = null;
            for (Party check : PartyChat.getPartyHandler().getPartyMap().values())
                for (Pair<Player, Player> players : check.getPendingInvitations())
                    if (players.getFirst() == player) {
                        party = check; inviter = players.getSecond(); break;
                    }

            // Player has no pending invitations
            if (party == null) {
                sendMessage(player, "no-invites"); return false;
            }

            // Send messages
            Player leader = Bukkit.getPlayer(party.getLeader());
            if (leader != null && leader.isOnline())
                sendMessageWithReplacement(leader, "decline-join", "%player%", player.getDisplayName());
            if (inviter != null && inviter.isOnline() && inviter != leader)
                sendMessageWithReplacement(inviter, "decline-join", "%player%", player.getDisplayName());
            sendMessage(player, "you-decline");

            party.removePendingInvitation(player);

            return true;
        }

        // Player is in a party
        else {
            Party party = user.getParty();
            // This should never happen, but I want to know if it does
            if (party == null) {
                PartyChat.error("Error: PC-DNY_1 | The plugin is fine, but please report this error " +
                        "code here: https://discord.gg/CGgvDUz");
                sendMessage(player, "error"); return false;
            }
            Player leader = Bukkit.getPlayer(party.getLeader());

            // Check if the player has a pending summon request
            if (party.getPendingSummons().contains(player)) {
                sendMessage(player, "teleport-denied-player");
                if (leader != null && leader.isOnline() && player != leader)
                    sendMessageWithReplacement(leader, "teleport-denied",
                            "%player%", player.getDisplayName());
                party.removePendingSummons(player);
                return true;
            }

            // Check if the player is a leader denying a join request
            else if (party.getPendingJoinRequests().size() > 0) {

                // Only leaders can deny join requests
                if (player.getUniqueId() != party.getLeader()) {
                    sendMessage(player, "in-party"); return false;
                }

                Player toDeny;
                // Check if the leader doesn't specify a player to accept
                if (args.length == 1) {
                    if (party.getPendingJoinRequests().size() == 1) {
                        toDeny = party.getPendingJoinRequests().get(0);
                    } else {
                        sendMessage(player, "specify-player"); return false;
                    }
                } else {
                    toDeny = Bukkit.getPlayer(args[1]);
                    if (!party.getPendingJoinRequests().contains(toDeny)) {
                        sendMessage(player, "no-request"); return false;
                    }
                }

                // This should never happen, but I want to know if it does
                if (toDeny == null) {
                    PartyChat.error("Error: PC-DNY_2 | The plugin is fine, but please report this error " +
                            "code here: https://discord.gg/CGgvDUz");
                    sendMessage(player, "error"); return false;
                }

                // Send messages
                sendMessage(toDeny, "join-denied");
                sendMessageWithReplacement(player, "deny-join", "%player%", player.getDisplayName());

                party.removePendingJoinRequest(toDeny);

                return true;
            } else {
                sendMessage(player, "no-usage"); return false;
            }
        }
    }
}
