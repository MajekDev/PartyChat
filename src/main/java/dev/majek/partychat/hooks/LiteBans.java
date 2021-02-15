package dev.majek.partychat.hooks;

import litebans.api.Database;
import dev.majek.partychat.PartyChat;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.logging.Level;

public class LiteBans {

    public static boolean muted;

    /**
     * Check if the player is muted by LiteBans
     *
     * @param uuid player's unique id
     * @param ipAddress player's ip address
     * @return true if muted
     */
    public static boolean isLiteBansMuted(UUID uuid, String ipAddress) {
        try {
            muted = false;
            if (PartyChat.hasLiteBans) {
                Bukkit.getScheduler().runTaskAsynchronously(PartyChat.instance, () -> {
                    muted = Database.get().isPlayerMuted(uuid, ipAddress);
                });
            }
            return muted;
        } catch (Exception ex) {
            PartyChat.instance.getLogger().log(Level.SEVERE, "Error checking if player is muted by LiteBans:");
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean banned;

    /**
     * Check if the player is banned by LiteBans
     *
     * @param uuid player's unique id
     * @param ipAddress player's ip address
     * @return true if banned
     */
    public static boolean isLiteBansBanned(UUID uuid, String ipAddress) {
        try {
            banned = false;
            if (PartyChat.hasLiteBans) {
                Bukkit.getScheduler().runTaskAsynchronously(PartyChat.instance, () -> {
                    banned = Database.get().isPlayerBanned(uuid, ipAddress);
                });
            }
            return banned;
        } catch (Exception ex) {
            PartyChat.instance.getLogger().log(Level.SEVERE, "Error checking if player is banned by LiteBans:");
            ex.printStackTrace();
            return false;
        }
    }
}
