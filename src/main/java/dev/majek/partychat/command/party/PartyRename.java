package dev.majek.partychat.command.party;

import dev.majek.partychat.PartyChat;
import dev.majek.partychat.command.CommandParty;
import dev.majek.partychat.data.Party;
import dev.majek.partychat.util.Chat;
import org.bukkit.entity.Player;

public class PartyRename extends CommandParty {

    public static void execute(Player player, String[] args) {

        // Check if the player is not in a party
        if (!Party.inParty(player)) {
            sendMessageWithPrefix(player, m.getString("not-in-party")); return;
        }

        Party party = Party.getParty(player);

        // Check if the player is not the party leader
        if (!player.getUniqueId().equals(party.leader)) {
            sendMessageWithPrefix(player, m.getString("not-leader")); return;
        }

        // Make sure the player specifies a new name
        if (args.length == 1) {
            sendMessageWithPrefix(player, m.getString("no-name")); return;
        }

        execute(player, args[1]);
    }

    public static void execute(Player player, String partyName) {
        Party party = Party.getParty(player);

        if (Party.nameTaken(partyName)) {
            sendMessageWithPrefix(player, m.getString("name-taken")); return;
        }

        // Check to see if the admins want to block inappropriate party names
        if (c.getBoolean("block-inappropriate-names")) {
            for (String blockedWord : c.getStringList("blocked-names.wordlist"))
                if (partyName.equalsIgnoreCase(blockedWord)) {
                    sendMessageWithPrefix(player, m.getString("inappropriate-name")); return;
                }
        }

        // Check if the party name exceeds the character limit
        int max = c.getInt("max-characters");
        String noColor = Chat.removeColorCodes(partyName);
        if (noColor.length() > max) {
            sendMessageWithPrefix(player, m.getString("less-20")); return;
        }

        // Passed all checks
        Party.changeName(party, partyName);
        sendMessageWithPrefix(player, (m.getString("party-rename") + "")
                .replace("%partyName%", partyName));

        if (c.getBoolean("party-save-on-update"))
            PartyChat.getDatabase().updateParty(party);
    }
}
