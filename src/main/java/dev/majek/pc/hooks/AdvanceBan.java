package dev.majek.pc.hooks;

import dev.majek.pc.PartyChat;
//import me.leoko.advancedban.manager.PunishmentManager;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Currently unused. AdvancedBan has been annoyingly finicky.
 */
public class AdvanceBan {

    /**
     * Check if the player is muted by AdvancedBan
     *
     * @param uuid player's unique id
     * @return true if muted
     */
    /*
    public static boolean isAdvanceBanMuted(UUID uuid) {
        try {
            if (PartyChat.hasAdvancedBan)
                return PunishmentManager.get().isMuted(uuid.toString());
            return false;
        } catch (Exception ex) {
            PartyChat.instance.getLogger().log(Level.SEVERE, "Error checking if player is muted by AdvancedBan:");
            ex.printStackTrace();
        }
        return false;
    }
     */

    /**
     * Check if the player is banned by AdvancedBan
     *
     * @param uuid player's unique id
     * @return true if banned
     */
    /*
    public static boolean isAdvanceBanBanned(UUID uuid) {
        try {
            if (PartyChat.hasEssentials)
                return PunishmentManager.get().isBanned(uuid.toString());
            return false;
        } catch (Exception ex) {
            PartyChat.instance.getLogger().log(Level.SEVERE, "Error checking if player is banned by AdvancedBan:");
            ex.printStackTrace();
        }
        return false;
    }
     */

}
