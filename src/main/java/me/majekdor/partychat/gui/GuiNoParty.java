package me.majekdor.partychat.gui;

import dev.dbassett.skullcreator.SkullCreator;
import me.majekdor.partychat.PartyChat;
import me.majekdor.partychat.command.party.PartyCreate;
import me.majekdor.partychat.util.Chat;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiNoParty extends Gui {

    public GuiNoParty() {
        super("noParty", "Party Chat", 9);
    }

    @Override
    protected void populateInventory(Player p) {
        inv.clear();

        ItemStack close = new ItemStack(Material.BARRIER); ItemMeta meta = close.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Click to exit"); close.setItemMeta(meta);
        addActionItem(7, close, p::closeInventory);

        List<String> lore = new ArrayList<>();
        ItemStack create = new ItemStack(Material.NETHER_STAR); meta = create.getItemMeta();
        lore.add(ChatColor.GRAY + "Click here to create a new party!");
        lore.add(ChatColor.GRAY + "Note: You may use color codes in your party name."); meta.setLore(lore);
        meta.setDisplayName(ChatColor.YELLOW + "Create A Party"); create.setItemMeta(meta);
        lore.clear(); addActionItem(3, create, () -> partyNameAnvil(p));

        String version = PartyChat.minecraftVersion;
        ItemStack join;
        if (Integer.parseInt(version) < 13)
            join = new ItemStack(Material.GREEN_WOOL);
        else
            join = new ItemStack(Material.GREEN_CONCRETE);

        meta = join.getItemMeta();
        lore.add(ChatColor.GRAY + "Click here to join an existing party!"); meta.setLore(lore);
        meta.setDisplayName(ChatColor.GREEN + "Join A Party"); join.setItemMeta(meta);
        addActionItem(5, join, () -> new GuiJoinParty().openGui(p)); lore.clear();

        ItemStack head = new ItemStack(SkullCreator.itemFromUuid(p.getUniqueId()));
        meta = head.getItemMeta();
        meta.setDisplayName(Chat.colorize("&r&b" + p.getDisplayName())); head.setItemMeta(meta);
        addLabel(1, head); lore.clear();
    }

    private void partyNameAnvil(Player p) {
        // Item in the first slot of anvil
        List<String> lore = new ArrayList<>();
        ItemStack slot1 = new ItemStack(Material.PAPER); ItemMeta meta = slot1.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Party Name");
        lore.add(ChatColor.GRAY + "Type in your party name and click on the output paper.");
        lore.add(ChatColor.GRAY + "Note: You may use color codes in the name."); meta.setLore(lore);
        slot1.setItemMeta(meta);

        new AnvilGUI.Builder()
                .onClose(player -> {                        // called when the inventory is closing
                })
                .onComplete((player, text) -> {             // called when the inventory output slot is clicked
                    String newText = text.replaceAll("\\s","");
                    PartyCreate.execute(player, newText);
                    return AnvilGUI.Response.close();
                })
                .text("Party Name")                         // sets the text the GUI should start with
                .title("Create New Party")
                .item(slot1)                                // use a custom item for the first slot
                .plugin(PartyChat.instance)                 // set the plugin instance
                .open(p);                                   // opens the GUI for the player provided
    }
}
