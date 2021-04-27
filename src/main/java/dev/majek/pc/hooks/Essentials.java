package dev.majek.pc.hooks;

import dev.majek.pc.PartyChat;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.logging.Level;

public class Essentials {

    /**
     * Check if the player is muted by Essentials
     *
     * @param uuid player's unique id
     * @return true if muted
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isEssentialsMuted(UUID uuid) {
        try {
            if (PartyChat.hasEssentials) {
                com.earth2me.essentials.Essentials essentials = (com.earth2me.essentials.Essentials)
                        Bukkit.getPluginManager().getPlugin("Essentials");
                return essentials.getUser(uuid).isMuted();
            }
            return false;
        } catch (Exception ex) {
            PartyChat.error("Error checking if player is muted by Essentials:");
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Check if the player is banned by Essentials
     *
     * @param uuid player's unique id
     * @return true if banned
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isEssentialsBanned(UUID uuid) {
        try {
            if (PartyChat.hasEssentials) {
                com.earth2me.essentials.Essentials essentials = (com.earth2me.essentials.Essentials)
                        Bukkit.getPluginManager().getPlugin("Essentials");
                return essentials.getUser(uuid).getBase().isBanned();
            }
            return false;
        } catch (Exception ex) {
            PartyChat.error("Error checking if player is banned by Essentials:");
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Check if the player is vanished by Essentials
     *
     * @param uuid player's unique id
     * @return true if vanished
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isEssentialsVanished(UUID uuid) {
        try {
            if (PartyChat.hasEssentials) {
                com.earth2me.essentials.Essentials essentials = (com.earth2me.essentials.Essentials)
                        Bukkit.getPluginManager().getPlugin("Essentials");
                return essentials.getUser(uuid).isVanished();
            }
            return false;
        } catch (Exception ex) {
            PartyChat.error("Error checking if player is vanished by Essentials:");
            ex.printStackTrace();
        }
        return false;
    }

}
