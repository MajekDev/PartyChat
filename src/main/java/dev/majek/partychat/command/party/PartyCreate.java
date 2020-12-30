package dev.majek.partychat.command.party;

import dev.majek.partychat.api.PartyCreateEvent;
import dev.majek.partychat.command.CommandParty;
import dev.majek.partychat.PartyChat;
import dev.majek.partychat.data.Party;
import dev.majek.partychat.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PartyCreate extends CommandParty {

    private static boolean canceled = false;

    public static void execute(Player player, String[] args) {

        // Check if the player is already in a party
        if (Party.inParty(player)) {
            sendMessageWithPrefix(player, m.getString("in-party")); return;
        }

        // Check if the player specifies a party name
        if (args.length == 1) {
            sendMessageWithPrefix(player, m.getString("no-name")); return;
        }

        String partyName = args[1];
        execute(player, partyName);
    }

    public static void execute(Player player, String partyName) {
        // Check if a party with that name already exists
        if (Party.nameTaken(partyName)) {
            sendMessageWithPrefix(player, m.getString("name-taken")); return;
        }

        // Check to see if the admins want to block inappropriate party names
        if (c.getBoolean("block-inappropriate-names")) {
            for (String blockedWord : c.getStringList("blocked-names.wordlist")) {
                if (partyName.contains(blockedWord)) {
                    sendMessageWithPrefix(player, m.getString("inappropriate-name"));
                    return;
                }
            }
        }

        // Check if the party name exceeds the character limit
        int max = c.getInt("max-characters");
        String noColor = Chat.removeColorCodes(partyName);
        if (noColor.length() > max) {
            sendMessageWithPrefix(player, m.getString("less-20")); return;
        }

        // Passed all requirements
        Party party = new Party(player, partyName);

        // Run party create event
        Bukkit.getScheduler().runTask(PartyChat.instance, () -> {
            PartyCreateEvent pce = new PartyCreateEvent(player, party);
            Bukkit.getPluginManager().callEvent(pce);
            if (pce.isCancelled())
                canceled = true;
        });

        //  Give the event time to fire before proceeding
        Bukkit.getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () -> {
            if (canceled) { // Stop if event was canceled
                canceled = false;
                return;
            }
            Party.partyMap.put(player.getUniqueId(), party);
            for (String partyCreated : m.getStringList("party-created")) {
                sendMessageWithPrefix(player, (partyCreated).replace("%partyName%", partyName));
            }
            if (c.getBoolean("party-save-on-update"))
                PartyChat.getDatabase().updateParty(party);
        }, 2);
    }
}
