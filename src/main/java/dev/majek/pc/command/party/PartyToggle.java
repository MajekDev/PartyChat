package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.bukkit.entity.Player;

public class PartyToggle extends PartyCommand {

    public PartyToggle() {
        super(
                "toggle", getSubCommandUsage("toggle"), getSubCommandDescription("toggle"),
                true, getSubCommandDisabled("toggle"), getSubCommandCooldown("toggle"),
                getSubCommandAliases("toggle")
        );
    }

    @Override
    public boolean execute(Player player, String[] args, boolean leftServer) {
        User user = PartyChat.getDataHandler().getUser(player);

        // Check if the player is not in a party
        if (!user.isInParty()) {
            sendMessage(player, "not-in-party");
            return false;
        }

        if (args.length == 1) {
            sendMessage(player, "choose-toggle");
            return false;
        }

        Party party = user.getParty();

        // This should never happen, but I want to know if it does
        if (party == null) {
            PartyChat.error("Error: PC-TOG_1 | The plugin is fine, but please report this error " +
                    "code here: https://discord.gg/CGgvDUz");
            sendMessage(player, "error");
            return false;
        }

        // Apply toggles
        if (args[1].equalsIgnoreCase("public")) {
            sendMessage(player, "toggle-public");
            party.setPublic(true);
        } else if (args[1].equalsIgnoreCase("private")) {
            sendMessage(player, "toggle-private");
            party.setPublic(false);
        } else if (args[1].equalsIgnoreCase("friendly-fire")) {
            if (args.length == 2) {
                sendMessageWithReplacement(player, "friendly-fire-status", "%status%",
                        party.allowsFriendlyFire() ? "allow" : "deny");
            } else {
                if (args[2].equalsIgnoreCase("allow")) {
                    sendMessage(player, "friendly-fire-set-enabled");
                    party.setFriendlyFire(true);
                } else if (args[2].equalsIgnoreCase("deny")) {
                    sendMessage(player, "friendly-fire-set-disabled");
                    party.setFriendlyFire(false);
                }
            }
        }

        return true;
    }
}
