package me.majekdor.partychat.bukkit.command.party;

import me.majekdor.partychat.bukkit.command.CommandParty;
import org.bukkit.entity.Player;

public class PartyHelp extends CommandParty {

    // Very simple - send help messages (2 page options)
    public static void execute(Player player, String[] args) {
        //TODO Internalize help messages - don't include in messages.yml - add hover events for better descriptions

        if ((args.length == 1) || args[1].equalsIgnoreCase("1")) {
            for (String partyHelp1 : m.getStringList("party-help1")) {
                sendMessageWithPrefix(player, partyHelp1);
            }
        } else if (args[1].equalsIgnoreCase("2")) {
            for (String partyHelp2 : m.getStringList("party-help2")) {
                sendMessageWithPrefix(player, partyHelp2);
            }
        }
    }
}
