package Me.Majekdor.PartyChat.command;

import Me.Majekdor.PartyChat.PartyChat;
import Me.Majekdor.PartyChat.util.Chat;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandPartyChat implements CommandExecutor {

    public static Map<String, Boolean> partyChat = new HashMap<>();

    FileConfiguration m = PartyChat.messageData.getConfig(); FileConfiguration c = PartyChat.instance.getConfig();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(Chat.colorize("&cConsole cannot use this command!"));
            return true;
        }

        Player p = (Player) sender;
        if (c.getBoolean("use-permissions")) {
            if (!p.hasPermission("partychat.use")) {
                p.sendMessage(Chat.format(m.getString("no-permission"))); return true;
            }
        }

        if (cmd.getName().equalsIgnoreCase("partychat")) {
            if (!(CommandParty.inParty.containsKey(p.getName()))) {
                p.sendMessage(Chat.format(m.getString("not-in-party"))); return true;
            }
            if (args.length == 0) {
                if (!(partyChat.get(p.getName()))) {
                    partyChat.replace(p.getName(), true);
                    p.sendMessage(Chat.format(m.getString("pc-enabled")));
                } else {
                    partyChat.replace(p.getName(), false);
                    p.sendMessage(Chat.format(m.getString("pc-disabled")));
                }
            }
            if (args.length >= 1) {
                // Send message to party chat if they type /pc <message>
                StringBuilder message = new StringBuilder();
                for (String arg : args) {
                    message.append(" ").append(arg);
                }
                String withoutSpace = message.substring(1);
                String partyName = CommandParty.inParty.get(p.getName());
                List<Player> partymembers = CommandParty.partyMembers.get(partyName);
                List<String> messageReceived = new ArrayList<>();
                // Log all PartyChat messages to console
                Bukkit.getConsoleSender().sendMessage(Chat.colorize(ChatColor.RED + "[PCSPY] [" + partyName + ChatColor.RED + "] " + p.getName() + ":" + message));
                for (Player pl : partymembers) {
                    if (!(pl == null)) {
                        messageReceived.add(pl.getName());
                        pl.sendMessage(Chat.format((m.getString("message-format") + withoutSpace)
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
        }

        return false;
    }
}

