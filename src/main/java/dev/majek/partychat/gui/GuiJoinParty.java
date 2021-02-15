package dev.majek.partychat.gui;

import dev.dbassett.skullcreator.SkullCreator;
import dev.majek.partychat.command.party.PartyJoin;
import dev.majek.partychat.data.Party;
import dev.majek.partychat.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiJoinParty extends Gui {
    private int page;

    public GuiJoinParty() {
        super("JoinParty", windowName(0), 54);
    }

    private void changeInventory(int move) {
        page += move;
        newInventory(54, windowName(page));
    }

    @Override
    protected void populateInventory(Player p) {
        List<Party> parties = new ArrayList<>();
        for (Party partyCheck : Party.partyMap.values())
            if (partyCheck.isPublic && !parties.contains(partyCheck))
                parties.add(partyCheck);
        for (int i = 0; i < 45 && i + page * 45 < parties.size(); ++i) {
            Party party = parties.get(i + page * 45);
            ItemStack head = new ItemStack(SkullCreator.itemFromUuid(Bukkit.getOfflinePlayer(party.leader).getUniqueId()));
            List<String> lore = new ArrayList<>();  ItemMeta meta = head.getItemMeta();
            lore.add(Chat.colorize("&7Leader: &b" +  Bukkit.getOfflinePlayer(party.leader).getName()));
            lore.add(Chat.colorize("&7Members: &b" + party.size)); meta.setLore(lore);
            meta.setDisplayName(Chat.colorize("&r&e" + party.name)); head.setItemMeta(meta);
            addActionItem(i, head, () -> joinParty(p, party.name)); p.closeInventory();
        }

        int totalPages = totalPages();

        addActionItem(49, Material.BARRIER, ChatColor.YELLOW + "Go Back", () -> new GuiNoParty().openGui(p));

        if(page < totalPages - 1)
            addActionItem(53, Material.EMERALD_BLOCK, ChatColor.GREEN.toString() + "Next", () -> changeInventory(1));
        else
            addLabel(53, Material.REDSTONE_BLOCK, ChatColor.RED.toString() + "No Next Page");

        if(page > 0)
            addActionItem(45, Material.EMERALD_BLOCK, ChatColor.GREEN.toString() + "Previous", () -> changeInventory(-1));
        else
            addLabel(45, Material.REDSTONE_BLOCK, ChatColor.RED.toString() + "No Previous Page");
    }

    private static String windowName(int page) {
        return "Join A Party (Page " + (page + 1) + "/" + totalPages() + ")";
    }

    private static int totalPages() {
        return (int) (1 + Party.partyMap.values().stream().distinct().count() / 46);
    }

    private void joinParty(Player player, String name) {
        player.closeInventory();
        PartyJoin.execute(player, name);
    }
}
