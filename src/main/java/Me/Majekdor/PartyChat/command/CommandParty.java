package Me.Majekdor.PartyChat.command;

import Me.Majekdor.PartyChat.PartyChat;
import Me.Majekdor.PartyChat.util.Chat;
import Me.Majekdor.PartyChat.util.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandParty implements CommandExecutor {

    // Hashmaps and lists
    public static Map<String, String> inParty = new HashMap<>();
    public static List<String> privateParties = new ArrayList<>();
    public static List<String> parties = new ArrayList<>();
    public static List<String> partiesRaw = new ArrayList<>();
    public static Map<String, String> partyMatch = new HashMap<>();
    public static Map<String, List<Player>> partyMembers = new HashMap<>();
    public static Map<String, Player> isLeader = new HashMap<>();
    public static Map<String, String> pendingInv = new HashMap<>();
    public static Map<String, Player> pendingJoin = new HashMap<>();
    public static Map<Player, String> joinDelay = new HashMap<>();


    FileConfiguration m = PartyChat.messageData.getConfig(); FileConfiguration c = PartyChat.instance.getConfig();
    String goofyPrefix = m.getString("other-format-prefix");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(Chat.colorize("&cConsole cannot use this command!"));
            return true;
        }

        Player p = (Player) sender;
        PluginDescriptionFile pdf = PartyChat.instance.getDescription();
        if (c.getBoolean("use-permissions")) {
            if (!p.hasPermission("partychat.use")) {
                p.sendMessage(Chat.format(m.getString("no-permission"))); return true;
            }
        }

        if (cmd.getName().equalsIgnoreCase("party") && args.length == 0) {
            for (String partyInfo : m.getStringList("party-info")) {
                p.sendMessage(Chat.format((partyInfo).replace("%version%", pdf.getVersion())));
            }
            return true;


            // Player wants to interact via commands
        } else if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help")) {
                if ((args.length == 1) || args[1].equalsIgnoreCase("1")) {
                    for (String partyHelp1 : m.getStringList("party-help1")) {
                        p.sendMessage(Chat.format(partyHelp1));
                    }
                } else if (args[1].equalsIgnoreCase("2")) {
                    for (String partyHelp2 : m.getStringList("party-help2")) {
                        p.sendMessage(Chat.format(partyHelp2));
                    }
                }
                return true;

            } else if (args[0].equalsIgnoreCase("create")) {
                String partyName;
                if (inParty.containsKey(p.getName())) {
                    p.sendMessage(Chat.format(m.getString("in-party")));
                    return true;
                }
                if (args.length == 1) {
                    p.sendMessage(Chat.format(m.getString("no-name")));
                    return true;
                }
                partyName = args[1];
                boolean stop = nameCheck(partyName, p);
                if (!stop)
                    return true;
                for (String partyCreated : m.getStringList("party-created")) {
                    p.sendMessage(Chat.format((partyCreated).replace("%partyName%", partyName)));
                }

                List<Player> members = new ArrayList<>();
                members.add(p); parties.add(partyName); partiesRaw.add(Chat.removeColorCodes(partyName));
                partyMatch.put(Chat.removeColorCodes(partyName), partyName);
                inParty.put(p.getName(), partyName);
                partyMembers.put(partyName, members);
                isLeader.put(partyName, p); return true;

            } else if (args[0].equalsIgnoreCase("info")) {
                String partyName = inParty.get(p.getName());
                if (!(inParty.containsKey(p.getName()))) {
                    p.sendMessage(Chat.format(m.getString("not-in-party"))); return true;
                }
                List<Player> partymembers = partyMembers.get(partyName);
                Player lead = isLeader.get(partyName);
                if (partymembers.size() == 1) {
                    p.sendMessage(Chat.format((m.getString("info-leader") + lead.getDisplayName())
                            .replace("%partyName%", partyName))); return true;
                }
                StringBuilder pmembers = new StringBuilder();
                for (Player pl : partymembers) {
                    if (!(pl == lead)) {
                        pmembers.append(pl.getDisplayName()).append(", ");
                    }
                }
                String trimmed = pmembers.toString().trim();
                String toSend = trimmed.substring(0, trimmed.length()-1);
                p.sendMessage(Chat.format((m.getString("info-members") + toSend)
                        .replace("%player%", lead.getDisplayName())
                        .replace("%partyName%", partyName)));

            } else if (args[0].equalsIgnoreCase("add")) {
                String partyName = inParty.get(p.getName());
                if (!(inParty.containsKey(p.getName()))) { p.sendMessage(Chat.format(m.getString("not-in-party"))); return true; }
                if  (args.length == 1) { p.sendMessage(Chat.format(m.getString("specify-player"))); return true; }
                Player t = Bukkit.getPlayer(args[1]);
                if (t == null) { p.sendMessage(Chat.format(m.getString("not-online"))); return true; }
                if (p == t) {
                    p.sendMessage(Chat.format(m.getString("add-self"))); return true;
                }
                for (String inv : m.getStringList("invite-message")) {
                    TextUtils.sendFormatted(t, (inv).replace("%partyName%", Chat.removeColorCodes(partyName))
                            .replace("%prefix%", goofyPrefix)
                            .replace("%player%", Chat.removeColorCodes(p.getDisplayName())));
                }
                p.sendMessage(Chat.format((m.getString("invite-sent")).replace("%player%", t.getDisplayName())));
                pendingInv.put(t.getName(), partyName);

                // Invite expires after the time defined in the config file
                int delay = c.getInt("expire-time") * 20;
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () -> {
                    if (pendingInv.containsKey(t.getName())) {
                        pendingInv.remove(t.getName());
                        t.sendMessage(Chat.format(m.getString("expired-invite")));
                    }
                }, delay);

            } else if (args[0].equalsIgnoreCase("accept")) {
                String partyName;
                List<Player> partymembers;
                if (pendingJoin.containsKey(p.getName())) {
                    partyName = inParty.get(p.getName());
                    partymembers = partyMembers.get(partyName);
                    Player join = pendingJoin.get(p.getName());
                    if (!(join.isOnline())) {
                        p.sendMessage(Chat.format(m.getString("not-online"))); return true;
                    }
                    pendingJoin.remove(p.getName());
                    for (Player pl : partymembers) {
                        if (!(pl == null))
                            pl.sendMessage(Chat.format((m.getString("player-join"))
                                    .replace("%player%", join.getDisplayName())));
                    }
                    partymembers.add(join); inParty.put(join.getName(), partyName);
                    partyMembers.replace(partyName, partymembers);
                    join.sendMessage(Chat.format((m.getString("you-join")).replace("%partyName%", partyName)));
                    return true;
                }
                if (inParty.containsKey(p.getName())) { p.sendMessage(Chat.format(m.getString("in-party"))); return true; }
                if (!(pendingInv.containsKey(p.getName()))) { p.sendMessage(Chat.format(m.getString("no-invites"))); return true; }
                partyName = pendingInv.get(p.getName());
                partymembers = partyMembers.get(partyName);
                for (Player pl : partymembers) {
                    if (!(pl == null))
                        pl.sendMessage(Chat.format((m.getString("player-join"))
                                .replace("%player%", p.getDisplayName())));
                }
                partymembers.add(p); inParty.put(p.getName(), partyName);
                partyMembers.replace(partyName, partymembers); pendingInv.remove(p.getName());
                p.sendMessage(Chat.format((m.getString("you-join")).replace("%partyName%", partyName)));

            } else if (args[0].equalsIgnoreCase("deny")) {
                if  (pendingJoin.containsKey(p.getName())) {
                    Player join = pendingJoin.get(p.getName());
                    if (!(join.isOnline())) {
                        p.sendMessage(Chat.format(m.getString("not-online"))); return true;
                    }
                    pendingJoin.remove(p.getName());
                    join.sendMessage(Chat.format(m.getString("join-denied")));
                    p.sendMessage(Chat.format(m.getString("deny-join"))); return true;
                }
                if (inParty.containsKey(p.getName())) { p.sendMessage(Chat.format(m.getString("in-party"))); return true; }
                if (!(pendingInv.containsKey(p.getName()))) { p.sendMessage(Chat.format(m.getString("no-invites"))); return true; }
                String partyName = pendingInv.get(p.getName());
                Player lead = isLeader.get(partyName);
                p.sendMessage(Chat.format(m.getString("you-decline")));
                lead.sendMessage(Chat.format((m.getString("decline-join"))
                        .replace("%player%", p.getDisplayName())));
                pendingInv.remove(p.getName());

            } else if (args[0].equalsIgnoreCase("leave")) {
                if (!(inParty.containsKey(p.getName()))) {
                    p.sendMessage(Chat.format(m.getString("not-in-party")));
                    return true;
                }
                String partyName = inParty.get(p.getName());
                List<Player> partymembers = partyMembers.get(partyName);
                partymembers.remove(p);
                inParty.remove(p.getName());
                p.sendMessage(Chat.format((m.getString("you-leave")).replace("%partyName%", partyName)));
                for (Player pl : partymembers) {
                    if (!(pl == null))
                        p.sendMessage(Chat.format((m.getString("player-leave"))
                                .replace("%player%", p.getDisplayName())));
                }
                // If the party leader is the one leaving
                if (isLeader.get(partyName).equals(p)) {
                    isLeader.remove(partyName, p);
                    if (partymembers.isEmpty()) {
                        parties.remove(partyName); partiesRaw.remove(Chat.removeColorCodes(partyName));
                        partyMatch.remove(partyName);
                        return true;
                    }
                    Random random = new Random();
                    Player newLeader = partymembers.get(random.nextInt(partymembers.size()));
                    isLeader.put(partyName, newLeader);
                    assert newLeader != null;
                    newLeader.sendMessage(Chat.format((m.getString("you-leader"))));
                    for (Player pl : partymembers) {
                        if ((!(pl == null)) && (!(pl == newLeader)))
                            pl.sendMessage(Chat.format((m.getString("new-leader"))
                                    .replace("%player%", newLeader.getDisplayName())));
                    }
                }
            } else if (args[0].equalsIgnoreCase("join")) {
                if (inParty.containsKey(p.getName())) { p.sendMessage(Chat.format(m.getString("in-party"))); return true; }
                if (args.length == 1) { p.sendMessage(Chat.format(m.getString("specify-party"))); return true; }
                if (!partiesRaw.contains(args[1])) { p.sendMessage(Chat.format(m.getString("unknown-party"))); return true; }
                String partyName = partyMatch.get(args[1]); Player leader = isLeader.get(partyName);
                if (privateParties.contains(partyName)) {
                    p.sendMessage(Chat.format(m.getString("party-private"))); return true;
                }
                if (joinDelay.containsKey(p)) {
                    String blockedParty = joinDelay.get(p);
                    if (blockedParty.equalsIgnoreCase(args[1])) {
                        p.sendMessage(Chat.format(m.getString("join-wait"))); return true;
                    }
                }
                for (String inv : m.getStringList("request-join")) {
                    TextUtils.sendFormatted(leader, (inv).replace("%prefix%", goofyPrefix)
                            .replace("%player%", Chat.removeColorCodes(p.getDisplayName())));
                }
                p.sendMessage(Chat.format(m.getString("request-sent")));
                pendingJoin.put(leader.getName(), p);
                joinDelay.put(p, partyName);
                int delay = c.getInt("block-time") * 20;
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () -> {
                    if (pendingJoin.containsKey(leader.getName())) {
                        pendingJoin.remove(leader.getName());
                        p.sendMessage(Chat.format(m.getString("expired-join")));
                    }
                }, delay);
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () -> {
                    joinDelay.remove(p);
                }, delay);


                // Party leader commands
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (!(inParty.containsKey(p.getName()))) { p.sendMessage(Chat.format(m.getString("not-in-party"))); return true; }
                String partyName = inParty.get(p.getName()); Player lead = isLeader.get(partyName);
                if (!(lead.getName().equals(p.getName()))) { p.sendMessage(Chat.format(m.getString("not-leader"))); return true; }
                if (args.length == 1) { p.sendMessage(Chat.format(m.getString("specify-player"))); return true; }
                List<Player> partymembers = partyMembers.get(partyName);
                if (!(partymembers.contains(Bukkit.getPlayerExact(args[1])))) { p.sendMessage(Chat.format(m.getString("player-not-in-party"))); return true; }
                Player target = Bukkit.getPlayer(args[1]); assert target != null;
                if (target == p) {
                    p.sendMessage(Chat.format(m.getString("remove-self"))); return true;
                }
                target.sendMessage(Chat.format((m.getString("you-removed"))
                        .replace("%player%", p.getDisplayName())));
                partymembers.remove(target); inParty.remove(target.getName());
                for (Player pl : partymembers) {
                    if (!(pl == null))
                        p.sendMessage(Chat.format((m.getString("player-removed"))
                                .replace("%player%", target.getDisplayName())));
                }
                if (partymembers.isEmpty()) {
                    parties.remove(partyName); partiesRaw.remove(Chat.removeColorCodes(partyName));
                    partyMatch.remove(partyName);
                }

            } else if (args[0].equalsIgnoreCase("disband")) {
                if (!(inParty.containsKey(p.getName()))) { p.sendMessage(Chat.format(m.getString("not-in-party"))); return true; }
                String partyName = inParty.get(p.getName()); Player lead = isLeader.get(partyName);
                if (!(lead.getName().equals(p.getName()))) { p.sendMessage(Chat.format(m.getString("not-leader"))); return true; }
                List<Player> partymembers = partyMembers.get(partyName);
                for (Player pl: partymembers) {
                    if (!(pl == null) && (!(pl == lead))) {
                        pl.sendMessage(Chat.format((m.getString("party-disbanded"))
                                .replace("%partyName%", partyName)));
                        inParty.remove(pl.getName()); partyMembers.remove(partyName);
                    }
                }
                parties.remove(partyName); partiesRaw.remove(Chat.removeColorCodes(partyName));
                partyMatch.remove(partyName); isLeader.remove(partyName, p);
                inParty.remove(p.getName()); partyMembers.remove(partyName);
                p.sendMessage(Chat.format((m.getString("party-disbanded")).replace("%partyName%", partyName)));

            } else if (args[0].equalsIgnoreCase("promote")) {
                if (!(inParty.containsKey(p.getName()))) { p.sendMessage(Chat.format(m.getString("not-in-party"))); return true; }
                String partyName = inParty.get(p.getName()); Player lead = isLeader.get(partyName);
                if (!(lead.getName().equals(p.getName()))) { p.sendMessage(Chat.format(m.getString("not-leader"))); return true; }
                if (args.length == 1) { p.sendMessage(Chat.format(m.getString("specify-player"))); return true; }
                List<Player> partymembers = partyMembers.get(partyName);
                if (!(partymembers.contains(Bukkit.getPlayerExact(args[1])))) { p.sendMessage(Chat.format(m.getString("player-not-in-party"))); return true; }
                Player target = Bukkit.getPlayer(args[1]); assert target != null;
                if (args[1].equalsIgnoreCase(p.getName())) { p.sendMessage(Chat.format(m.getString("promote-self"))); return true; }
                isLeader.remove(partyName, p); isLeader.put(partyName, target);
                target.sendMessage(Chat.format((m.getString("you-promoted"))
                        .replace("%player%", p.getDisplayName())));
                for (Player pl : partymembers) {
                    if ((!(pl == null)) && (!(pl == target)))
                        p.sendMessage(Chat.format((m.getString("new-leader"))
                                .replace("%player%", target.getDisplayName())));
                }
            } else if (args[0].equalsIgnoreCase("toggle")) {
                if (!(inParty.containsKey(p.getName()))) { p.sendMessage(Chat.format(m.getString("not-in-party"))); return true; }
                String partyName = inParty.get(p.getName()); Player lead = isLeader.get(partyName);
                if (!(lead.getName().equals(p.getName()))) { p.sendMessage(Chat.format(m.getString("not-leader"))); return true; }
                if (args.length == 1) { p.sendMessage(Chat.format(m.getString("choose-toggle"))); return true; }
                if (args[1].equalsIgnoreCase("public")) {
                    privateParties.remove(partyName);
                    p.sendMessage(Chat.format(m.getString("toggle-public"))); return true;
                }
                if (args[1].equalsIgnoreCase("private")) {
                    privateParties.add(partyName);
                    p.sendMessage(Chat.format(m.getString("toggle-private"))); return true;
                }
                p.sendMessage(Chat.format(m.getString("unknown-command")));

            } else { p.sendMessage(Chat.format(m.getString("unknown-command"))); }
        }
        return false;
    }

    /**
     * Check whether or not the party name is available
     *
     * @param partyName the player's requested name
     * @param p the player creating the party
     * @return true if passed and false if failed
     */
    public boolean nameCheck(String partyName, Player p) {
        int max = c.getInt("max-characters");
        String noColor = Chat.removeColorCodes(partyName);
        if (noColor.length() > max) {
            p.sendMessage(Chat.format(m.getString("less-20")));
            return false;
        }
        if (CommandParty.parties.contains(partyName) || CommandParty.partiesRaw.contains(Chat.removeColorCodes(partyName))) {
            p.sendMessage(Chat.format(m.getString("name-taken")));
            return false;
        }
        //Check to see if the server wants to block inappropriate party names
        if (c.getBoolean("block-inappropriate-names")) {
            for (String i : c.getStringList("blocked-names.wordlist")) {
                if (partyName.equalsIgnoreCase(i)) {
                    p.sendMessage(Chat.format(m.getString("inappropriate-name")));
                    return false;
                }
            }
        }
        return true;
    }
}
