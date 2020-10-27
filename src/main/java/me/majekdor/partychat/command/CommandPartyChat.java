package me.majekdor.partychat.command;

import me.majekdor.partychat.PartyChat;
import me.majekdor.partychat.data.Party;
import me.majekdor.partychat.listener.PlayerChat;
import me.majekdor.partychat.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandPartyChat implements CommandExecutor, TabCompleter {

    public static Map<Player, Boolean> partyChat = new HashMap<>();
    public static FileConfiguration c = PartyChat.getInstance().getConfig();
    public static FileConfiguration m = PartyChat.messageData.getConfig();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("partychat")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This is not a console command."); return true;
            }
            Player player = (Player) sender;

            // Check if the admins wants to use permissions
            if (c.getBoolean("use-permissions"))
                if (!player.hasPermission("partychat-use")) {
                    player.sendMessage(Chat.format(m.getString("no-permission"))); return true;
                }

            // Check if the player is not in a party
            if (!Party.inParty.containsKey(player)) {
                player.sendMessage(Chat.format(m.getString("not-in-party"))); return true;
            }

            Party party = Party.getParty(player);

            // Toggle party chat
            if (args.length == 0) {
                if (!partyChat.get(player)) {
                    partyChat.replace(player, true);
                    player.sendMessage(Chat.format(m.getString("pc-enabled")));
                } else {
                    partyChat.replace(player, false);
                    player.sendMessage(Chat.format(m.getString("pc-disabled")));
                }
            }

            // Send message to party chat if in party, to normal chat if not
            if (args.length > 0) {
                StringBuilder message = new StringBuilder();
                for (String arg : args) {
                    message.append(arg).append(" ");
                }
                if (!partyChat.get(player)) { // Send message to party chat
                    List<Player> messageReceived = new ArrayList<>(); // This is used so staff don't get the message twice

                    if (c.getBoolean("console-log")) // Log message to console
                        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PCSPY] [" + Party.getRawName(party) + ChatColor.RED + "] "
                                + player.getName() + ": " + message);

                    PartyChat.debug(player, "CommandPartyChat", partyChat.get(player), "Party"); // Debug

                    // Send message to party members
                    for (Player member : party.members) {
                        messageReceived.add(member);
                        member.sendMessage(Chat.format((m.getString("message-format") + message)
                                .replace("%partyName%", party.name)
                                .replace("%player%", player.getDisplayName())));
                    }

                    // Send message to staff members
                    for (Player staff : PartyChat.serverStaff) {
                        if ((!messageReceived.contains(staff)) && CommandPartySpy.spyToggle.get(staff))
                            staff.sendMessage(Chat.format((m.getString("spy-format") + " " + message)
                                    .replace("%partyName%", Chat.removeColorCodes(party.name))
                                    .replace("%player%", Chat.removeColorCodes(player.getName()))));
                    }
                } else { // Send message to normal chat
                    PlayerChat.fromCommandPartyChat = true;
                    player.chat(message.toString());
                    PartyChat.debug(player, "CommandPartyChat", partyChat.get(player), "Chat"); // Debug
                }
            }
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String s, @NotNull String[] strings) throws IllegalArgumentException {
        return Collections.emptyList();
    }
}
