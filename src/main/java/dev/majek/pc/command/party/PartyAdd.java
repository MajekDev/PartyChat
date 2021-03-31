package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.util.Pair;
import dev.majek.pc.util.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PartyAdd extends PartyCommand {

    public PartyAdd() {
        super(
                "add", getSubCommandUsage("add"), getSubCommandDescription("add"),
                getConfigBoolean("only-leader-can-add"), getSubCommandDisabled("add"),
                getSubCommandCooldown("add"), getSubCommandAliases("add")
        );
    }

    @Override
    public boolean execute(Player player, String[] args, boolean leftServer) {

        // Make sure the player is actually in a party
        if (!PartyChat.getDataHandler().getUser(player).isInParty()) {
            sendMessage(player, "not-in-party"); return false;
        }

        // Make sure the player specifies the player they wish to invite
        if (args.length == 1) {
            sendMessage(player, "specify-player"); return false;
        }

        return execute(player, args[1]);
    }

    public static boolean execute(Player player, String name) {

        Party party = PartyChat.getPartyHandler().getParty(PartyChat.getDataHandler().getUser(player));

        // This should never happen, but I want to know if it does
        if (party == null) {
            PartyChat.error("Error: PC-ADD_1 | The plugin is fine, but please report this error " +
                    "code here: https://discord.gg/CGgvDUz");
            sendMessage(player, "error"); return false;
        }

        // Try to get specified player to invite
        Player invited = Bukkit.getPlayer(name);
        if (invited == null) {
            sendMessage(player, "not-online"); return false;
        }

        // Player did /party add <their name>
        if (invited == player) {
            sendMessage(player, "add-self"); return false;
        }

        // Check if the player is trying to invite someone who is already in the party
        for (User user : party.getMembers()) {
            Player member = user.getPlayer();
            if (member == invited) {
                sendMessage(player, "player-in-party"); return false;
            }
        }

        // Passed all checks, send messages
        for (String message : PartyChat.getDataHandler().getConfigStringList(PartyChat
                .getDataHandler().messages, "invite-message")) {
            TextUtils.sendFormatted(invited, (message).replace("%prefix%", PartyChat.getDataHandler()
                    .getConfigString(PartyChat.getDataHandler().messages, "other-format-prefix"))
                    .replace("%partyName%", party.getRawName())
                    .replace("%player%", player.getDisplayName()));
        }
        sendMessageWithReplacement(player, "invite-sent", "%player%", invited.getDisplayName());
        Player leader = Bukkit.getPlayer(party.getLeader());
        if (leader != null && leader.isOnline() && player != leader)
            sendMessageWithReplacement(leader, "invite-sent", "%player%", invited.getDisplayName());
        party.addPendingInvitation(invited, player);

        // Check after the expire time if the player still hasn't accepted or declined
        int expireTime = PartyChat.getDataHandler().getConfigInt(mainConfig, "invite-expire-time");
        if (expireTime != -1) {
            runTaskLater(expireTime, () -> {
                for (Pair<Player, Player> players : party.getPendingInvitations())
                    if (players.getFirst() == invited) {
                        party.removePendingInvitation(invited);
                        sendMessage(invited, "expired-invite");
                    }
            });
        }

        return true;
    }
}
