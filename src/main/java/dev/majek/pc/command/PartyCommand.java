package dev.majek.pc.command;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.DataHandler;
import dev.majek.pc.data.object.Cooldown;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.Restrictions;
import dev.majek.pc.data.object.User;
import dev.majek.pc.util.*;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class handles all /party commands and subcommands.
 */
public abstract class PartyCommand implements CommandExecutor, TabCompleter {

    private final String name;
    private final String usage;
    private final String description;
    private final boolean requiresLeader;
    private int cooldown;
    private boolean disabled;
    private final List<String> aliases;

    /**
     * PartyChat's main config file.
     */
    public static FileConfiguration mainConfig = PartyChat.getDataHandler().mainConfig;
    /**
     * Map in a map for cooldown storage.
     */
    public static Map<Player, Map<PartyCommand, Cooldown>> cooldownMap = new HashMap<>();

    protected PartyCommand(String name, String usage, String description, boolean requiresLeader, boolean disabled,
                           int cooldown, List<String> aliases) {
        this.name = name;
        this.usage = usage;
        this.description = description;
        this.requiresLeader = requiresLeader;
        this.cooldown = cooldown;
        this.disabled = disabled;
        this.aliases = new ArrayList<>(aliases);
    }

    public abstract boolean execute(Player player, String[] args, boolean leftServer);

    /**
     * Refresh the main PartyChat config object used in this class and subclasses.
     */
    public static void reload() {
        PartyChat.getCore().reloadConfig();
        mainConfig = PartyChat.getDataHandler().mainConfig;
    }

    /**
     * Handle all of the /party command and subcommands.
     * @param sender The command's sender. In this method we only accept a Player.
     * @param command The command itself.
     * @param label The command's label.
     * @param args The command arguments.
     * @return True/False just to make Bukkit happy.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        String commandString = "/" + command.getName() + " " + String.join(" ", args);
        try {
            if (command.getName().equalsIgnoreCase("party")) {

                PartyChat.getDataHandler().logToFile("Sender " + sender.getName()
                        + " executed command " + commandString, "COMMAND");

                // Don't allow use from console
                if (!(sender instanceof Player)) {
                    sendMessage(sender, "no-console"); return true;
                }
                Player player = (Player) sender;

                // Check if the admins wants to use permissions
                // Ignore this whole section if they have admin perms
                if (PartyChat.getDataHandler().getConfigBoolean(mainConfig, "use-permissions")
                        && !player.hasPermission("partychat.admin"))
                    if (!player.hasPermission("partychat.use")) {
                        sendMessage(player, "no-permission"); return true;
                    }

                if (args.length > 0) {

                    // Get the specified subcommand
                    PartyCommand partyCommand = PartyChat.getCommandHandler().getCommand(args[0].toLowerCase());
                    if (partyCommand == null) {
                        sendMessage(player, "unknown-command");
                        return true;
                    }

                    // Make sure the player cann use the command
                    if (!partyCommand.canUse(player))
                        return true;

                    // Execute command
                    boolean executedFully = partyCommand.execute(player, args, false);

                    // Put the command on cooldown if it completely executed
                    if (executedFully) {
                        Map<PartyCommand, Cooldown> newMap = new HashMap<>();
                        newMap.put(partyCommand, new Cooldown(partyCommand));
                        cooldownMap.put(player, newMap);
                    }
                }
            }
        } catch (Exception ex) {
            StringBuilder error = new StringBuilder();
            error.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append('\n');
            for (StackTraceElement ste : ex.getStackTrace())
                error.append("    at ").append(ste.toString()).append('\n');
            String errorString = error.toString();
            if (sender.hasPermission("partychat.admin"))
                sendMessageWithReplacement(sender, "command-error-staff", "%command%", commandString);
            else
                sendMessageWithReplacement(sender, "command-error", "%command%", commandString);
            PartyChat.error("There was an error executing command " + commandString);
            PartyChat.error(errorString);
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Tab completion for /party command and subcommands.
     * @param sender The command's sender.
     * @param command The command itself.
     * @param alias The command's alias.
     * @param args The command arguments.
     * @return A list of strings to show the sender.
     * @throws IllegalArgumentException Allows for returning {@link Collections#emptyList()}.
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        Player player = (Player) sender;
        User user = PartyChat.getDataHandler().getUser(player);
        if (args.length == 1) {
            // If you're not in a party or you're the party leader all subcommands will be available to tab complete
            // If you're just a party member then leader commands won't be shown
            return (user.isLeader() || player.hasPermission("partychat.bypass")) ? TabCompleterBase.filterStartingWith(args[0],
                    PartyChat.getCommandHandler().getLeaderCommandsAndAliases())
                    : TabCompleterBase.filterStartingWith(args[0], PartyChat
                    .getCommandHandler().getAllCommandsAndAliases());
        } else if (args.length > 3) {
            return Collections.emptyList();
        } else {
            PartyCommand partyCommand = PartyChat.getCommandHandler().getCommand(args[0]);
            if (partyCommand == null)
                return Collections.emptyList();
            switch (partyCommand.getName()) {
                case "accept":
                case "deny":
                    return (user.isLeader() || player.hasPermission("partychat.bypass")) ? TabCompleterBase.filterStartingWith(args[1], PartyChat.getPartyHandler()
                            .getParty(player).getPendingInvitations().stream().map(Pair::getFirst)
                            .map(Player::getName)) : Collections.emptyList();
                case "help":
                    return TabCompleterBase.filterStartingWith(args[1], Arrays.asList("1", "2"));
                case "toggle":
                    if (args.length == 2)
                        return (user.isLeader() || player.hasPermission("partychat.bypass")) ? TabCompleterBase.filterStartingWith(args[1], Arrays
                                .asList("private", "public", "friendly-fire")) : Collections.emptyList();
                    if (args.length == 3)
                        return (user.isLeader() || player.hasPermission("partychat.bypass")) ? TabCompleterBase.filterStartingWith(args[2], Arrays
                                .asList("allow", "deny")) : Collections.emptyList();
                case "add":
                    return TabCompleterBase.getOnlinePlayers(args[1]).stream().filter(person -> person != null &&
                            !Restrictions.isVanished(Bukkit.getPlayerExact(person))).collect(Collectors.toList());
                case "promote":
                case "remove":
                    return (user.isLeader() || player.hasPermission("partychat.bypass")) ? TabCompleterBase.filterStartingWith(args[1], PartyChat.getPartyHandler()
                            .getParty(user).getMembers().stream().map(User::getUsername))
                            : Collections.emptyList();
                case "join":
                    return TabCompleterBase.filterStartingWith(args[1], PartyChat.getPartyHandler().getPartyMap()
                            .values().stream().distinct().filter(Party::isPublic).map(Party::getRawName));
                default:
                    return Collections.emptyList();
            }
        }
    }

    /**
     * Check if a player can use a certain subcommand. This will check to see if the command requires the player
     * be the party leader, if the command is still on cooldown, and if the command is disabled.
     * @param player The player running the subcommand.
     * @return Whether or not the player may use the subcommand.
     */
    public boolean canUse(Player player) {
        User user = PartyChat.getDataHandler().getUser(player);
        if (this.requiresLeader() && !user.isLeader() && !player.hasPermission("partychat.bypass")) {
            if (!user.isInParty())
                sendMessage(player, "not-in-party");
            else
                sendMessage(player, "not-leader");
            return false;
        } else if (cooldownMap.get(player) != null && cooldownMap.get(player).containsKey(this)
                && !cooldownMap.get(player).get(this).isFinished()) {
            sendMessageWithReplacement(player, "cooldown", "%time%", TimeInterval
                    .formatTime(cooldownMap.get(player).get(this)
                            .getTimeRemaining() * 1000L, false));
            return false;
        } else if (this.isDisabled()) {
            sendMessage(player, "command-disabled");
            return false;
        } else
            return true;
    }

    @SuppressWarnings("deprecation")
    public static void sendFormattedMessage(CommandSender sender, String message) {
        if (PartyChat.getDataHandler().messageType == null) {
            try {
                sender.sendMessage(Chat.parseExpression(Chat.applyColorCodes(message)));
                PartyChat.getDataHandler().messageType = DataHandler.MessageType.COMPONENT;
                PartyChat.getDataHandler().logToFile("Set message type to Component", "INFO");
            } catch (NoSuchMethodError error) {
                try {
                    sender.spigot().sendMessage(BungeeComponentSerializer.get().serialize(Chat
                            .parseExpression(Chat.applyColorCodes(message))));
                    PartyChat.getDataHandler().messageType = DataHandler.MessageType.BASECOMPONENT;
                    PartyChat.getDataHandler().logToFile("Set message type to BaseComponent", "INFO");
                } catch (NoSuchMethodError error1) {
                    sender.sendMessage(Chat.applyColorCodes(LegacyComponentSerializer.legacyAmpersand()
                            .serialize(Chat.parseExpression(Chat.applyColorCodes(message)))));
                    PartyChat.getDataHandler().messageType = DataHandler.MessageType.RAW;
                    PartyChat.getDataHandler().logToFile("Set message type to raw", "INFO");
                }
            }
        } else {
            switch (PartyChat.getDataHandler().messageType) {
                case COMPONENT:
                    sender.sendMessage(Chat.parseExpression(Chat.applyColorCodes(message)));
                    break;
                case BASECOMPONENT:
                    sender.spigot().sendMessage(BungeeComponentSerializer.get().serialize(Chat
                            .parseExpression(Chat.applyColorCodes(message))));
                    break;
                case RAW:
                default:
                    sender.sendMessage(Chat.applyColorCodes(LegacyComponentSerializer.legacyAmpersand()
                            .serialize(Chat.parseExpression(Chat.applyColorCodes(message)))));
            }
        }
    }

    /**
     * Send a player a message from the messages config file (this will be set to the correct language).
     * Replace the %prefix% placeholder with the defined prefix in the file.
     * @param sender The player/console to send the message to.
     * @param path The path to get the message from in the file.
     */
    public static void sendMessage(CommandSender sender, String path) {
        String prefix = PartyChat.getDataHandler().getConfigString(PartyChat.getDataHandler().messages, "prefix");
        String message = PartyChat.getDataHandler().getConfigString(PartyChat.getDataHandler().messages, path);
        message = message.replace("%prefix%", prefix);
        sendFormattedMessage(sender, message);
    }

    /**
     * Send a player a message from the messages config file (this will be set to the correct language)
     * while replacing a target with a defined string.
     * @param sender The player/console to send the message to.
     * @param path The path to get the message from in the file.
     * @param target The target string to be replaced.
     * @param replacement The replacement for the target string.
     */
    public static void sendMessageWithReplacement(CommandSender sender, String path, String target, String replacement) {
        String prefix = PartyChat.getDataHandler().getConfigString(PartyChat.getDataHandler().messages, "prefix");
        String message = PartyChat.getDataHandler().getConfigString(PartyChat.getDataHandler().messages, path);
        message = message.replace(target, replacement);
        message = message.replace("%prefix%", prefix);
        sendFormattedMessage(sender, message);
    }

    /**
     * Send a message with replacements and message to send to PartyChat.
     * @param sender Player/console to send the message to.
     * @param path Path to get from messages config file.
     * @param target1 First placeholder to find and replace.
     * @param replacement1 Replacement for first placeholder.
     * @param target2 Second placeholder to find and replace.
     * @param replacement2 Replacement for second placeholder.
     * @param toAdd Message to add to the end of everything.
     */
    public static void sendMessageWithEverything(CommandSender sender, String path, String target1, String replacement1,
                                                String target2, String replacement2, String toAdd) {
        String prefix = PartyChat.getDataHandler().getConfigString(PartyChat.getDataHandler().messages, "prefix");
        String message = PartyChat.getDataHandler().getConfigString(PartyChat.getDataHandler().messages, path);
        message = message.replace(target1, replacement1).replace(target2, replacement2) + toAdd;
        message = message.replace("%prefix%", prefix);
        sendFormattedMessage(sender, message);
    }

    public static void runTaskLater(int delay, Runnable task) {
        try {
            Bukkit.getScheduler().runTaskLaterAsynchronously(PartyChat.getCore(), task, delay * 20L);
        } catch (Exception ex) {
            PartyChat.error("Error running async task. Please consider restarting your server and using /pc bugreport");
        }
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresLeader() {
        return requiresLeader;
    }

    public boolean anyoneCanUse() {
        return !requiresLeader;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isEnabled() {
        return !disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public static boolean getConfigBoolean(String path) {
        return PartyChat.getDataHandler().getConfigBoolean(PartyChat.getDataHandler().mainConfig, path);
    }

    public static String getSubCommandUsage(String command) {
        return PartyChat.getDataHandler().commandConfig.getString("party-subcommands." + command + ".usage");
    }

    public static String getSubCommandDescription(String command) {
        return PartyChat.getDataHandler().commandConfig.getString("party-subcommands." + command + ".description");
    }

    public static boolean getSubCommandDisabled(String command) {
        return PartyChat.getDataHandler().commandConfig.getBoolean("party-subcommands." + command + ".disabled");
    }

    public static int getSubCommandCooldown(String command) {
        return PartyChat.getDataHandler().commandConfig.getInt("party-subcommands." + command + ".cooldown");
    }

    public static List<String> getSubCommandAliases(String command) {
        return PartyChat.getDataHandler().commandConfig.getStringList("party-subcommands." + command + ".aliases")
                .stream().filter(alias -> !alias.equalsIgnoreCase("")).collect(Collectors.toList());
    }
}
