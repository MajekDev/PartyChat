package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.util.Paginate;
import dev.majek.pc.util.Utils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PartyHelp extends PartyCommand {

    public PartyHelp() {
        super(
                "help", getSubCommandUsage("help"), getSubCommandDescription("help"),
                false, getSubCommandDisabled("help"), getSubCommandCooldown("help"),
                getSubCommandAliases("help")
        );
    }

    @Override
    public boolean execute(Player player, String[] args, boolean leftServer) {

        // Build list of lines
        List<String> lines = new ArrayList<>();
        for (PartyCommand partyCommand : PartyChat.getCommandHandler().getCommands()) {
            String line = "&9&l" + Utils.capitalize(partyCommand.getName()) + " &8»" + "${hover,&7 " + PartyChat
                    .getDataHandler().getConfigString(PartyChat.getDataHandler().messages, "description") + ","
                    + partyCommand.getDescription() + "}${hover, &7" + PartyChat.getDataHandler()
                    .getConfigString(PartyChat.getDataHandler().messages, "usage") + "," +
                    partyCommand.getUsage() + "}";
            lines.add(line);
        }

        // Create pagination from the lines
        Paginate paginate = new Paginate(lines, PartyChat.getDataHandler().getConfigString(PartyChat.getDataHandler()
                .messages, "header").replace("%prefix%", PartyChat.getDataHandler().getConfigString(PartyChat
                .getDataHandler().messages, "prefix")), 8, "party help ");
        String toSend = args.length == 1 ? paginate.getPage(1) : paginate.getPage(Integer.parseInt(args[1]));

        if (toSend == null) {
            sendMessageWithReplacement(player, "invalid-page", "%max%", String.valueOf(paginate.getMaxPage()));
            return true;
        }

        sendFormattedMessage(player, toSend);
        return true;
    }
}
