package me.majekdor.partychat.bukkit.command.party;

import me.majekdor.partychat.bukkit.PartyChat;
import me.majekdor.partychat.bukkit.command.CommandParty;
import me.majekdor.partychat.bukkit.data.Party;
import me.majekdor.partychat.bukkit.util.Chat;
import me.majekdor.partychat.bukkit.util.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class PartyAdd extends CommandParty {

    public static void execute(Player player, String[] args) {

        // Check if the player is not in a party
        if (!(Party.inParty.containsKey(player.getUniqueId()))) {
            sendMessageWithPrefix(player, m.getString("not-in-party")); return;
        }

        // Make sure the player specifies another player to invite
        if (args.length == 1) {
            sendMessageWithPrefix(player, m.getString("specify-player")); return;
        }

        execute(player, args[1]);
    }

    public static void execute(Player player, String newPlayer) {
        Party party  = Party.getParty(player);

        // Try to get the specified player, return if not online
        Player invitee = Bukkit.getPlayer(newPlayer);
        if (invitee == null) {
            sendMessageWithPrefix(player, m.getString("not-online")); return;
        }

        // Player is trying to add themself *facepalm*
        if (invitee == player) {
            sendMessageWithPrefix(player, m.getString("add-self")); return;
        }

        // Player is trying to invite a player already in the party
        for (UUID memberUUID : party.members) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member == invitee) {
                sendMessageWithPrefix(player, m.getString("player-in-party")); return;
            }
        }

        // Passed all the checks - send message to player and invitee
        for (String inv : m.getStringList("invite-message")) {
            TextUtils.sendFormatted(invitee, (inv).replace("%partyName%", Chat.removeColorCodes(party.name))
                    .replace("%player%", player.getDisplayName())
                    .replace("%prefix%", Objects.requireNonNull(m.getString("other-format-prefix"))));
        }
        sendMessageWithPrefix(player, (m.getString("invite-sent") + "")
                .replace("%player%", invitee.getDisplayName()));
        party.pendingInvitations.add(invitee);

        // Invite expires after the time defined in the config file
        int delay = c.getInt("expire-time") * 20;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () -> {
            if (party.pendingInvitations.contains(invitee)) {
                party.pendingInvitations.remove(invitee);
                sendMessageWithPrefix(invitee, m.getString("expired-invite"));
            }
        }, delay);
    }
}
