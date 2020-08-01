package Me.Majekdor.PartyChat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class PartyTab implements TabCompleter{
    List<String> arguments = new ArrayList<String>();
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (arguments.isEmpty()) {
            arguments.add("create"); arguments.add("accept"); arguments.add("deny"); arguments.add("info"); arguments.add("promote");
            arguments.add("add"); arguments.add("remove"); arguments.add("leave"); arguments.add("disband"); arguments.add("help");
        }
        List<String> result = new ArrayList<String>();
        if (args.length == 1) {
            for (String s : arguments) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase()))
                    result.add(s);
            }
            return result;
        }
        return null;
    }
}
