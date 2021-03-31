package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.util.Utils;
import org.bukkit.entity.Player;

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

        // Send help messages
        if (args.length == 1 || args[1].equalsIgnoreCase("1")) {
            StringBuilder builder = new StringBuilder();
            builder.append("&f[${hover,&8Prev,&7No previous page.}&f] &b&lParty&e&lChat &7Help &f[${hover,&8Next,&7No next page.}&f]");
            for (PartyCommand partyCommand : PartyChat.getCommandHandler().getCommands()) {
                builder.append("\n&9&l").append(Utils.capitalize(partyCommand.getName())).append(" &8» ")
                        .append("   ${hover,&7Description,").append(partyCommand.getDescription())
                        .append("}    ${hover, &7Usage,").append(partyCommand.getUsage()).append("}");
            }
            sendFormattedMessage(player, builder.toString());
            return true;
        } else if (args[1].equalsIgnoreCase("2")) {
            return true;
        } else
            return false;
    }
}
