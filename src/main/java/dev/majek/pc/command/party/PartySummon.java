package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.util.Utils;
import org.bukkit.entity.Player;

public class PartySummon extends PartyCommand {

    public PartySummon() {
        super(
                "summon", getSubCommandUsage("summon"), getSubCommandDescription("summon"),
                true, getSubCommandDisabled("summon"), getSubCommandCooldown("summon"),
                getSubCommandAliases("summon")
        );
    }

    @Override
    public boolean execute(Player player, String[] args, boolean leftServer) {
        User user = PartyChat.getDataHandler().getUser(player);
        Party party = user.getParty();

        // Check if the player is already in a party
        if (!PartyChat.getDataHandler().getUser(player).isInParty()) {
            sendMessage(player, "not-in-party");
            return false;
        }

        // This should never happen, but I want to know if it does
        if (party == null) {
            PartyChat.error("Error: PC-SUM_1 | The plugin is fine, but please report this error " +
                    "code here: https://discord.gg/CGgvDUz");
            sendMessage(player, "error");
            return false;
        }

        // Make sure the leader is in a safe location
        if (!Utils.isSafe(player.getLocation())) {
            sendMessage(player, "location-unsafe");
            return false;
        }

        // Send summons to all members
        for (User member : party.getMembers()) {
            if (!member.isOnline())
                continue;
            if (member.getPlayer() == player)
                continue;
            for (String string : PartyChat.getDataHandler().getConfigStringList(mainConfig, "summon-request")) {
                sendFormattedMessage(member.getPlayer(), string.replace("%prefix%", PartyChat.getDataHandler()
                        .getConfigString(PartyChat.getDataHandler().messages, "prefix"))
                        .replace("%player%", player.getDisplayName()));
            }
            party.addPendingSummons(member.getPlayer());
            int timeout = PartyChat.getDataHandler().getConfigInt(mainConfig, "summon-expire-time");
            if (timeout != -1) {
                runTaskLater(timeout, () ->  {
                    if (party.getPendingSummons().contains(member.getPlayer())) {
                        party.removePendingSummons(member.getPlayer());
                        sendMessage(member.getPlayer(), "teleport-timeout");
                    }
                });
            }
        }
        sendMessage(player, "summon-sent");
        return true;
    }
}
