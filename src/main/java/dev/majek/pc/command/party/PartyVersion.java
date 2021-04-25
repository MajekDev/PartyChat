package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.util.Chat;
import org.bukkit.entity.Player;

public class PartyVersion extends PartyCommand {

    public PartyVersion() {
        super(
                "version", getSubCommandUsage("version"), getSubCommandDescription("version"),
                false, getSubCommandDisabled("version"), getSubCommandCooldown("version"),
                getSubCommandAliases("version")
        );
    }

    @Override
    public boolean execute(Player player, String[] args, boolean leftServer) {
        return execute(player);
    }

    public static boolean execute(Player player) {
        for (String message : PartyChat.getDataHandler().getConfigStringList(PartyChat.getDataHandler().messages, "party-info"))
            player.sendMessage(Chat.applyColorCodes(message.replace("%prefix%", PartyChat.getDataHandler()
                    .getConfigString(PartyChat.getDataHandler().messages, "prefix"))
                    .replace("%version%", PartyChat.getCore().getDescription().getVersion())));
        return true;
    }
}
