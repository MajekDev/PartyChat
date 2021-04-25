package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PartyInfo extends PartyCommand {

    public PartyInfo() {
        super(
                "info", getSubCommandUsage("info"), getSubCommandDescription("info"),
                false, getSubCommandDisabled("info"), getSubCommandCooldown("info"),
                getSubCommandAliases("info")
        );
    }

    @Override
    public boolean execute(Player player, String[] args, boolean leftServer) {

        User user = PartyChat.getDataHandler().getUser(player);

        // Make sure player is in a party
        if (!PartyChat.getDataHandler().getUser(player).isInParty()) {
            sendMessage(player, "not-in-party"); return false;
        }

        Party party = user.getParty();

        // This should never happen, but I want to know if it does
        if (party == null) {
            PartyChat.error("Error: PC-INF_1 | The plugin is fine, but please report this error " +
                    "code here: https://discord.gg/CGgvDUz");
            sendMessage(player, "error"); return false;
        }

        // If the player is in the party by themself -> leader
        if (party.getSize() == 1) {
            sendMessageWithEverything(player, "info-leader", "%partyName%", party.getName(),
                    "", "", party.getLeader().getUsername());
            return true;
        }

        // Build member list string
        StringBuilder memberList = new StringBuilder();
        party.getMembers().stream().filter(member -> !member.equals(party.getLeader())).map(User::getUsername)
                .forEach(name -> memberList.append(name).append(", "));
        String cleanList = memberList.toString().trim().substring(0, memberList.toString().length() - 2);

        // Send message
        sendMessageWithEverything(player, "info-members", "%partyName%", party.getName(),
                "%player%", party.getLeader().getUsername(), cleanList);

        return true;
    }
}
