package me.majekdor.partychat.command.party;

import me.majekdor.partychat.command.CommandParty;
import me.majekdor.partychat.data.Party;
import me.majekdor.partychat.util.Chat;
import me.majekdor.partychat.util.TabCompleterBase;
import me.majekdor.partychat.util.TextUtils;
import me.majekdor.partychat.util.Utils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PartyShareItem extends CommandParty {

    public static void execute(Player player, String[] args) {

        // Check if the player is not in a party
        if (!(Party.inParty.containsKey(player))) {
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
        for (Player member : party.members) {
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
