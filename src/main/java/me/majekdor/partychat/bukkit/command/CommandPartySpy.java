package me.majekdor.partychat.bukkit.command;

import me.majekdor.partychat.bukkit.PartyChat;
import me.majekdor.partychat.bukkit.util.Chat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CommandPartySpy implements CommandExecutor {

    public static FileConfiguration m = PartyChat.messageData.getConfig();
    public static Map<Player, Boolean> spyToggle = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (command.getName().equalsIgnoreCase("spy")) {

            // All party messages are already logged to console... no point giving it /spy access
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This is not a console command.");
                return true;
            }
            Player player = (Player) sender;

            // Check if the player has permission to use this command
            if (!player.hasPermission("partychat.admin")) {
                player.sendMessage(Chat.format(m.getString("no-permission"))); return true;
            }

            // Check toggle and swap
            if (spyToggle.get(player)) {
                spyToggle.replace(player, false);
                player.sendMessage(Chat.format(m.getString(("spy-disabled"))));
            } else {
                spyToggle.replace(player, true);
                player.sendMessage(Chat.format((m.getString("spy-enabled"))));
            }
            return true;

        }
        return false;
    }
}
