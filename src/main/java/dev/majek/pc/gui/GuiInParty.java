package dev.majek.pc.gui;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.bukkit.entity.Player;

import static dev.majek.pc.command.PartyCommand.sendMessage;

public class GuiInParty extends Gui {

    protected GuiInParty() {
        super("inParty", "Party Chat", 9);
    }

    @Override
    protected void populateInventory(Player player) {
        User user = PartyChat.getDataHandler().getUser(player);
        Party party = user.getParty();

        // This should never happen, but I want to know if it does
        if (party == null) {
            PartyChat.error("Error: PC-GUI_IN_1 | The plugin is fine, but please report this error " +
                    "code here: https://discord.gg/CGgvDUz");
            sendMessage(player, "error"); return;
        }



    }
}
