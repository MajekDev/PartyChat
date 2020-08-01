package Me.Majekdor.PartyChat;

import Me.Majekdor.PartyChat.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpyCommand implements CommandExecutor{

    Main plugin;
    public SpyCommand(Main instance){
        plugin = instance;
    }

    //Stuff I'll need
    public static Map<String, Boolean> spyToggle = new HashMap<String, Boolean>();
    public static List<String> serverStaff = new ArrayList<String>();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        FileConfiguration m = MessageDataWrapper.MessageConfig.getConfig();

        //Get preferences from config file
        String prefix = m.getString("party-prefix");

        if (cmd.getName().equalsIgnoreCase("spy")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Main.format("&cSorry console, no spying for you.")); return true;
            }
            Player player = (Player) sender;
            if(player.hasPermission("partychat.spy")) {
                if (spyToggle.get(player.getName())) {
                    spyToggle.replace(player.getName(), false);
                    player.sendMessage(Main.format(m.getString(("spy-disabled")).replace("%prefix%", prefix)));
                } else if (!(spyToggle.get(player.getName()))){
                    spyToggle.replace(player.getName(), true);
                    player.sendMessage(Main.format((m.getString("spy-enabled")).replace("%prefix%", prefix)));
                }
                return true;
            } else {
                player.sendMessage(Main.format((m.getString("no-permission")).replace("%prefix%", prefix)));
                return true;
            }
        }
        return false;
    }
}

