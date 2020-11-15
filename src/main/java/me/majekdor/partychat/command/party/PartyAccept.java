package me.majekdor.partychat.command.party;

import me.majekdor.partychat.PartyChat;
import me.majekdor.partychat.api.PlayerPartyJoinEvent;
import me.majekdor.partychat.command.CommandParty;
import me.majekdor.partychat.data.Party;
import me.majekdor.partychat.util.Bar;
import me.majekdor.partychat.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PartyAccept extends CommandParty {

    private static boolean canceled = false;

    public static void execute(Player player) {

        // Check if the player is in a party
        if (Party.inParty(player)) {
            Party party = Party.getParty(player);

            // Check if the player has a pending summon request
            if (party.pendingSummons.contains(player)) {
                bar = new Bar(); bar.createBar(); bar.addPlayer(player);
                Player leader = Bukkit.getPlayer(party.leader);
                if (leader == null) {
                    sendMessageWithPrefix(player, m.getString("leader-offline")); return;
                }
                if (party.size < 6) {
                    sendMessageWithPrefix(Bukkit.getPlayer(party.leader), (m.getString("teleport-accepted") + "")
                            .replace("%player%", player.getDisplayName()));
                }
                sendMessageWithPrefix(player, m.getString("teleport-prepare"));
                Party.noMove.add(player); party.pendingSummons.remove(player);
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () -> {
                    if (Party.noMove.contains(player)) {

                        // Make sure the location is safe
                        Location safe = Utils.findSafe(leader.getLocation(),
                                leader.getLocation().getBlockY()-5, 256);
                        if (safe == null) {
                            sendMessageWithPrefix(player, m.getString("teleport-unsafe"));
                        } else {
                            player.teleport(safe);
                            sendMessageWithPrefix(player, m.getString("teleported"));

                            // Give the player temporary invulnerability to keep them safe
                            if (!(player.isInvulnerable())) {
                                player.setInvulnerable(true);
                                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () ->
                                        player.setInvulnerable(false), 60L); // 3 second delay
                            }
                        }
                        bar.removePlayer(player); Party.noMove.remove(player);
                    }
                }, 60L); return;
            }

            // Check if the player is trying to accept a join request
            if (party.pendingJoinRequests.size() > 0) {
                Player requester = party.pendingJoinRequests.get(0);

                // Make sure the player who requested to join is still online
                if (!requester.isOnline()) {
                    sendMessageWithPrefix(player, m.getString("not-online")); return;
                }

                // Run the event
                eventFire(player, party);

                //  Give the event time to fire before proceeding
                Bukkit.getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () -> {
                    if (canceled) { // Stop if event was canceled
                        canceled = false;
                        return;
                    }
                    // Send messages
                    sendMessageWithPrefix(requester, (m.getString("you-join") + "")
                            .replace("%partyName%", party.name));
                    for (UUID memberUUID : party.members) {
                        Player member = Bukkit.getPlayer(memberUUID);
                        if (member == null) continue;
                        sendMessageWithPrefix(member, (m.getString("player-join") + "")
                                .replace("%player%", requester.getDisplayName()));
                    }

                    // Put the player in the party
                    party.pendingJoinRequests.remove(requester);
                    Party.partyMap.put(requester.getUniqueId(), party);
                    party.members.add(requester.getUniqueId()); party.size++;
                    if (c.getBoolean("party-save-on-update"))
                        PartyChat.getDatabase().updateParty(party);
                }, 2);
                return;
            }

            // Player is in a party and /p accept has no use
            sendMessageWithPrefix(player, m.getString("in-party"));

            // Player is not in a party
        } else {
            Party party = null; // Check for invites
            for (Party check : Party.partyMap.values())
                if (check.pendingInvitations.contains(player)) {
                    party = check; break;
                }

            // Player has no pending invitations
            if (party == null) {
                sendMessageWithPrefix(player, m.getString("no-invites")); return;
            }

            // Run the event
            eventFire(player, party);

            //  Give the event time to fire before proceeding
            Party finalParty = party; // Variable must be final for lambda expression
            Bukkit.getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () -> {
                if (canceled) { // Stop if event was canceled
                    canceled = false;
                    return;
                }
                // Send messages
                for (UUID memberUUID : finalParty.members) {
                    Player member = Bukkit.getPlayer(memberUUID);
                    if (member == null) continue;
                    sendMessageWithPrefix(member, (m.getString("player-join") + "")
                            .replace("%player%", player.getDisplayName()));
                }
                sendMessageWithPrefix(player, (m.getString("you-join")  + "")
                        .replace("%partyName%", finalParty.name));

                // Put the player in the party
                finalParty.pendingInvitations.remove(player);
                Party.partyMap.put(player.getUniqueId(), finalParty);
                finalParty.members.add(player.getUniqueId()); finalParty.size++;
                if (c.getBoolean("party-save-on-update"))
                    PartyChat.getDatabase().updateParty(finalParty);
            }, 2);
        }
    }

    public static void eventFire(Player player, Party party) {
        Bukkit.getScheduler().runTask(PartyChat.instance, () -> {
            PlayerPartyJoinEvent ppje = new PlayerPartyJoinEvent(player, party);
            Bukkit.getPluginManager().callEvent(ppje);
            if (ppje.isCancelled())
                canceled = true;
        });
    }
}
