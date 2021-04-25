package dev.majek.pc.hooks;

import dev.majek.pc.PartyChat;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;

public class Vault {

    public static String getPlayerDisplayName(Player player) {
        Chat vaultChat = PartyChat.getCore().getServer().getServicesManager().load(Chat.class);
        if (vaultChat == null) {
            PartyChat.error("Couldn't hook into vault!");
            return null;
        }
        if (PartyChat.getCore().getConfig().getBoolean("use-vault-chat"))
            return vaultChat.getPlayerPrefix(player) + (PartyChat.getDataHandler().useDisplayNames
                    ? player.getDisplayName() : player.getName()) + vaultChat.getPlayerSuffix(player);
        else
            return PartyChat.getDataHandler().useDisplayNames ? player.getDisplayName() : player.getName();
    }
}
