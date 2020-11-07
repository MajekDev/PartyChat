package me.majekdor.partychat.event;

import me.majekdor.partychat.PartyChat;
import me.majekdor.partychat.command.CommandPartyChat;
import me.majekdor.partychat.command.CommandPartySpy;
import me.majekdor.partychat.data.Party;
import me.majekdor.partychat.data.Restrictions;
import me.majekdor.partychat.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerChat implements Listener {

    public static FileConfiguration m = PartyChat.messageData.getConfig();
    public static FileConfiguration c = PartyChat.getInstance().getConfig();
    public static boolean fromCommandPartyChat = false;

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (Party.inParty.containsKey(player.getUniqueId()) && CommandPartyChat.partyChat.get(player) && !fromCommandPartyChat) {
            event.setCancelled(true); String message = event.getMessage();
            Party party = Party.getParty(player);
            List<Player> messageReceived = new ArrayList<>(); // This is used so staff don't get the message twice

            // Check if the player is muted - don't allow chat if they are
            if (Restrictions.isMuted(player)) {
                Chat.format(m.getString("muted")); return;
            }

            PartyChat.debug(player, "AsyncPlayerChatEvent", CommandPartyChat.partyChat.get(player), "Party"); // Debug

            if (c.getBoolean("console-log")) // Log message to console
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PCSPY] [" + Party.getRawName(party) + ChatColor.RED + "] "
                        + player.getName() + ": " + message);

            // Send message to party members
            for (UUID memberUUID : party.members) {
                Player member = Bukkit.getPlayer(memberUUID);
                if (member == null) continue;
                messageReceived.add(member);
                member.sendMessage(Chat.format((m.getString("message-format") + message)
                        .replace("%partyName%", party.name)
                        .replace("%player%", player.getDisplayName())));
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
