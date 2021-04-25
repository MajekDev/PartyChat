package dev.majek.pc.mechanic;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.Restrictions;
import dev.majek.pc.data.object.User;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit extends Mechanic {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        User user = PartyChat.getDataHandler().getUser(event.getPlayer());
        if (user.isInParty() && !PartyChat.getDataHandler().getConfigBoolean(PartyChat.getDataHandler().mainConfig, "persistent-parties"))
            PartyChat.getCommandHandler().getCommand("leave").execute(user.getPlayer(), new String[0], true);

        // Remove the player from the party if they left the server due to a ban
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PartyChat.getCore(), () -> {
            if (Restrictions.isBanned(event.getPlayer()))
                PartyChat.getCommandHandler().getCommand("leave").execute(user.getPlayer(), new String[0], true);
        }, 20L); // Give hooked plugins time to figure their shit out
    }
}
