package dev.majek.partychat.command.party;

import dev.majek.partychat.command.CommandParty;
import dev.majek.partychat.data.Party;
import dev.majek.partychat.util.Chat;
import dev.majek.partychat.util.TabCompleterBase;
import dev.majek.partychat.util.TextUtils;
import dev.majek.partychat.util.Utils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PartyShareItem extends CommandParty {

    public static void execute(Player player, String[] args) {

        // Check if the player is not in a party
        if (!Party.inParty(player)) {
            sendMessageWithPrefix(player, m.getString("not-in-party")); return;
        }

        // Make sure the player actually has an item in their hand
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            sendMessageWithPrefix(player, m.getString("no-item")); return;
        }

        // Convert ItemStack to json string
        String json = Utils.convertItemStackToJson(item);
        String name; // Correctly format the name
        if (item.getItemMeta().getDisplayName().equals("")) {
            name = item.getType().name().replace("_", " ");
            name = WordUtils.capitalizeFully(name);
        } else
            name = item.getItemMeta().getDisplayName();

        // Send message using sendFormatted to implement hover tool tip
        Party party = Party.getParty(player);
        for (UUID memberUUID : party.members) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member == null) continue;
            TextUtils.sendFormatted(member, false, Chat.format((m.getString("message-format") + "")
                    .replace("%partyName%", party.name).replace("%player%", player.getDisplayName())
                    + (args.length > 1 ? TabCompleterBase.joinArgsBeyond(0, " ", args) + " "
                    + "{$(item,"+json+",&(aqua)[i] " + name + "&(white))}" :
                    "{$(item,"+json+",&(aqua)[i] " + name + "&(white))}")));
        }

        // Log message to console
        if (args.length > 1)
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PCSPY] [" + party.name + ChatColor.RED + "] "
                    + player.getName() + ": " + TabCompleterBase.joinArgsBeyond(0, " ", args) + " [i] " + name);
        else
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PCSPY] [" + party.name + ChatColor.RED + "] "
                    + player.getName() + ": " + "[i] " + name);
    }
}
