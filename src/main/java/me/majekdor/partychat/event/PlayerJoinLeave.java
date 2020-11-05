package me.majekdor.partychat.event;

import me.majekdor.partychat.PartyChat;
import me.majekdor.partychat.command.CommandPartyChat;
import me.majekdor.partychat.command.CommandPartySpy;
import me.majekdor.partychat.command.party.PartyLeave;
import me.majekdor.partychat.data.Party;
import me.majekdor.partychat.util.Chat;
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
        if (Party.inParty.containsKey(event.getPlayer().getUniqueId())) {
            if (!(c.getBoolean("persistent-parties"))) {
                PartyLeave.execute(event.getPlayer(), true);
            }
        }
    }
}
