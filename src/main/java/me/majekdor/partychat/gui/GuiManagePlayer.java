package me.majekdor.partychat.gui;

import dev.dbassett.skullcreator.SkullCreator;
import me.majekdor.partychat.command.party.PartyPromote;
import me.majekdor.partychat.command.party.PartyRemove;
import me.majekdor.partychat.util.Chat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiManagePlayer extends Gui {

    public Player target;

    public GuiManagePlayer(Player p) {
        super("ManagePlayer", "Manage Player", 9);
        target = p;
    }

    @Override
    protected void populateInventory(Player p) {

        ItemStack head = new ItemStack(SkullCreator.itemFromUuid(target.getUniqueId()));
        ItemMeta meta = head.getItemMeta(); List<String> lore = new ArrayList<>();
        meta.setDisplayName(Chat.colorize(target.getDisplayName())); head.setItemMeta(meta);
        addLabel(1, head);

        ItemStack promote = new ItemStack(Material.NETHER_STAR);
        meta = promote.getItemMeta(); meta.setDisplayName(ChatColor.YELLOW + "Click to promote player");
        lore.add(ChatColor.GRAY + "This will make " + target.getDisplayName()
                + " the party leader and demote you to a member");
        meta.setLore(lore); promote.setItemMeta(meta); lore.clear();
        addActionItem(3, promote, () -> promotePlayer(p, target));

        ItemStack remove = new ItemStack(Material.IRON_AXE);
        meta = remove.getItemMeta(); meta.setDisplayName(ChatColor.YELLOW + "Click to remove player");
        lore.add(ChatColor.GRAY + "This will remove the player from the party");
        meta.setLore(lore); remove.setItemMeta(meta); lore.clear();
        addActionItem(5, remove, () -> removePlayer(p, target));

        ItemStack close = new ItemStack(Material.BARRIER); meta = close.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Click to go back"); close.setItemMeta(meta);
        addActionItem(7, close, () -> new GuiMembers(p).openGui(p));
    }

    private void removePlayer(Player currentLeader, Player toRemove) {
        currentLeader.closeInventory();
        PartyRemove.execute(currentLeader, toRemove.getName());
    }

    private void promotePlayer(Player currentLeader, Player newLeader) {
        currentLeader.closeInventory();
        PartyPromote.execute(currentLeader, newLeader.getName());
    }
}
