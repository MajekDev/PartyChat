package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.Restrictions;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.api.PartyCreateEvent;
import dev.majek.pc.util.Chat;
import dev.majek.pc.util.TabCompleterBase;
import org.bukkit.entity.Player;

public class PartyCreate extends PartyCommand {

    public PartyCreate() {
        super(
                "create", getSubCommandUsage("create"), getSubCommandDescription("create"),
                false, getSubCommandDisabled("create"), getSubCommandCooldown("create"),
                getSubCommandAliases("create")
        );
    }


    @Override
    public boolean execute(Player player, String[] args, boolean leftServer) {

        // Check if the player is already in a party
        if (PartyChat.getDataHandler().getUser(player).isInParty()) {
            sendMessage(player, "in-party");
            return false;
        }

        // Make sure the player specifies a party name
        if (args.length == 1) {
            sendMessage(player, "no-name");
            return false;
        }

        // Continue in a separate method, passing through party name
        return execute(player, TabCompleterBase.joinArgsBeyond(0, "-", args));
    }

    public static boolean execute(Player player, String name) {
        // Check if a party with that name already exists
        if (PartyChat.getPartyHandler().isNameTaken(name)) {
            sendMessage(player, "name-taken");
            return false;
        }

        // Check if the server is blocking inappropriate names and block them if the name contains them
        if (PartyChat.getDataHandler().getConfigBoolean(mainConfig, "block-inappropriate-names")) {
            if (Restrictions.containsCensoredWord(name)) {
                sendMessage(player, "inappropriate-name");
                return false;
            }
        }

        // Check if the party name exceeds the character limit defined in the config file
        int max = PartyChat.getDataHandler().getConfigInt(mainConfig, "max-name-length");
        if (Chat.removeColorCodes(name).length() > max && max > 0) {
            sendMessage(player, "name-too-long");
            return false;
        }

        // Passed all requirements, create new Party object
        Party party = new Party(player, name);

        // Run PartyCreateEvent
        PartyCreateEvent event = new PartyCreateEvent(player, party);
        PartyChat.getCore().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;

        // Put the player in the main party map and send message
        PartyChat.getPartyHandler().addToPartyMap(party.getId(), party);
        PartyChat.getDataHandler().getUser(player).setPartyID(party.getId());
        PartyChat.getDataHandler().getUser(player).setInParty(true);
        for (String message : PartyChat.getDataHandler().getConfigStringList(PartyChat
                .getDataHandler().messages, "party-created")) {
            player.sendMessage(Chat.applyColorCodes(message.replace("%prefix%", PartyChat
                    .getDataHandler().getConfigString(PartyChat.getDataHandler().messages, "prefix"))
                    .replace("%partyName%", name)));
        }

        // Update the database if persistent parties is enabled
        if (PartyChat.getDataHandler().persistentParties) {
            PartyChat.getPartyHandler().saveParty(party);
        }

        return true;
    }
}