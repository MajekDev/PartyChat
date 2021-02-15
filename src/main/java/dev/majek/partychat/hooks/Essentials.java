package dev.majek.partychat.hooks;

import com.earth2me.essentials.User;
import dev.majek.partychat.PartyChat;
import net.ess3.api.IEssentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;

public class Essentials {

    /**
     * Check if the player is muted by Essentials
     *
     * @param uuid player's unique id
     * @return true if muted
     */
    public static boolean isEssentialsMuted(UUID uuid) {
        try {
            if (PartyChat.hasEssentials) {
                com.earth2me.essentials.Essentials essentials = (com.earth2me.essentials.Essentials)
                        Bukkit.getPluginManager().getPlugin("Essentials");
                return essentials.getUser(uuid).isMuted();
            }
            return false;
        } catch (Exception ex) {
            PartyChat.instance.getLogger().log(Level.SEVERE, "Error checking if player is muted by Essentials:");
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
    public static boolean isEssentialsBanned(UUID uuid) {
        try {
            if (PartyChat.hasEssentials) {
                com.earth2me.essentials.Essentials essentials = (com.earth2me.essentials.Essentials)
                        Bukkit.getPluginManager().getPlugin("Essentials");
                return essentials.getUser(uuid).getBase().isBanned();
            }
            return false;
        } catch (Exception ex) {
            PartyChat.instance.getLogger().log(Level.SEVERE, "Error checking if player is banned by Essentials:");
            ex.printStackTrace();
        }
        return false;
    }

    public static String getFormat(Player player) {
        IEssentials essentials = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentials == null)
            throw new NullPointerException("Error hooking into essentials plugin");
        User user = essentials.getUser(player);
        String group = user.getGroup();
        String world = user.getWorld().getName();
        Team team = user.getBase().getScoreboard().getPlayerTeam(user.getBase());

        String format = essentials.getSettings().getChatFormat(group);
        format = format.replace("{0}", group);
        format = format.replace("{1}", world);
        format = format.replace("{2}", world.substring(0, 1).toUpperCase(Locale.ENGLISH));
        format = format.replace("{3}", team == null ? "" : team.getPrefix());
        format = format.replace("{4}", team == null ? "" : team.getSuffix());
        format = format.replace("{5}", team == null ? "" : team.getDisplayName());
        return format;
    }

}
