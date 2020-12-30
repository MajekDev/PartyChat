package dev.majek.partychat.event;

import dev.majek.partychat.command.CommandPartySpy;
import dev.majek.partychat.PartyChat;
import dev.majek.partychat.command.CommandPartyChat;
import dev.majek.partychat.command.party.PartyLeave;
import dev.majek.partychat.data.Party;
import dev.majek.partychat.data.Restrictions;
import dev.majek.partychat.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinLeave implements Listener {

    public static FileConfiguration c = PartyChat.getInstance().getConfig();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (CommandPartyChat.partyChat.containsKey(player))
            CommandPartyChat.partyChat.replace(player, false);
        else
            CommandPartyChat.partyChat.put(player, false);

        if (player.hasPermission("partychat.admin")) {
            if (!PartyChat.serverStaff.contains(player))
                PartyChat.serverStaff.add(player);
            CommandPartySpy.spyToggle.put(player, c.getBoolean("auto-spy"));
            if (PartyChat.hasUpdate) {
                player.sendMessage(Chat.colorize("&6There is a new &bParty&eChat &6update available!"));
                player.sendMessage(Chat.colorize("&6Download it here: &chttps://www.spigotmc.org/resources/partychat.79295/"));
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (Party.inParty(event.getPlayer())) {
            if (!(c.getBoolean("persistent-parties"))) {
                PartyLeave.execute(event.getPlayer(), true);
            }
        }

        // Remove the player from the party if they left the server due to a ban
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () -> {
            if (Restrictions.isBanned(event.getPlayer()))
                PartyLeave.execute(event.getPlayer(), false);
        }, 20L); // Give hooked plugins time to figure their shit out
    }
}
