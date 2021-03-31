package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class PartyLeave extends PartyCommand {

    public PartyLeave() {
        super(
                "leave", getSubCommandUsage("leave"), getSubCommandDescription("leave"),
                false, getSubCommandDisabled("leave"), getSubCommandCooldown("leave"),
                getSubCommandAliases("leave")
        );
    }

    @Override
    public boolean execute(Player player, String[] args, boolean leftServer) {
        User user = PartyChat.getDataHandler().getUser(player);

        // Player can only leave a party if they're in one
        if (!user.isInParty()) {
            sendMessage(player, "not-in-party");
            return false;
        }

        Party party = user.getParty();

        // This should never happen, but I want to know if it does
        if (party == null) {
            PartyChat.error("Error: PC-LEV_1 | The plugin is fine, but please report this error " +
                    "code here: https://discord.gg/CGgvDUz");
            sendMessage(player, "error"); return false;
        }

        if (!leftServer)
            sendMessageWithReplacement(player, "you-leave", "%partyName%", party.getName());

        boolean partyDisbanded = party.getSize() == 1;
        party.removeMember(user);
        if (partyDisbanded)
            PartyChat.getPartyHandler().removeFromPartyMap(user.getPartyID());
        user.setPartyID(null);
        user.setInParty(false);

        if (!partyDisbanded) {
            party.getMembers().stream().map(User::getPlayer).filter(Objects::nonNull).forEach(member ->
                    sendMessageWithReplacement(member, "player-leave", "%player%", player.getDisplayName()));

            // Check if the player who left was the leader
            if (user.getPlayerID().equals(party.getLeader())) {
                Random random  = new Random(); // Assign a new random leader
                party.setLeader(party.getMembers().stream().map(User::getPlayerID)
                        .collect(Collectors.toList()).get(random.nextInt(party.getSize())));
                Player leader = Bukkit.getPlayer(party.getLeader());
                if (leader != null)
                    sendMessage(leader, "you-leader");
                party.getMembers().stream().map(User::getPlayer).filter(Objects::nonNull).filter(p -> p.getUniqueId()
                        != party.getLeader()).forEach(member -> sendMessageWithReplacement(member, "new-leader",
                        "%player%", Bukkit.getOfflinePlayer(party.getLeader()).getName()));
            }
        }
        // Update the database if persistent parties is enabled
        if (PartyChat.getDataHandler().persistentParties)
            if (partyDisbanded)
                PartyChat.getPartyHandler().deleteParty(party);
            else
                PartyChat.getPartyHandler().saveParty(party);

        return true;
    }
}
