package Me.Majekdor.PartyChat;

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
    Main plugin;
    public PartyListeners(Main instance){
        plugin = instance;
    }

    FileConfiguration m = MessageDataWrapper.MessageConfig.getConfig();

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        String prefix = m.getString("party-prefix");
        Player player = event.getPlayer();
        if (PartyCommands.inParty.contains(player.getName())) {
            String removePlayer = player.getName();
            String partyName = PartyCommands.players.get(player.getName());
            List<String> partymembers = PartyCommands.party.get(partyName);
            partymembers.remove(removePlayer); PartyCommands.inParty.remove(removePlayer); PartyCommands.players.remove(removePlayer, partyName);
            for (String s : partymembers) {
                Player p = Bukkit.getPlayerExact(s);
                if (!(p == null))
                    p.sendMessage(Main.format((m.getString("player-leave")).replace("%player%", player.getDisplayName()).replace("%prefix%", prefix)));
            }
            if (partymembers.isEmpty()) {
                PartyCommands.parties.remove(partyName);
            }
            if (SpyCommand.serverStaff.contains(player.getName())) {
                SpyCommand.serverStaff.remove(player.getName());
            }
            //Give the party a new random leader if the party creator leaves
            Player lead = PartyCommands.isLeader.get(partyName);
            String leader1 = lead.getName();
            if (leader1 == player.getName()) {
                PartyCommands.isLeader.remove(partyName, player);
                if (!(partymembers.isEmpty())) {
                    Random random = new Random();
                    String newLeader = partymembers.get(random.nextInt(partymembers.size()));
                    Player leader = (Player) Bukkit.getPlayer(newLeader);
                    PartyCommands.isLeader.put(partyName, leader);
                    leader.sendMessage(Main.format((m.getString("you-leader")).replace("%prefix%", prefix)));
                    for (String s : partymembers) {
                        Player p = Bukkit.getPlayerExact(s);
                        if ((!(p == null)) && (!(s.equals(newLeader))))
                            p.sendMessage(Main.format((m.getString("new-leader")).replace("%player%", leader.getDisplayName()).replace("%prefix%", prefix)));
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getDisplayName() == null)  {
            player.setDisplayName(player.getName());
        }
        PartyCommands.partyChat.put(player.getName(), false);
        if (player.hasPermission("partychat.spy") && (!(SpyCommand.serverStaff.contains(player.getName())))) {
            Boolean autoSpy = m.getBoolean("auto-spy");
            SpyCommand.spyToggle.put(player.getName(), autoSpy);
            SpyCommand.serverStaff.add(player.getName());
        }
    }
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (PartyCommands.inParty.contains(player.getName())) {
            if (PartyCommands.partyChat.get(player.getName())) {
                String message = event.getMessage();
                event.setCancelled(true);
                String partyName = PartyCommands.players.get(player.getName());
                List<String> partymembers = PartyCommands.party.get(partyName);
                List<String> messageReceived = new ArrayList<String>();
                for (String s : partymembers) {
                    Player p = Bukkit.getPlayerExact(s);
                    if (!(p == null))
                        messageReceived.add(p.getName());
                    p.sendMessage(Main.format((m.getString("message-format") + message).replace("%partyName%", partyName).replace("%player%", player.getDisplayName())));
                }
                // Send spy message to staff
                for (String s : SpyCommand.serverStaff) {
                    Player p = Bukkit.getPlayerExact(s);
                    if ((!(p == null)) && (!(messageReceived.contains(p.getName()))))
                        if (SpyCommand.spyToggle.get(p.getName())) {
                            p.sendMessage(Main.format((m.getString("spy-format") + message).replace("%partyName%", partyName).replace("%player%", player.getDisplayName())));
                        }
                }
            }
        } else {
            event.setMessage(Main.format(event.getMessage()));
        }
    }
}

