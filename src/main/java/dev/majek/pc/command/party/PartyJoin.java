package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.Party;
import org.bukkit.entity.Player;

public class PartyJoin extends PartyCommand {

    public PartyJoin() {
        super(
                "join", getSubCommandUsage("join"), getSubCommandDescription("join"),
                false, getSubCommandDisabled("join"), getSubCommandCooldown("join"),
                getSubCommandAliases("join")
        );
    }

    @Override
    public boolean execute(Player player, String[] args, boolean leftServer) {

        // Check if the player is already in a party
        if (PartyChat.getDataHandler().getUser(player).isInParty()) {
            sendMessage(player, "in-party");
            return false;
        }

        // Player needs to specify a party to join
        if (args.length == 1) {
            sendMessage(player, "specify-party");
            return false;
        }
        return execute(player, args[1]);
    }

    public static boolean execute(Player player, String partyName) {
        // Try to find the party from the name
        Party findParty = null;
        for (Party check : PartyChat.getPartyHandler().getPartyMap().values())
            if (check.getRawName().equalsIgnoreCase(partyName)) {
                findParty = check;
                break;
            }

        // Make sure the specified party exists
        if (findParty == null) {
            sendMessage(player, "unknown-party");
            return false;
        }

        Party party = findParty;

        // Make sure the party is public
        if (!party.isPublic()) {
            sendMessage(player, "party-private");
            return false;
        }

        // Check if the player is blocked from joining the party
        if (party.getBlockedPlayers().contains(player)) {
            sendMessage(player, "join-wait");
            return false;
        }

        Player leader = party.getLeader().getPlayer();

        // Make sure the leader is online
        if (leader == null) {
            sendMessage(player, "leader-offline");
            return false;
        }

        // Send messages
        for (String request : PartyChat.getDataHandler().getConfigStringList(PartyChat
                .getDataHandler().messages, "request-join"))
            sendFormattedMessage(leader, request.replace("%prefix%", PartyChat.getDataHandler()
                    .getConfigString(PartyChat.getDataHandler().messages, "prefix"))
                    .replace("%player%", PartyChat.getDataHandler().getUser(player).getNickname()));
        sendMessage(player, "request-sent");

        party.addPendingJoinRequest(player);
        party.addBlockedPlayer(player);

        // Remove them from the blocked list after a configured amount of time
        int blockTime = PartyChat.getDataHandler().getConfigInt(mainConfig, "block-time");
        if (blockTime != -1)
            runTaskLater(blockTime, () -> party.removeBlockedPlayer(player));

        // Check after the expire time if the player still hasn't been accepted or declined
        int expireTime = PartyChat.getDataHandler().getConfigInt(mainConfig, "join-expire-time");
        if (expireTime != -1) {
            runTaskLater(expireTime, () -> {
                if (party.getPendingJoinRequests().contains(player)) {
                    party.removePendingJoinRequest(player);
                    sendMessage(player, "expired-join");
                }
            });
        }

        return true;
    }
}
