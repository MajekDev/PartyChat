package Me.Majekdor.PartyChat.command;

import Me.Majekdor.PartyChat.util.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabComplete implements TabCompleter{
    List<String> arguments = new ArrayList<>();
    List<String> toggles = new ArrayList<>();
    public List<String> onTabComplete(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) throws IllegalArgumentException {
        if (cmd.getName().equalsIgnoreCase("party")) {
            if (arguments.isEmpty()) {
                arguments.add("create"); arguments.add("accept"); arguments.add("deny"); arguments.add("info");
                arguments.add("promote"); arguments.add("add"); arguments.add("remove"); arguments.add("leave");
                arguments.add("disband"); arguments.add("help"); arguments.add("join"); arguments.add("toggle");
            }
            if (toggles.isEmpty()) {
                toggles.add("public"); toggles.add("private");
            }
            List<String> result = new ArrayList<>();
            if (args.length == 1) {
                for (String s : arguments) {
                    if (s.toLowerCase().startsWith(args[0].toLowerCase()))
                        result.add(s);
                }
                return result;
            } else if (args.length == 2) {
                switch (args[0]) {
                    case ("create"):
                        return Collections.singletonList("<party-name>");
                    case ("join"):
                        List<String> returnParties = new ArrayList<>();
                        for (String party : CommandParty.parties) {
                            if (!(CommandParty.privateParties.contains(party)))
                                returnParties.add(Chat.removeColorCodes(party));
                        }
                        return returnParties;
                    case ("toggle"):
                        for (String s : toggles) {
                            if (s.toLowerCase().startsWith(args[1].toLowerCase()))
                                result.add(s);
                        }
                        return result;
                }
            }
        }
        if (cmd.getName().equalsIgnoreCase("partychat")) {
            return Collections.emptyList();
        }
        return null;
    }
}
