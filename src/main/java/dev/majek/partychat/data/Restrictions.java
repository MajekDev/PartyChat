package dev.majek.partychat.data;

import dev.majek.partychat.PartyChat;
import dev.majek.partychat.hooks.AdvanceBan;
import dev.majek.partychat.hooks.Essentials;
import dev.majek.partychat.hooks.LiteBans;
import dev.majek.partychat.hooks.Vanilla;
import org.bukkit.entity.Player;

public class Restrictions {

    /**
     * Check if a player is muted by any of our hooked plugins
     *
     * @param player the player to check
     * @return true if muted
     */
    public static boolean isMuted(Player player) {
        if (PartyChat.hasEssentials)
            return Essentials.isEssentialsMuted(player.getUniqueId());
        if (PartyChat.hasLiteBans)
            return LiteBans.isLiteBansMuted(player.getUniqueId(), player.getAddress().getHostString());
        if (PartyChat.hasAdvancedBan)
            return AdvanceBan.isAdvanceBanMuted(player.getUniqueId());
        return false;
    }

    /**
     * Check if a player is banned by any of our hooked plugins or by vanilla command
     *
     * @param player the player to check
     * @return true if banned
     */
    public static boolean isBanned(Player player) {
        if (PartyChat.hasEssentials)
            return Essentials.isEssentialsBanned(player.getUniqueId());
        if (PartyChat.hasLiteBans)
            return LiteBans.isLiteBansBanned(player.getUniqueId(), player.getAddress().getHostName());
        if (PartyChat.hasAdvancedBan)
            return AdvanceBan.isAdvanceBanBanned(player.getUniqueId());
        return false;
    }

    /**
     * Check if a player is vanished
     *
     * @param player the player to check
     * @return true if vanished
     */
    public static boolean isVanished(Player player) {
        return Vanilla.isVanished(player);
    }

}
