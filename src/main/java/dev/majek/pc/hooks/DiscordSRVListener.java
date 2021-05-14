package dev.majek.pc.hooks;

import dev.majek.pc.PartyChat;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreBroadcastEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DiscordSRVListener {

    @Subscribe
    public void discordMessageToMineCraft(DiscordGuildMessagePreBroadcastEvent event) {
        List<CommandSender> recipients = StreamSupport.stream(event.getRecipients().spliterator(), false).collect(Collectors.toList());
        recipients.removeIf(sender -> (sender instanceof Player) && PartyChat.getDataHandler().getUser((Player) sender).isPartyOnly());
        event.setRecipients(recipients);
    }

}
