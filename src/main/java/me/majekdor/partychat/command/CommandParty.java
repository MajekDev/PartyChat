package me.majekdor.partychat.command;

import me.majekdor.partychat.PartyChat;
import me.majekdor.partychat.data.Party;
import me.majekdor.partychat.data.Restrictions;
import me.majekdor.partychat.gui.GuiInParty;
import me.majekdor.partychat.gui.GuiNoParty;
import me.majekdor.partychat.util.Bar;
import me.majekdor.partychat.util.Chat;
import me.majekdor.partychat.util.TabCompleterBase;
import me.majekdor.partychat.command.party.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CommandParty implements CommandExecutor, TabCompleter {

    public static FileConfiguration c = PartyChat.getInstance().getConfig();
    public static FileConfiguration m = PartyChat.messageData.getConfig();
    public static Bar bar;

    /**
     * Handle all /party commands - send to different classes for better organization
     *
     * @param sender the player sending the command
     * @param command the command itself
     * @param label command alias(es)
     * @param args command arguments
     * @return true if passed false if failed
     */

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("party")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This is not a console command."); return true;
            }
            Player player = (Player) sender;

            // Check if the admins wants to use permissions
            if (c.getBoolean("use-permissions"))
                if (!player.hasPermission("partychat-use")) {
                    sendMessageWithPrefix(player, m.getString("no-permission")); return true;
                }

            // This is for better organization - each sub command gets it's own class
            if (args.length > 0) {
                switch (args[0]) {
                    case "help":
                        PartyHelp.execute(player, args); break;
                    case "create":
                        PartyCreate.execute(player, args); break;
                    case "info":
                        PartyInfo.execute(player); break;
                    case "add":
                        PartyAdd.execute(player, args); break;
                    case "accept":
                        PartyAccept.execute(player); break;
                    case "deny":
                        PartyDeny.execute(player); break;
                    case "join":
                        PartyJoin.execute(player, args); break;
                    case "leave":
                        PartyLeave.execute(player, false); break;
                    case "shareitem":
                        PartyShareItem.execute(player, args); break;
                    case "promote":
                        PartyPromote.execute(player, args); break;
                    case "remove":
                        PartyRemove.execute(player, args); break;
                    case "rename":
                        PartyRename.execute(player, args); break;
                    case "disband":
                        PartyDisband.execute(player); break;
                    case "summon":
                        PartySummon.execute(player); break;
                    case "toggle":
                        PartyToggle.execute(player, args); break;
                    default:
                        sendMessageWithPrefix(player, m.getString("unknown-command"));
                }
            } else { // no args
                if (PartyChat.disableGuis) {
                    for (String partyInfo : m.getStringList("party-info")) {
                        sendMessageWithPrefix(player, partyInfo.replace("%version%",
                                PartyChat.instance.getDescription().getVersion()));
                    }
                    return true;
                }
                if (Party.inParty(player)) {
                    new GuiInParty().openGui(player);
                } else {
                    new GuiNoParty().openGui(player);
                }
            }
            return true;
        }
        return false;
    }

    // Tab complete method for all party sub commands
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) throws IllegalArgumentException {
        List<String> arguments = new ArrayList<>();
        arguments.add("create"); arguments.add("accept"); arguments.add("deny"); arguments.add("info");
        arguments.add("promote"); arguments.add("add"); arguments.add("remove"); arguments.add("leave");
        arguments.add("disband"); arguments.add("help"); arguments.add("join"); arguments.add("toggle");
        arguments.add("rename"); arguments.add("summon"); arguments.add("shareitem");

        if (args.length == 1) {
            return TabCompleterBase.filterStartingWith(args[0], arguments);
        } else {
            switch (args[0]) {
                case "help":
                    return TabCompleterBase.filterStartingWith(args[1], Arrays.asList("1", "2"));
                case "toggle":
                    return TabCompleterBase.filterStartingWith(args[1], Arrays.asList("private", "public"));
                case "add":
                    return TabCompleterBase.getOnlinePlayers(args[1]).stream().filter(player -> player != null &&
                            !Restrictions.isVanished(Bukkit.getPlayerExact(player))).collect(Collectors.toList());
                case "promote":
                case "remove'":
                    List<String> members = new ArrayList<>();
                    for (UUID memberUUID : Party.getParty((Player) sender).members) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberUUID);
                        members.add(offlinePlayer.getName());
                    }
                    return TabCompleterBase.filterStartingWith(args[1], members);
                case "join":
                    return TabCompleterBase.filterStartingWith(args[1], Party.partyMap.values().stream().distinct()
                            .filter(party -> party.isPublic).map(Party::getRawName));
                default:
                    return Collections.emptyList();
            }
        }
    }

    /**
     * Easier way to send messages to players and add the PartyChat prefix
     *
     * @param player the player to send the message to
     * @param message message to be sent that needs formatting
     */
    public static void sendMessageWithPrefix(Player player, String message) {
        String prefix = m.getString("prefix");
        if (prefix == null)
            Bukkit.getConsoleSender().sendMessage("Error in the messages.yml file. You deleted the prefix.");
        assert prefix != null;
        message = message.replace("%prefix%", prefix);
        player.sendMessage(Chat.colorize(message));
    }

    /**
     * Used internally - this should never be in use when an update is pushed
     * @param string debug message
     */
    public static void debug(String string) {
        PartyChat.getInstance().getLogger().log(Level.SEVERE, "INTERNAL DEBUG: " + string);
    }
}
