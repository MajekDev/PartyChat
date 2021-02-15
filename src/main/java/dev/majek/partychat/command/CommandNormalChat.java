package dev.majek.partychat.command;

import dev.majek.partychat.PartyChat;
import dev.majek.partychat.event.PlayerChat;
import dev.majek.partychat.util.Chat;
import dev.majek.partychat.data.Restrictions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandNormalChat implements CommandExecutor, TabCompleter {

    public static FileConfiguration c = PartyChat.getInstance().getConfig();
    public static FileConfiguration m = PartyChat.messageData.getConfig();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This is not a console command."); return true;
        }
        Player player = (Player) sender;

        // Check if the admins wants to use permissions
        if (c.getBoolean("use-permissions"))
            if (!player.hasPermission("partychat-use")) {
                player.sendMessage(Chat.format(m.getString("no-permission"))); return true;
            }

        if (args.length == 0) {
            return false;
        }

        // Check if the player is muted - don't allow chat if they are
        if (Restrictions.isMuted(player)) {
            player.sendMessage(Chat.format(m.getString("muted"))); return true;
        }

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }

        PlayerChat.fromCommandPartyChat = true;
        player.chat(Chat.colorize(message.toString()));
        PlayerChat.fromCommandPartyChat = false;
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException{
        return Collections.emptyList();
    }
}
