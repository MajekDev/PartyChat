package dev.majek.partychat.gui;

import dev.dbassett.skullcreator.SkullCreator;
import dev.majek.partychat.PartyChat;
import dev.majek.partychat.data.Party;
import dev.majek.partychat.data.Restrictions;
import dev.majek.partychat.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuiMembers extends Gui {

    private int page;
    public static Party party;

    public GuiMembers(Player player) {
        super("PartyMembers", windowName(0, player), 54);
    }

    private void changeInventory(int move, Player player) {
        page += move;
        newInventory(54, windowName(page, player));
    }

    @Override
    protected void populateInventory(Player p) {
        List<UUID> members = Party.getParty(p).members;
        party = Party.getParty(p);
        for (int i = 0; i < 45 && i + page * 45 < members.size(); ++i) {
            OfflinePlayer member = Bukkit.getOfflinePlayer(members.get(i + page * 45));
            if (member.getPlayer() != null && Restrictions.isVanished(member.getPlayer())) // Don't include vanished players
                if (PartyChat.instance.getConfig().getBoolean("hide-vanished-players")) continue;
            ItemStack head = new ItemStack(SkullCreator.itemFromUuid(member.getUniqueId()));
            List<String> lore = new ArrayList<>();  ItemMeta meta = head.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA  + Chat.colorize(member.getName()));
            if (p.getUniqueId().equals(party.leader)) {
                lore.add(ChatColor.GRAY + "Click to manage player");
                meta.setLore(lore); head.setItemMeta(meta);
                addActionItem(i, head, () -> new GuiManagePlayer(member).openGui(p));
            } else {
                head.setItemMeta(meta);
                addLabel(i, head);
            }
        }

        int totalPages = totalPages(p);

        addActionItem(49, Material.BARRIER, ChatColor.YELLOW + "Go Back", () -> new GuiInParty().openGui(p));

        if(page < totalPages - 1)
            addActionItem(53, Material.EMERALD_BLOCK, ChatColor.GREEN.toString() + "Next", () -> changeInventory(1, p));
        else
            addLabel(53, Material.REDSTONE_BLOCK, ChatColor.RED.toString() + "No Next Page");

        if(page > 0)
            addActionItem(45, Material.EMERALD_BLOCK, ChatColor.GREEN.toString() + "Previous", () -> changeInventory(-1, p));
        else
            addLabel(45, Material.REDSTONE_BLOCK, ChatColor.RED.toString() + "No Previous Page");
    }

    private static String windowName(int page, Player player) {
        return "Party Members (Page " + (page + 1) + "/" + totalPages(player) + ")";
    }

    private static int totalPages(Player player) {
        return 1 + Party.getParty(player).size / 46;
    }

}
