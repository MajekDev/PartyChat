package dev.majek.partychat.hooks;

import dev.majek.partychat.PartyChat;
import dev.majek.partychat.command.CommandPartyChat;
import dev.majek.partychat.data.Party;
import dev.majek.partychat.util.Chat;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.MessageAction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class DiscordSRVListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (Party.inParty(event.getPlayer()) && CommandPartyChat.partyChat.get(event.getPlayer())
                && PartyChat.getInstance().getConfig().getBoolean("messages-to-discordsrv")
                && PartyChat.hasDiscordSRV) {
            sendMessage(event.getPlayer(), event.getMessage());
        }
    }

    public static void sendMessage(Player player, String message) {
        MessageAction messageAction = DiscordSRV.getPlugin().getMainTextChannel().sendMessage(Chat.removeColorCodes(
                (PartyChat.messageData.getConfig().getString("message-format") + message)
                        .replace("%partyName%", Chat.removeColorCodes(Party.getParty(player).name))
                        .replace("%player%", player.getName())));
        messageAction.queue();
    }

}
