package Me.Majekdor.PartyChat.util;

import Me.Majekdor.PartyChat.PartyChat;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chat {
    // Add PartyChat prefix to the message
    public static String format(String msg) {
        FileConfiguration m = PartyChat.messageData.getConfig();
        String prefix = m.getString("prefix");
        if (prefix == null)
            Bukkit.getConsoleSender().sendMessage("Error in the messages.yml file. You deleted something.");
        return Chat.colorize(msg.replace("%prefix%", prefix));
    }

    public static final Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
    /**
     * Apply standard Minecraft color codes (&a) and hex color codes (#aabbcc)
     * First locates and translates hex color codes, then translates Minecraft codes and returns
     *
     * @param msg raw string with color codes
     */
    public static String colorize(String msg) {
        if (Bukkit.getVersion().contains("1.16")) {
            Matcher match = pattern.matcher(msg);
            while (match.find()) {
                String color = msg.substring(match.start(), match.end());
                msg = msg.replace(color, ChatColor.of(color) + "");
                match = pattern.matcher(msg);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    /**
     * Removes standard Minecraft color codes (&a) and hex color codes (#aabbcc)
     *
     * @param msg the string to strip colors from
     * @return sb.toString() the same string without color code characters
     */
    public static String removeColorCodes(String msg) {
        StringBuilder sb = new StringBuilder(msg.length());
        char[] chars =  msg.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] == '&' || chars[i] == org.bukkit.ChatColor.COLOR_CHAR) {
                ++i; continue;
            }
            if (chars[i] == '#') {
                i += 6; continue;
            }
            sb.append(chars[i]);
        }
        return sb.toString();
    }
}
