package dev.majek.partychat.command;

import dev.majek.partychat.PartyChat;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

public class CommandReload implements CommandExecutor {

    PluginDescriptionFile pdf = PartyChat.instance.getDescription();

    // Used to reload the party chat plugin
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (cmd.getName().equalsIgnoreCase("pcreload")) {
            if (sender.hasPermission("partychat.admin")) {
                /*
                 These first four are the only important lines
                 The rest is all formatting to look cool :D
                 */
                PartyChat.instance.getPluginLoader().disablePlugin(PartyChat.instance);
                sender.sendMessage(ChatColor.DARK_GRAY  + "[LOG]" + ChatColor.RED + " Disabling PartyChat plugin...");
                PartyChat.instance.getPluginLoader().enablePlugin(PartyChat.instance);
                PartyChat.instance.reloadConfig(); PartyChat.messageData.reloadConfig();
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () ->
                        sender.sendMessage(ChatColor.DARK_GRAY  + "[LOG]" + ChatColor.GREEN  +
                                " Enabling PartyChat plugin..."), 10L);
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () ->
                        sender.sendMessage(ChatColor.DARK_GRAY  + "[LOG]" + ChatColor.GRAY +
                                " Successfully loaded PartyChat version " + pdf.getVersion()), 30L);
            } else {
                CommandParty.sendMessageWithPrefix((Player) sender, "&cYou do not have permission to use this command.");
            }
            return true;
        }
        return false;
    }
}
