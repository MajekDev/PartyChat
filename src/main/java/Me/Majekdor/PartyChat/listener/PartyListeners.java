package Me.Majekdor.PartyChat.listener;

import Me.Majekdor.PartyChat.PartyChat;
import Me.Majekdor.PartyChat.command.CommandParty;
import Me.Majekdor.PartyChat.command.CommandPartyChat;
import Me.Majekdor.PartyChat.command.CommandSpy;
import Me.Majekdor.PartyChat.util.Chat;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PartyListeners implements Listener {

    FileConfiguration m = PartyChat.messageData.getConfig();
    FileConfiguration c = PartyChat.instance.getConfig();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        CommandPartyChat.partyChat.put(p.getName(), false);
        if (p.hasPermission("partychat.admin") && (!(CommandSpy.serverStaff.contains(p.getName())))) {
            CommandSpy.spyToggle.put(p.getName(), true);
            CommandSpy.serverStaff.add(p.getName());
        } else {
            CommandSpy.spyToggle.put(p.getName(), false);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if (CommandParty.inParty.containsKey(p.getName())) {
            String partyName = CommandParty.inParty.get(p.getName());
            List<Player> partymembers = CommandParty.partyMembers.get(partyName);
            partymembers.remove(p); CommandParty.inParty.remove(p.getName());
            for (Player pl : partymembers) {
                if (!(pl == null))
                    pl.sendMessage(Chat.format((m.getString("player-leave"))
                            .replace("%player%", p.getDisplayName())));
            }
            if (partymembers.isEmpty()) {
                CommandParty.parties.remove(partyName);
            }
            CommandSpy.serverStaff.remove(p.getName());
            //Give the party a new random leader if the party creator leaves
            Player lead = CommandParty.isLeader.get(partyName);
            String leader1 = lead.getName();
            if (leader1.equals(p.getName())) {
                CommandParty.isLeader.remove(partyName, p);
                if (!(partymembers.isEmpty())) {
                    Random random = new Random();
                    Player newLeader = partymembers.get(random.nextInt(partymembers.size()));
                    CommandParty.isLeader.put(partyName, newLeader);
                    newLeader.sendMessage(Chat.format(m.getString("you-leader")));
                    for (Player pl : partymembers) {
                        if ((!(pl== null)) && (!(pl.equals(newLeader))))
                            pl.sendMessage(Chat.format((m.getString("new-leader"))
                                    .replace("%player%", newLeader.getDisplayName())));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (CommandParty.inParty.containsKey(p.getName())) {
            if (CommandPartyChat.partyChat.get(p.getName())) {
                String message = e.getMessage(); e.setCancelled(true);
                String partyName = CommandParty.inParty.get(p.getName());
                List<Player> partymembers = CommandParty.partyMembers.get(partyName);
                List<String> messageReceived = new ArrayList<>();
                // Log all partychat messages to console
                Bukkit.getConsoleSender().sendMessage(Chat.format(ChatColor.RED + "[PCSPY] [" + partyName
                        + ChatColor.RED + "] " + p.getName() + ": " + message));
                for (Player pl : partymembers) {
                    if (!(pl == null)) {
                        messageReceived.add(pl.getName());
                        pl.sendMessage(Chat.format((m.getString("message-format") + message)
                                .replace("%partyName%", partyName).replace("%player%", p.getDisplayName())));
                    }
                }
                // Send spy message to staff
                for (String s : CommandSpy.serverStaff) {
                    Player staff = Bukkit.getPlayerExact(s);
                    assert staff != null;
                    if (!(messageReceived.contains(staff.getName()))) {
                        if (CommandSpy.spyToggle.get(staff.getName())) {
                            staff.sendMessage(Chat.format((m.getString("spy-format") + message)
                                    .replace("%partyName%", Chat.removeColorCodes(partyName))
                                    .replace("%player%", Chat.removeColorCodes(p.getDisplayName()))));
                        }
                    }
                }
            }
        } else {
            e.setMessage(Chat.format(e.getMessage()));
        }
    }
}
