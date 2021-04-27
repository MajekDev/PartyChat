package dev.majek.pc.data;

import dev.majek.pc.PartyChat;
import dev.majek.pc.hooks.AdvanceBan;
import dev.majek.pc.hooks.Essentials;
import dev.majek.pc.hooks.LiteBans;
import dev.majek.pc.hooks.Vanilla;
import org.bukkit.entity.Player;

/**
 * Handles checking if a player is banned, muted, vanished, etc.
 */
public class Restrictions {

    /**
     * Check if a player is muted by any of our hooked plugins
     *
     * @param player the player to check
     * @return true if muted
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isMuted(Player player) {
        boolean muted = false;
        if (PartyChat.hasEssentials)
            muted = Essentials.isEssentialsMuted(player.getUniqueId());
        if (PartyChat.hasLiteBans)
            muted = LiteBans.isLiteBansMuted(player.getUniqueId(), player.getAddress().getHostString());
        //if (PartyChat.hasAdvancedBan)
            //muted = AdvanceBan.isAdvanceBanMuted(player.getUniqueId());
        return muted;
    }

    /**
     * Check if a player is banned by any of our hooked plugins or by vanilla command
     *
     * @param player the player to check
     * @return true if banned
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isBanned(Player player) {
        boolean banned = false;
        if (PartyChat.hasEssentials)
            banned = Essentials.isEssentialsBanned(player.getUniqueId());
        if (PartyChat.hasLiteBans)
            banned = LiteBans.isLiteBansBanned(player.getUniqueId(), player.getAddress().getHostString());
        //if (PartyChat.hasAdvancedBan)
            //banned = AdvanceBan.isAdvanceBanBanned(player.getUniqueId());
        return banned;
    }

    /**
     * Check if a player is vanished
     *
     * @param player the player to check
     * @return true if vanished
     */
    public static boolean isVanished(Player player) {
        return Vanilla.isVanished(player) || (PartyChat.hasEssentials && Essentials.isEssentialsVanished(player.getUniqueId()));
    }

    public static boolean containsCensoredWord(String string) {
        boolean contains = false;
        for (String censorWord : PartyChat.getDataHandler().censorWords) {
            if (string.contains(censorWord)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

}
