package dev.majek.pc.mechanic;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.Restrictions;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.util.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static dev.majek.pc.command.PartyCommand.sendMessage;

public class PlayerChat extends Mechanic {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        User user = PartyChat.getDataHandler().getUser(player);

        if (user.isInParty() && user.partyChatToggle()) {
            event.setCancelled(true);
            Party party = user.getParty();
            String message = event.getMessage();

            // This should never happen, but I want to know if it does
            if (party == null) {
                PartyChat.error("Error: PC-CHT_1 | The plugin is fine, but please report this error " +
                        "code here: https://discord.gg/CGgvDUz");
                sendMessage(user, "error");
                return;
            }

            if (Restrictions.isMuted(user.getPlayer())) {
                sendMessage(user, "muted");
                return;
            }

            PartyChat.getPartyHandler().sendMessageToPartyChat(party, user, message);
        } else {
            event.getRecipients().removeIf(recipient -> PartyChat.getDataHandler().getUser(recipient).isPartyOnly());
            if (PartyChat.getDataHandler().getConfigBoolean(PartyChat.getDataHandler().mainConfig, "format-chat"))
                event.setMessage(Chat.applyColorCodes(event.getMessage()));
        }
    }
}
