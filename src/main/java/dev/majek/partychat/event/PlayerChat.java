package dev.majek.partychat.event;

import dev.majek.partychat.command.CommandPartySpy;
import dev.majek.partychat.PartyChat;
import dev.majek.partychat.command.CommandPartyChat;
import dev.majek.partychat.data.Party;
import dev.majek.partychat.data.Restrictions;
import dev.majek.partychat.hooks.Essentials;
import dev.majek.partychat.hooks.Vault;
import dev.majek.partychat.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerChat implements Listener {

    public static FileConfiguration m = PartyChat.messageData.getConfig();
    public static FileConfiguration c = PartyChat.getInstance().getConfig();
    public static boolean fromCommandPartyChat = false;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (Party.inParty(player) && CommandPartyChat.partyChat.get(player) && !fromCommandPartyChat) {
            event.setCancelled(true);
            Party party = Party.getParty(player);
            String message = event.getMessage();
            String format = event.getFormat();
            if (PartyChat.debug) {
                player.sendMessage("Format: " + format + " Message: " + message + " Party Name: " + party.name + " Display Name: " + player.getDisplayName());
                Bukkit.getConsoleSender().sendMessage("Format: " + format + " Message: " + message + " Party Name: " + party.name);
            }
            List<Player> messageReceived = new ArrayList<>(); // This is used so staff don't get the message twice

            // Check if the player is muted - don't allow chat if they are
            if (Restrictions.isMuted(player)) {
                Chat.format(m.getString("muted")); return;
            }

            PartyChat.debug(player, "AsyncPlayerChatEvent", CommandPartyChat.partyChat.get(player), "Party"); // Debug

            if (c.getBoolean("console-log")) // Log message to console
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PCSPY] [" + Party.getRawName(party) + ChatColor.RED + "] "
                        + player.getName() + ": " + message);

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
                    staff.sendMessage(Chat.format((m.getString("spy-format") +  " " + message)
                            .replace("%partyName%", Chat.removeColorCodes(party.name))
                            .replace("%player%", Chat.removeColorCodes(player.getName()))));
            }
        } else {
            if (c.getBoolean("format-chat")) {
                event.setMessage(Chat.colorize(event.getMessage())); // Add colors to all messages sent in chat
            }
            PartyChat.debug(player, "AsyncPlayerChatEvent", CommandPartyChat.partyChat.get(player), "Chat"); // Debug
        }
    }
}
