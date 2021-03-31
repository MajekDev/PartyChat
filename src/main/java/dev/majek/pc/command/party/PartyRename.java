package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.Restrictions;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.util.Chat;
import org.bukkit.entity.Player;

public class PartyRename extends PartyCommand {

    public PartyRename() {
        super(
                "rename", getSubCommandUsage("rename"), getSubCommandDescription("rename"),
                true, getSubCommandDisabled("rename"), getSubCommandCooldown("rename"),
                getSubCommandAliases("rename")
        );
    }

    @Override
    public boolean execute(Player player, String[] args, boolean leftServer) {

        // Check if the player is already in a party
        if (!PartyChat.getDataHandler().getUser(player).isInParty()) {
            sendMessage(player, "not-in-party");
            return false;
        }

        // Make sure the player specifies a party name
        if (args.length == 1) {
            sendMessage(player, "no-name");
            return false;
        }

        return execute(player, args[1]);
    }

    public static boolean execute(Player player, String newName) {
        User user = PartyChat.getDataHandler().getUser(player);
        Party party = user.getParty();

        // This should never happen, but I want to know if it does
        if (party == null) {
            PartyChat.error("Error: PC-REN_1 | The plugin is fine, but please report this error " +
                    "code here: https://discord.gg/CGgvDUz");
            sendMessage(player, "error");
            return false;
        }

        // Check if a party with that name already exists
        if (PartyChat.getPartyHandler().isNameTaken(newName)) {
            sendMessage(player, "name-taken");
            return false;
        }

        // Check if the server is blocking inappropriate names and block them if the name contains them
        if (PartyChat.getDataHandler().getConfigBoolean(mainConfig, "block-inappropriate-names")) {
            if (Restrictions.containsCensoredWord(newName)) {
                sendMessage(player, "inappropriate-name");
                return false;
            }
        }

        // Check if the party name exceeds the character limit defined in the config file
        if (Chat.removeColorCodes(newName).length() > PartyChat.getDataHandler()
                .getConfigInt(mainConfig, "max-name-length")) {
            sendMessage(player, "name-too-long");
            return false;
        }

        party.setName(newName);
        sendMessageWithReplacement(player, "party-rename", "%partyName%", newName);

        // Update the database if persistent parties is enabled
        if (PartyChat.getDataHandler().persistentParties)
            PartyChat.getPartyHandler().saveParty(party);

        return true;
    }
}
