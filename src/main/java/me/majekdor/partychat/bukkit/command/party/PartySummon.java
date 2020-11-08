package me.majekdor.partychat.bukkit.command.party;

import me.majekdor.partychat.bukkit.PartyChat;
import me.majekdor.partychat.bukkit.command.CommandParty;
import me.majekdor.partychat.bukkit.data.Party;
import me.majekdor.partychat.bukkit.util.TextUtils;
import me.majekdor.partychat.bukkit.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class PartySummon extends CommandParty {

    public static void execute(Player player) {

        // Check if the player is not in a party
        if (!Party.inParty.containsKey(player.getUniqueId())) {
            sendMessageWithPrefix(player, m.getString("not-in-party")); return;
        }

        Party party = Party.getParty(player);

        // Check if the player is not the party leader
        if (!player.getUniqueId().equals(party.leader)) {
            sendMessageWithPrefix(player, m.getString("not-leader")); return;
        }

        // Make sure the leader is in a safe location
        if (!Utils.isSafe(player.getLocation())) {
            sendMessageWithPrefix(player, m.getString("location-unsafe")); return;
        }

        // Send summons to all members
        for (UUID memberUUID : party.members) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member == null) continue;
            if (member == player) continue;
            for (String summons : m.getStringList("summon-request")) {
                TextUtils.sendFormatted(member, summons.replace("%prefix%",
                        Objects.requireNonNull(m.getString("other-format-prefix")))
                        .replace("%player%", Objects.requireNonNull(Bukkit.getPlayer(party.leader)).getDisplayName()));
            }
            party.pendingSummons.add(member);
            int delay = c.getInt("expire-time") * 20;
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () -> {
                party.pendingSummons.remove(member);
                if (party.pendingSummons.contains(player))
                    sendMessageWithPrefix(member, m.getString("teleport-timeout"));
            }, delay);
        }
        sendMessageWithPrefix(player, m.getString("summon-sent"));
    }
}
