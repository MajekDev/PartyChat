package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.stream.Collectors;

public class PartyPromote extends PartyCommand {

    public PartyPromote() {
        super(
                "promote", getSubCommandUsage("promote"), getSubCommandDescription("promote"),
                true, getSubCommandDisabled("promote"), getSubCommandCooldown("promote"),
                getSubCommandAliases("promote")
        );
    }

    @Override
    public boolean execute(Player player, String[] args, boolean leftServer) {
        User user = PartyChat.getDataHandler().getUser(player);

        // Must be in a party to use
        if (!user.isInParty()) {
            sendMessage(player, "not-in-party");
            return false;
        }

        Party party = user.getParty();

        // This should never happen, but I want to know if it does
        if (party == null) {
            PartyChat.error("Error: PC-PRO_1 | The plugin is fine, but please report this error " +
                    "code here: https://discord.gg/CGgvDUz");
            sendMessage(player, "error");
            return false;
        }

        if (args.length == 1) {
            sendMessage(player, "specify-player");
            return false;
        }

        return execute(player, args[1]);
    }

    public static boolean execute(Player player, String newLeader) {
        User user = PartyChat.getDataHandler().getUser(player);
        Party party = user.getParty();

        // This should never happen, but I want to know if it does
        if (party == null) {
            PartyChat.error("Error: PC-PRO_2 | The plugin is fine, but please report this error " +
                    "code here: https://discord.gg/CGgvDUz");
            sendMessage(player, "error");
            return false;
        }

        // Make sure the specified player is in the party
        Player target = Bukkit.getPlayerExact(newLeader);
        if (target == null) {
            sendMessage(player, "not-online");
            return false;
        }
        if (!(party.getMembers().stream().map(User::getPlayer).collect(Collectors.toList()).contains(target))) {
            sendMessage(player, "player-not-in-party");
            return false;
        }

        // Player is trying to promote themself :P
        if (player == target) {
            sendMessage(player, "promote-self");
            return false;
        }

        // Promote player
        party.setLeader(target.getUniqueId());
        sendMessageWithReplacement(target, "you-promoted", "%player%", player.getDisplayName());
        party.getMembers().stream().map(User::getPlayer).filter(Objects::nonNull).filter(p -> p.getUniqueId()
                != party.getLeader()).forEach(member -> sendMessageWithReplacement(member, "new-leader",
                "%player%", target.getDisplayName()));

        // Update the database if persistent parties is enabled
        if (PartyChat.getDataHandler().persistentParties)
            PartyChat.getPartyHandler().saveParty(party);

        return true;
    }
}
