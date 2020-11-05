package me.majekdor.partychat.command.party;

import me.majekdor.partychat.command.CommandParty;
import me.majekdor.partychat.data.Party;
import me.majekdor.partychat.util.Chat;
import org.bukkit.entity.Player;

public class PartyCreate extends CommandParty {

    public static void execute(Player player, String[] args) {

        // Check if the player is already in a party
        if (Party.inParty.containsKey(player.getUniqueId())) {
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
        if (Party.inParty.containsValue(partyName)) {
            sendMessageWithPrefix(player, m.getString("name-taken")); return;
        }

        // Check to see if the admins want to block inappropriate party names
        if (c.getBoolean("block-inappropriate-names")) {
            for (String blockedWord : c.getStringList("blocked-names.wordlist")) {
                if (partyName.equalsIgnoreCase(blockedWord))
                    sendMessageWithPrefix(player, m.getString("inappropriate-name")); return;
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
        Party.inParty.put(player.getUniqueId(), partyName);
        Party.parties.put(partyName, party);
        for (String partyCreated : m.getStringList("party-created")) {
            sendMessageWithPrefix(player, (partyCreated).replace("%partyName%", partyName));
        }
    }
}
