package dev.majek.partychat.hooks;

import dev.majek.partychat.PartyChat;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;

public class Vault {

    public static String getPlayerDisplayName(Player player) {
        Chat vaultChat = PartyChat.getInstance().getServer().getServicesManager().load(Chat.class);
        if (vaultChat == null) {
            PartyChat.getInstance().getLogger().severe("Couldn't hook into vault!");
            return null;
        }
        if (PartyChat.getInstance().getConfig().getBoolean("use-vault-chat"))
            return vaultChat.getPlayerPrefix(player) + player.getDisplayName() + vaultChat.getPlayerSuffix(player);
        else
            return player.getDisplayName();
    }
}
