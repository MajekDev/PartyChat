package Me.Majekdor.PartyChat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.*;

public class PartyCommands implements CommandExecutor {

    Main plugin;
    public PartyCommands(Main instance){
        plugin = instance;
    }

    //Generating hashmaps and lists that we'll need
    public static List<String> inParty = new ArrayList<String>();
    public static List<String> parties = new ArrayList<String>();
    public static Map<String, String> players = new HashMap<String, String>();
    public static Map<String, List<String>> party = new HashMap<String, List<String>>();
    public static Map<String, Boolean> partyChat = new HashMap<String, Boolean>();
    public static Map<String, Player> isLeader = new HashMap<String, Player>();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.format("&cSorry console, you can't join the party"));
            return true;
        }
        FileConfiguration m = MessageDataWrapper.MessageConfig.getConfig();

        PluginDescriptionFile pdf = plugin.getDescription();
        Player player = (Player) sender;
        //Get preferences from config file
        String prefix = m.getString("party-prefix");

        //Check if the server wants to implement permissions
        if (plugin.getConfig().getBoolean("use-permissions")) {
            if (!(player.hasPermission("partychat.use"))) {
                player.sendMessage(Main.format((m.getString("no-permission")).replace("%prefix%", prefix))); return true;
            }
        }

        //Command for toggling party chat to talk to your party or just talking to your party
        if (cmd.getName().equalsIgnoreCase("partychat")) {
            if (!(inParty.contains(player.getName()))) {
                player.sendMessage(Main.format((m.getString("not-in-party")).replace("%prefix%", prefix))); return true;
            }
            if (args.length == 0) {
                if (!(partyChat.get(player.getName()))) {
                    partyChat.replace(player.getName(), true);
                    player.sendMessage(Main.format((m.getString("pc-enabled")).replace("%prefix%", prefix)));
                } else {
                    partyChat.replace(player.getName(), false);
                    player.sendMessage(Main.format((m.getString("pc-disabled")).replace("%prefix%", prefix)));
                }
            }
            if (args.length >= 1) {
                // Send message to party chat if they type /pc <message>
                StringBuilder message = new StringBuilder();
                for (String arg : args) {
                    message.append(" ").append(arg);
                }
                String withoutSpace = message.substring(1);
                String partyName = players.get(player.getName());
                List<String> partymembers = party.get(partyName);
                List<String> messageReceived = new ArrayList<String>();
                for (String s : partymembers) {
                    Player p = Bukkit.getPlayerExact(s);
                    if (!(p == null))
                        messageReceived.add(p.getName());
                    p.sendMessage(Main.format((m.getString("message-format") + withoutSpace).replace("%partyName%", partyName).replace("%player%", player.getDisplayName()).replace("%prefix%", prefix)));
                }
                // Send spy message to staff
                for (String s : SpyCommand.serverStaff) {
                    Player p = Bukkit.getPlayerExact(s);
                    if ((!(p == null)) && (!(messageReceived.contains(p.getName()))))
                        if (SpyCommand.spyToggle.get(p.getName())) {
                            p.sendMessage(Main.format((m.getString("spy-format") + withoutSpace).replace("%partyName%", partyName).replace("%player%", player.getDisplayName()).replace("%prefix%", prefix)));
                        }
                }
            }
        }

        //Party commands for managing the party
        if (cmd.getName().equalsIgnoreCase("party")) {
            if (args.length == 0) {
                for (String partyInfo : m.getStringList("party-info")) {
                    player.sendMessage(Main.format((partyInfo).replace("%prefix%", prefix).replace("%version%", pdf.getVersion())));
                }

                //NEW PARTY HELP
            } else if (args[0].equalsIgnoreCase("help")) {
                if ((args.length == 1) || args[1].equalsIgnoreCase("1")) {
                    for (String partyHelp1 : m.getStringList("party-help1")) {
                        player.sendMessage(Main.format((partyHelp1).replace("%prefix%", prefix)));
                    }
                } else if (args[1].equalsIgnoreCase("2")) {
                    for (String partyHelp2 : m.getStringList("party-help2")) {
                        player.sendMessage(Main.format((partyHelp2).replace("%prefix%", prefix)));
                    }
                } else {
                    player.sendMessage(Main.format((m.getString("unknown-command")).replace("%prefix%", prefix)));
                }

                //Create Command
            } else if (args[0].equalsIgnoreCase("create")) {
                if (inParty.contains(player.getName())) {
                    player.sendMessage(Main.format((m.getString("in-party")).replace("%prefix%", prefix))); return true;
                }
                if (args.length == 1) {
                    player.sendMessage(Main.format((m.getString("no-name")).replace("%prefix%", prefix))); return true;
                } else if (args.length == 2) {
                    String partyName = args[1];
                    if (args[1].length() >= 20) {
                        player.sendMessage(Main.format((m.getString("less-20")).replace("%prefix%", prefix))); return true;
                    }
                    if (parties.contains(args[1])) {
                        player.sendMessage(Main.format((m.getString("name-taken")).replace("%prefix%", prefix))); return true;
                    }
                    //Check to see if the server wants to block inappropriate party names
                    if (plugin.getConfig().getBoolean("block-inappropriate-names")) {
                        for (String i : plugin.getConfig().getStringList("blocked-names.wordlist")) {
                            if (args[1].equalsIgnoreCase(i)) {
                                player.sendMessage(Main.format((m.getString("inappropriate-name")).replace("%prefix%", prefix))); return true;
                            }
                        }
                    }
                    parties.add(partyName);
                    inParty.add(player.getName());
                    for (String partyCreated : m.getStringList("party-created")) {
                        player.sendMessage(Main.format((partyCreated).replace("%prefix%", prefix).replace("%partyName%", partyName)));
                    }
                    List<String> members = new ArrayList<String>();
                    String leader = player.getName(); isLeader.put(partyName, player);
                    players.put(leader, partyName); members.add(leader); party.put(partyName, members);
                } else if (args.length >= 3) {
                    player.sendMessage(Main.format((m.getString("name-only-one")).replace("%prefix%", prefix)));
                }
                //Info Command
            } else if (args[0].equalsIgnoreCase("info")) {
                if (args.length == 1) {
                    if (!(inParty.contains(player.getName()))) {
                        player.sendMessage(Main.format((m.getString("not-in-party")).replace("%prefix%", prefix))); return true;
                    }
                    String partyName = players.get(player.getName());
                    Player lead = isLeader.get(partyName);
                    String leader = lead.getDisplayName();
                    List<String> partymembers = party.get(partyName);
                    if (partymembers.size() == 1) {
                        player.sendMessage(Main.format((m.getString("info-leader") + leader).replace("%partyName%", partyName).replace("%prefix%", prefix))); return true;
                    }
                    StringBuilder members = new StringBuilder();
                    for (String s : partymembers) {
                        if (!(s.equals(leader))) {
                            members.append(s).append(", ");
                        }
                    }
                    String trimmed = members.toString().trim();
                    String toSend = trimmed.substring(0, trimmed.length()-1);
                    player.sendMessage(Main.format((m.getString("info-members") + toSend).replace("%player%", leader).replace("%partyName%", partyName).replace("%prefix%", prefix)));
                }
                //Add Command
            } else if (args[0].equalsIgnoreCase("add")) {
                if (!(inParty.contains(player.getName()))) {
                    player.sendMessage(Main.format((m.getString("not-in-party")).replace("%prefix%", prefix))); return true;
                }
                if (args.length == 1) {
                    player.sendMessage(Main.format((m.getString("specify-player")).replace("%prefix%", prefix)));
                } else if (args.length == 2) {
                    Player target = Bukkit.getPlayer(args[1]); String partyName = players.get(player.getName());
                    if (target == null) {
                        player.sendMessage(Main.format((m.getString("not-online")).replace("%prefix%", prefix))); return true;
                    }
                    if (inParty.contains(target.getName())) {
                        player.sendMessage(Main.format((m.getString("player-in-party")).replace("%prefix%", prefix))); return true;
                    }
                    String addedPlayer = target.getDisplayName();
                    String targetReal = target.getName();
                    for (String inviteMessage : m.getStringList("invite-message")) {
                        target.sendMessage(Main.format((inviteMessage).replace("%prefix%", prefix).replace("%partyName%", partyName).replace("%player%", player.getDisplayName())));
                    }
                    player.sendMessage(Main.format((m.getString("invite-sent")).replace("%prefix%", prefix).replace("%player%", target.getDisplayName())));
                    players.put(targetReal, partyName);
                }
                //Accept Command
            } else if (args[0].equalsIgnoreCase("accept")) {
                if (inParty.contains(player.getName())) {
                    player.sendMessage(Main.format((m.getString("in-party")).replace("%prefix%", prefix))); return true;
                }
                if (!(players.containsKey(player.getName()))) {
                    player.sendMessage(Main.format((m.getString("no-invites")).replace("%prefix%", prefix))); return true;
                }
                String partyName = players.get(player.getName());
                List<String> partymembers = party.get(partyName);
                for (String s : partymembers) {
                    Player p = Bukkit.getPlayerExact(s);
                    if (!(p == null))
                        p.sendMessage(Main.format((m.getString("player-join")).replace("%prefix%", prefix).replace("%player%", player.getDisplayName())));
                }
                partymembers.add(player.getName()); inParty.add(player.getName()); party.replace(partyName, partymembers);
                player.sendMessage(Main.format((m.getString("you-join")).replace("%prefix%", prefix).replace("%partyName%", partyName)));
                //Deny Command
            } else if (args[0].equalsIgnoreCase("deny")) {
                if (inParty.contains(player.getName())) {
                    player.sendMessage(Main.format((m.getString("in-party")).replace("%prefix%", prefix))); return true;
                }
                if (!(players.containsKey(player.getName()))) {
                    player.sendMessage(Main.format((m.getString("no-invites")).replace("%prefix%", prefix))); return true;
                }
                String partyName = players.get(player.getName());
                players.remove(player.getName(), partyName);
                player.sendMessage(Main.format((m.getString("you-decline")).replace("%prefix%", prefix)));
                Player leader = isLeader.get(partyName);
                leader.sendMessage(Main.format((m.getString("decline-join")).replace("%prefix%", prefix).replace("%player%", player.getDisplayName())));
                //Leave Command
            } else if (args[0].equalsIgnoreCase("leave")) {
                if (!(inParty.contains(player.getName()))) {
                    player.sendMessage(Main.format((m.getString("not-in-party")).replace("%prefix%", prefix))); return true;
                }
                String partyName = players.get(player.getName());
                List<String> partymembers = party.get(partyName);
                partymembers.remove(player.getName()); inParty.remove(player.getName());
                players.remove(player.getName(), partyName);
                player.sendMessage(Main.format((m.getString("you-leave")).replace("%partyName%", partyName).replace("%prefix%", prefix)));
                for (String s : partymembers) {
                    Player p = Bukkit.getPlayerExact(s);
                    if (!(p == null))
                        p.sendMessage(Main.format((m.getString("player-leave")).replace("%player%", player.getDisplayName()).replace("%prefix%", prefix)));
                }
                if (isLeader.get(partyName).equals(player.getName())) {
                    isLeader.remove(partyName, player.getName());
                    if (partymembers.isEmpty()) {
                        parties.remove(partyName); return true;
                    }
                    Random random = new Random();
                    String newLeader = partymembers.get(random.nextInt(partymembers.size()));
                    Player leader = Bukkit.getPlayer(newLeader);
                    PartyCommands.isLeader.put(partyName, leader);
                    leader.sendMessage(Main.format((m.getString("you-leader")).replace("%prefix%", prefix)));
                    for (String s : partymembers) {
                        Player p = Bukkit.getPlayerExact(s);
                        if ((!(p == null)) && (!(s.equals(newLeader))))
                            p.sendMessage(Main.format((m.getString("new-leader")).replace("%player%", leader.getDisplayName()).replace("%prefix%", prefix)));
                    }
                }

                //LEADER ONLY COMMANDS
                //Remove Command
            } else if (args[0].equalsIgnoreCase("remove")) {
                String partyName = players.get(player.getName());
                Player lead = isLeader.get(partyName);
                String leader = lead.getName();
                if (!(inParty.contains(player.getName()))) {
                    player.sendMessage(Main.format((m.getString("not-in-party")).replace("%prefix%", prefix))); return true;
                }
                if (!(leader == player.getName())) {
                    player.sendMessage(Main.format((m.getString("not-leader")).replace("%prefix%", prefix))); return true;
                }
                if (args.length == 1) {
                    player.sendMessage(Main.format((m.getString("specify-player")).replace("%prefix%", prefix))); return true;
                } else if (args.length == 2) {
                    if (!(inParty.contains(args[1]))) {
                        player.sendMessage(Main.format((m.getString("player-not-in-party")).replace("%prefix%", prefix))); return true;
                    }
                    Player target = Bukkit.getPlayer(args[1]);
                    String removePlayer = target.getName();
                    target.sendMessage(Main.format((m.getString("you-removed")).replace("%player%", player.getDisplayName()).replace("%prefix%", prefix)));
                    players.remove(removePlayer, partyName);
                    List<String> partymembers = party.get(partyName);
                    partymembers.remove(target.getName()); players.remove(target, partyName); inParty.remove(removePlayer);
                    for (String s : partymembers) {
                        Player p = Bukkit.getPlayerExact(s);
                        if (!(p == null))
                            p.sendMessage(Main.format((m.getString("player-removed")).replace("%player%", target.getDisplayName()).replace("%prefix%", prefix)));
                    }
                    if (partymembers.isEmpty()) {
                        parties.remove(partyName);
                    }
                }
                //Disband Command
            } else if (args[0].equalsIgnoreCase("disband")) {
                if (!(inParty.contains(player.getName()))) {
                    player.sendMessage(Main.format((m.getString("not-in-party")).replace("%prefix%", prefix))); return true;
                }
                String partyName = players.get(player.getName());
                Player lead = isLeader.get(partyName);
                String leader = lead.getName();
                if (!(leader == player.getName())) {
                    player.sendMessage(Main.format((m.getString("not-leader")).replace("%prefix%", prefix))); return true;
                }
                List<String> partymembers = party.get(partyName);
                for (String s : partymembers) {
                    Player p = Bukkit.getPlayerExact(s);
                    if (!(p == null) && (!(s == leader))) {
                        p.sendMessage(Main.format((m.getString("party-disbanded")).replace("%partyName%", partyName).replace("%prefix%", prefix)));
                        inParty.remove(p.getName());
                        players.remove(p.getName(), partyName);
                    }
                }
                parties.remove(partyName); isLeader.remove(partyName, player);
                inParty.remove(player.getName()); players.remove(player.getName(), partyName);
                player.sendMessage(Main.format((m.getString("party-disbanded")).replace("%partyName%", partyName).replace("%prefix%", prefix)));

                //Promote Command
            } else if (args[0].equalsIgnoreCase("promote")) {
                if (!(inParty.contains(player.getName()))) {
                    player.sendMessage(Main.format((m.getString("not-in-party")).replace("%prefix%", prefix))); return true;
                }
                String partyName = players.get(player.getName());
                Player lead = isLeader.get(partyName);
                String leader = lead.getName();
                if (!(leader == player.getName())) {
                    player.sendMessage(Main.format((m.getString("not-leader")).replace("%prefix%", prefix))); return true;
                }
                if (args.length == 1) {
                    player.sendMessage(Main.format((m.getString("specify-player")).replace("%prefix%", prefix))); return true;
                } else if (args.length == 2) {
                    if (!(inParty.contains(args[1]))) {
                        player.sendMessage(Main.format((m.getString("player-not-in-party")).replace("%prefix%", prefix))); return true;
                    }
                    Player target = Bukkit.getPlayer(args[1]);
                    String newLeader = target.getName(); String oldLeader = player.getDisplayName();
                    if (args[1].equalsIgnoreCase(oldLeader)) {
                        player.sendMessage(Main.format((m.getString("promote-self")).replace("%prefix%", prefix))); return true;
                    }
                    isLeader.remove(partyName, player);
                    isLeader.put(partyName, target);
                    target.sendMessage(Main.format((m.getString("you-promoted")).replace("%player%", oldLeader).replace("%prefix%", prefix)));
                    List<String> partymembers = PartyCommands.party.get(partyName);
                    for (String s : partymembers) {
                        Player p = Bukkit.getPlayerExact(s);
                        if ((!(p == null)) && (!(s == newLeader)))
                            p.sendMessage(Main.format((m.getString("new-leader")).replace("%player%", target.getDisplayName()).replace("%prefix%", prefix)));
                    }
                }

                //Error Messages
            } else {
                player.sendMessage(Main.format((plugin.messageData.getConfig().getString("unknown-command")).replace("%prefix%", prefix)));
            }
        }
        return false;
    }
}
