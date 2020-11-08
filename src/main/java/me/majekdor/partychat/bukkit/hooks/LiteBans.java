package me.majekdor.partychat.bukkit.hooks;

import litebans.api.Database;
import me.majekdor.partychat.bukkit.PartyChat;

import java.util.UUID;
import java.util.logging.Level;

public class LiteBans {

    /**
     * Check if the player is muted by LiteBans
     *
     * @param uuid player's unique id
     * @param ipAddress player's ip address
     * @return true if muted
     */
    public static boolean isLiteBansMuted(UUID uuid, String ipAddress) {
        try {
            if (PartyChat.hasLiteBans)
                return Database.get().isPlayerMuted(uuid, ipAddress);
            return false;
        } catch (Exception ex) {
            PartyChat.instance.getLogger().log(Level.SEVERE, "Error checking if player is muted by LiteBans:");
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Check if the player is banned by LiteBans
     *
     * @param uuid player's unique id
     * @param ipAddress player's ip address
     * @return true if banned
     */
    public static boolean isLiteBansBanned(UUID uuid, String ipAddress) {
        try {
            if (PartyChat.hasLiteBans)
                return Database.get().isPlayerBanned(uuid, ipAddress);
            return false;
        } catch (Exception ex) {
            PartyChat.instance.getLogger().log(Level.SEVERE, "Error checking if player is banned by LiteBans:");
            ex.printStackTrace();
        }
        return false;
    }

}
