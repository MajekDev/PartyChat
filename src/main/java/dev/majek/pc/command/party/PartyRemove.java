package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.stream.Collectors;

public class PartyRemove extends PartyCommand {

    public PartyRemove() {
        super(
                "remove", getSubCommandUsage("remove"), getSubCommandDescription("remove"),
                true, getSubCommandDisabled("remove"), getSubCommandCooldown("remove"),
                getSubCommandAliases("remove")
        );
    }

    @Override
    public boolean execute(Player player, String[] args, boolean leftServer) {
        User user = PartyChat.getDataHandler().getUser(player);
        Party party = user.getParty();

        // Make sure the player is in a party
        if (!user.isInParty()) {
            sendMessage(player, "not-in-party");
            return false;
        }

        // This should never happen, but I want to know if it does
        if (party == null) {
            PartyChat.error("Error: PC-REM_1 | The plugin is fine, but please report this error " +
                    "code here: https://discord.gg/CGgvDUz");
            sendMessage(player, "error");
            return false;
        }

        // Must specify a player to remove
        if (args.length == 1) {
            sendMessage(player, "specify-player");
            return false;
        }

        return execute(player, args[1]);
    }

    public static boolean execute(Player player, String toRemove) {
        User user = PartyChat.getDataHandler().getUser(player);
        Party party = user.getParty();

        // This should never happen, but I want to know if it does
        if (party == null) {
            PartyChat.error("Error: PC-REM_2 | The plugin is fine, but please report this error " +
                    "code here: https://discord.gg/CGgvDUz");
            sendMessage(player, "error");
            return false;
        }

        // Make sure the user is in the party
        User target = party.getMembers().stream().filter(member -> member.getUsername()
                .equalsIgnoreCase(toRemove)).collect(Collectors.toList()).get(0);
        if (target == null) {
            sendMessage(player, "player-not-in-party");
            return false;
        }

        // Player is trying to remove themself
        if (user == target) {
            sendMessage(player, "remove-self");
            return false;
        }

        // Player is trying to remove leader
        if (user.equals(party.getLeader())) {
            sendMessage(player, "remove-leader");
            return false;
        }

        party.removeMember(target);
        target.setPartyID(null);
        target.setInParty(false);

        if (target.isOnline() && target.getPlayer() != null)
            sendMessageWithReplacement(target.getPlayer(), "you-removed", "%player%", user.getNickname());

        party.getMembers().stream().map(User::getPlayer).filter(Objects::nonNull).forEach(member ->
                sendMessageWithReplacement(member, "player-removed", "%player%", target.getUsername()));

        // Update the database if persistent parties is enabled
        if (PartyChat.getDataHandler().persistentParties)
            PartyChat.getPartyHandler().saveParty(party);

        return true;
    }
}
