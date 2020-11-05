package me.majekdor.partychat.util;

import me.majekdor.partychat.PartyChat;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chat {

    public static final Pattern pattern6 = Pattern.compile("&#[a-fA-F0-9]{6}");
    public static final Pattern pattern3 = Pattern.compile("&#[a-fA-F0-9]{3}");
    public static List<Integer> formatted = new ArrayList<>();
    public static final List<org.bukkit.ChatColor> ILLEGAL_COLORS = Arrays.asList(org.bukkit.ChatColor.MAGIC, org.bukkit.ChatColor.BLACK);
    public static final List<Character> COLOR_CHARS = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'a', 'b', 'c', 'd', 'e', 'f', 'l', 'm', 'n', 'o', 'r', 'x');

    /**
     * Apply standard Minecraft color codes (&a) and hex color codes (#aabbcc)
     * First locates and translates hex color codes, then translates Minecraft codes and returns
     *
     * @param message raw string with color codes
     */
    public static String colorize(String message) {
        if (Bukkit.getVersion().contains("1.16")) {
            message = applyBukkitColors(message);
            for (org.bukkit.ChatColor color : ILLEGAL_COLORS) {
                message = message.replaceAll(org.bukkit.ChatColor.COLOR_CHAR + Character.toString(color.getChar()), "");
            }
            message = sixChar(message);
            message = threeChar(message);
            formatted.clear();
        } else {
            message = applyBukkitColors(message);
        }
        return message;
    }

    /**
     * Search for six character hex codes and format them
     *
     * @param message to format
     * @return formatted message
     */
    public static String sixChar(String message) {
        Matcher match6 = pattern6.matcher(message);
        while (match6.find()) {
            String color = message.substring(match6.start()+1, match6.end());
            if (!formatted.contains(match6.start())) {
                if (getLuma(color) > 16) {
                    StringBuilder sb = new StringBuilder(message);
                    sb.delete(match6.start(), match6.start() + 8);
                    sb.insert(match6.start(), ChatColor.of(color));
                    message = sb.toString();
                } else {
                    StringBuilder sb = new StringBuilder(message);
                    sb.delete(match6.start(), match6.start() + 8);
                    message = sb.toString();
                }
                formatted.add(match6.start());
                match6 = pattern6.matcher(message);
            }
        }
        return message;
    }

    /**
     * Search for three character hex codes and format them
     *
     * @param message to format
     * @return formatted message
     */
    public static String threeChar(String message) {
        Matcher match3 = pattern3.matcher(message);
        while (match3.find()) {
            String color = message.substring(match3.start()+1, match3.end());
            char[] chars = color.toCharArray();
            String newColor = String.valueOf(chars[0]) + chars[1] + chars[1] + chars[2] + chars[2] + chars[3] + chars[3];
            if (!formatted.contains(match3.start())) {
                if (getLuma(newColor) > 16) {
                    StringBuilder sb2 = new StringBuilder(message);
                    sb2.delete(match3.start(), match3.start() + 5);
                    sb2.insert(match3.start(), ChatColor.of(newColor));
                    message = sb2.toString();
                } else {
                    StringBuilder sb2 = new StringBuilder(message);
                    sb2.delete(match3.start(), match3.start() + 5);
                    message = sb2.toString();
                }
                formatted.add(match3.start());
                match3 = pattern3.matcher(message);
            }
        }
        return message;
    }

    public static String applyBukkitColors(String msg) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', msg);
    }

    /**
     * Check for luminescence from a six char hex code
     *
     * @param color six char hex code (#aabbcc)
     * @return luminescence (0-256)
     */
    public static double getLuma(String color) {
        int red = Integer.valueOf(color.substring(1,3), 16);
        int green = Integer.valueOf(color.substring(3,5), 16);
        int blue = Integer.valueOf(color.substring(5,7), 16);
        return (0.2126 * red) + (0.7152 * green) + (0.0722 * blue);
    }

    /**
     * Removes standard Minecraft color codes (&a) and hex color codes (#aabbcc/#abc)
     *
     * @param message the string to strip colors from
     * @return sb.toString() the same string without color code characters
     */
    public static String removeColorCodes(String message) {
        Matcher match6 = pattern6.matcher(message);
        Matcher match3 = pattern3.matcher(message);
        while (match6.find()) {
            String color = message.substring(match6.start(), match6.end());
            message = message.replace(color, "");
            match6 = pattern6.matcher(message);
            match3 = pattern3.matcher(message);
        }
        while (match3.find()) {
            String color = message.substring(match3.start(), match3.end());
            message = message.replace(color, "");
            match3 = pattern3.matcher(message);
        }
        StringBuilder sb = new StringBuilder(message.length());
        char[] chars = message.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] == '&' || chars[i] == org.bukkit.ChatColor.COLOR_CHAR) {
                try {
                    if (COLOR_CHARS.contains(chars[i+1])) {
                        ++i;
                        continue;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // do nothing :)
                    // allows for removing color codes in tab complete methods
                }
            }
            sb.append(chars[i]);
        }
        return sb.toString();
    }

    // Add PartyChat prefix to the message
    public static String format(String msg) {
        FileConfiguration m = PartyChat.messageData.getConfig();
        String prefix = m.getString("prefix");
        if (prefix == null)
            Bukkit.getConsoleSender().sendMessage("Error in the messages.yml file. You deleted something.");
        assert prefix != null;
        return Chat.colorize(msg.replace("%prefix%", prefix));
    }
}
