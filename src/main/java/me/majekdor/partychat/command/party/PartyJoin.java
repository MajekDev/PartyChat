package me.majekdor.partychat.command.party;

import me.majekdor.partychat.PartyChat;
import me.majekdor.partychat.command.CommandParty;
import me.majekdor.partychat.data.Party;
import me.majekdor.partychat.util.Chat;
import me.majekdor.partychat.util.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PartyJoin extends CommandParty implements TabCompleter {

    public static void execute(Player player, String[] args) {

        // Check if the player is already in a party
        if (Party.inParty.containsKey(player.getUniqueId())) {
            sendMessageWithPrefix(player, m.getString("in-party")); return;
        }

        // Make sure the player specifies a party
        if (args.length == 1) {
            sendMessageWithPrefix(player, m.getString("specify-party")); return;
        }
        execute(player, args[1]);
    }

    public static void execute(Player player, String partyName) {
        // See if the party exists
        boolean found = false;
        for (Party party : Party.parties.values()) {
            if (partyName.equalsIgnoreCase(Chat.removeColorCodes(party.name)) || partyName.equalsIgnoreCase(party.name)) {
                partyName = party.name; found = true;
                break;
            }
        }
        if (!found) {
            sendMessageWithPrefix(player, m.getString("unknown-party")); return;
        }

        Party party = Party.parties.get(partyName);

        // Don't let the player in if the party is private
        if (!party.isPublic) {
            sendMessageWithPrefix(player, m.getString("party-private")); return;
        }

        // Check if the player is on the blocked list
        if (party.blockedPlayers.contains(player)) {
            sendMessageWithPrefix(player, m.getString("join-wait")); return;
        }

        // Send join request to the party leader
        Player leader = Bukkit.getPlayer(party.leader);
        if (leader == null) {
            sendMessageWithPrefix(player, m.getString("leader-offline")); return;
        }

        for (String request : m.getStringList("request-join")) {
            TextUtils.sendFormatted(leader, request
                    .replace("%player%", player.getDisplayName())
                    .replace("%prefix%", Objects.requireNonNull(m.getString("other-format-prefix"))));
        }
        sendMessageWithPrefix(player, m.getString("request-sent"));
        party.pendingJoinRequests.add(player);
        party.blockedPlayers.add(player);

        // Don't let the player join again for a certain amount of time
        int delay = c.getInt("block-time") * 20;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () ->
                party.blockedPlayers.remove(player), delay);

        // Send the player a message if their request expires
        int expire = c.getInt("expire-time") * 20;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () -> {
            if (party.pendingJoinRequests.contains(player)) {
                party.pendingJoinRequests.remove(player);
                sendMessageWithPrefix(player, m.getString("expired-join"));
                sendMessageWithPrefix(leader, m.getString("expired-join"));
            }
                }, expire);
    }
}
