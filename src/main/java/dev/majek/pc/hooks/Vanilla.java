package dev.majek.pc.hooks;

import dev.majek.pc.PartyChat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.UUID;
import java.util.logging.Level;

public class Vanilla {

    /**
     * Check if a player is vanished
     *
     * @param player the player to check
     * @return true if vanished
     */
    public static boolean isVanished(Player player) {
        try {

            for (MetadataValue meta: player.getMetadata("vanished")) {
                if (meta.asBoolean())
                    return true;
            }

            return false;
        } catch (Exception ex) {
            PartyChat.error("Error checking if player is vanished:");
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Check if a player is vanilla banned
     *
     * @param uuid player's unique id
     * @return true if banned
     */
    public static boolean isVanillaBanned(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid).isBanned();
    }

}
