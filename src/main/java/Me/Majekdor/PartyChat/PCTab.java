package Me.Majekdor.PartyChat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public class PCTab implements TabCompleter {
    // This cancels tab complete for /pc so the player isn't spammed with online player names when typing their message
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }
}
