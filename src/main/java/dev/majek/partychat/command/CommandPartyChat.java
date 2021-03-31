package dev.majek.partychat.command;

import dev.majek.partychat.PartyChat;
import dev.majek.partychat.data.Party;
import dev.majek.partychat.hooks.DiscordSRVListener;
import dev.majek.partychat.hooks.Vault;
import dev.majek.partychat.util.Chat;
import dev.majek.partychat.data.Restrictions;
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
            // Ignore this whole section if they have admin perms
            if (c.getBoolean("use-permissions") && !player.hasPermission("partychat.admin"))
                if (!player.hasPermission("partychat.use")) {
                    CommandParty.sendMessageWithPrefix(player, m.getString("no-permission")); return true;
                }

            // Check if the player is not in a party
            if (!Party.inParty(player)) {
                player.sendMessage(Chat.format(m.getString("not-in-party"))); return true;
            }

            Party party = Party.getParty(player);

            // Toggle party chat
            if (args.length == 0) {
                if (!(partyChat.get(player))) {
                    partyChat.replace(player, true);
                    player.sendMessage(Chat.format(m.getString("pc-enabled")));
                } else {
                    partyChat.replace(player, false);
                    player.sendMessage(Chat.format(m.getString("pc-disabled")));
                }
                return true;
            }

            // Check if the player is muted - don't allow chat if they are
            if (Restrictions.isMuted(player)) {
                player.sendMessage(Chat.format(m.getString("muted"))); return true;
            }

            StringBuilder message = new StringBuilder();
            for (String arg : args) {
                message.append(arg).append(" ");
            }
            List<Player> messageReceived = new ArrayList<>(); // This is used so staff don't get the message twice

            if (c.getBoolean("console-log")) // Log message to console
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PCSPY] [" + Party.getRawName(party) + ChatColor.RED + "] "
                        + player.getName() + ": " + message);

            PartyChat.debug(player, "CommandPartyChat", partyChat.get(player), "Party"); // Debug

            String playerName;
            if (PartyChat.hasVault)
                playerName = Vault.getPlayerDisplayName(player);
            else
                playerName = player.getDisplayName();

            // Send message to party members
            for (UUID memberUUID : party.members) {
                Player member = Bukkit.getPlayer(memberUUID);
                if (member == null) continue;
                messageReceived.add(member);
                member.sendMessage(Chat.format((m.getString("message-format") + message)
                        .replace("%partyName%", party.name)
                        .replace("%player%", playerName)));
            }

            // Send message to staff members
            for (Player staff : PartyChat.serverStaff) {
                if ((!messageReceived.contains(staff)) && CommandPartySpy.spyToggle.get(staff))
                    staff.sendMessage(Chat.format((m.getString("spy-format") + " " + message)
                            .replace("%partyName%", Chat.removeColorCodes(party.name))
                            .replace("%player%", Chat.removeColorCodes(player.getName()))));
            }

            if (PartyChat.getInstance().getConfig().getBoolean("messages-to-discordsrv") && PartyChat.hasDiscordSRV) {
                DiscordSRVListener.sendMessage(player, message.toString());
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                                @NotNull String s, @NotNull String[] strings) throws IllegalArgumentException {
        return Collections.emptyList();
    }
}
