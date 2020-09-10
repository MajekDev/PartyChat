package Me.Majekdor.PartyChat.command;

import Me.Majekdor.PartyChat.PartyChat;
import Me.Majekdor.PartyChat.util.Chat;
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

public class CommandSpy implements CommandExecutor{

    PartyChat plugin;
    public CommandSpy(PartyChat instance){
        plugin = instance;
    }

    //Stuff I'll need
    public static Map<String, Boolean> spyToggle = new HashMap<>();
    public static List<String> serverStaff = new ArrayList<>();

    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {

        FileConfiguration m = PartyChat.messageData.getConfig();

        if (cmd.getName().equalsIgnoreCase("spy")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Chat.format("&cSorry console, no spying for you.")); return true;
            }
            Player player = (Player) sender;
            if(player.hasPermission("partychat.admin")) {
                if (spyToggle.get(player.getName())) {
                    spyToggle.replace(player.getName(), false);
                    player.sendMessage(Chat.format(m.getString(("spy-disabled"))));
                } else if (!(spyToggle.get(player.getName()))){
                    spyToggle.replace(player.getName(), true);
                    player.sendMessage(Chat.format((m.getString("spy-enabled"))));
                }
                return true;
            } else {
                player.sendMessage(Chat.format((m.getString("no-permission"))));
                return true;
            }
        }
        return false;
    }
}

