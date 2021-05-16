package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.bukkit.entity.Player;

public class PartyDisband extends PartyCommand {

    public PartyDisband() {
        super(
                "disband", getSubCommandUsage("disband"), getSubCommandDescription("disband"),
                true, getSubCommandDisabled("disband"), getSubCommandCooldown("disband"),
                getSubCommandAliases("disband")
        );
    }

    @Override
    public boolean execute(Player player, String[] args, boolean leftServer) {

        Party party = PartyChat.getPartyHandler().getParty(PartyChat.getDataHandler().getUser(player));

        // This should never happen, but I want to know if it does
        if (party == null) {
            PartyChat.error("Error: PC-DIS_1 | The plugin is fine, but please report this error " +
                    "code here: https://discord.gg/CGgvDUz");
            sendMessage(player, "error");
            return false;
        }

        // Disband the party
        for (User user : party.getMembers()) {
            user.setPartyID(null);
            user.setInParty(false);
            user.setPartyOnly(false);
            user.setPartyChatToggle(false);
            Player member = user.getPlayer();
            if (member != null && member.isOnline())
                sendMessageWithReplacement(member, "party-disbanded", "%partyName%", party.getName());
        }
        PartyChat.getPartyHandler().deleteParty(party);
        PartyChat.getPartyHandler().removeFromPartyMap(party.getId());

        return true;
    }
}
