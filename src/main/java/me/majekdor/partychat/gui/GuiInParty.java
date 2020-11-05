package me.majekdor.partychat.gui;

import dev.dbassett.skullcreator.SkullCreator;
import me.majekdor.partychat.PartyChat;
import me.majekdor.partychat.command.party.*;
import me.majekdor.partychat.data.Party;
import me.majekdor.partychat.util.Chat;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiInParty extends Gui {

    public GuiInParty() {
        super("inParty", "Party Chat", 9);
    }

    @Override
    protected void populateInventory(Player p) {
        inv.clear(); // Don't think this is actually necessary?

        Party party = Party.getParty(p);

        ItemStack head = new ItemStack(SkullCreator.itemFromUuid(p.getUniqueId()));
        ItemMeta meta = head.getItemMeta(); List<String> lore = new ArrayList<>();
        meta.setDisplayName(Chat.colorize("&r&b" + p.getDisplayName())); head.setItemMeta(meta);
        addLabel(0, head);
        setLore(0, Chat.colorize("&7Current party: " + party.name));

        ItemStack members = new ItemStack(Material.WRITABLE_BOOK); meta = members.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Party Members");
        lore.add(ChatColor.GRAY + "Click to view all of the current members of the party.");
        meta.setLore(lore); members.setItemMeta(meta); lore.clear();

        ItemStack invite = new ItemStack(Material.NETHER_STAR); meta = invite.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Click to invite a player"); invite.setItemMeta(meta);

        ItemStack leave = new ItemStack(Material.RED_STAINED_GLASS_PANE); meta = leave.getItemMeta();
        meta.setDisplayName(Chat.colorize("&eClick to leave " + party.name));
        leave.setItemMeta(meta);

        ItemStack close = new ItemStack(Material.BARRIER); meta = close.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Click to close"); close.setItemMeta(meta);
        addActionItem(8, close, p::closeInventory);

        if (Party.isLeader(party, p)) {

            addActionItem(1, members, () -> new GuiMembers(p).openGui(p));
            addActionItem(2, invite, () -> invitePlayerAnvil(p));

            ItemStack summon = new ItemStack(Material.ENDER_PEARL); meta = summon.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Click to summon party");
            lore.add(ChatColor.GRAY + "Request that all party members teleport to you."); meta.setLore(lore);
            summon.setItemMeta(meta); lore.clear();
            addActionItem(3, summon, () -> summonParty(p));

            ItemStack rename = new ItemStack(Material.OAK_SIGN); meta = rename.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Click to rename party"); rename.setItemMeta(meta);
            addActionItem(4, rename, () -> partyRenameAnvil(p));

            if (party.isPublic) {
                ItemStack publicParty = new ItemStack(Material.GREEN_CONCRETE); meta = publicParty.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + "Public party");
                lore.add(ChatColor.GRAY + "Players can request to join this party");
                lore.add(ChatColor.GRAY + "Click to make the party private"); meta.setLore(lore);
                publicParty.setItemMeta(meta); lore.clear();
                addActionItem(5, publicParty, () -> togglePrivate(p));
            } else {
                ItemStack privateParty = new ItemStack(Material.RED_CONCRETE); meta = privateParty.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "Private party");
                lore.add(ChatColor.GRAY + "Players cannot request to join this party");
                lore.add(ChatColor.GRAY + "Click to make the party public"); meta.setLore(lore);
                privateParty.setItemMeta(meta); lore.clear();
                addActionItem(5, privateParty, () -> togglePublic(p));
            }

            addActionItem(6, leave, () -> leaderLeaveAnvil(p, false));

            ItemStack disband = new ItemStack(Material.TNT); meta = disband.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Click to disband party");
            lore.add(ChatColor.GRAY + "This will delete the party and remove all players"); meta.setLore(lore);
            disband.setItemMeta(meta); lore.clear();
            addActionItem(7, disband, () -> disbandParty(p));

        } else {

            addActionItem(2, members, () -> new GuiMembers(p).openGui(p));
            addActionItem(4, invite, () -> invitePlayerAnvil(p));
            addActionItem(6, leave, () -> leaveParty(p));
        }
    }

    public boolean leave = true;

    private void leaderLeaveAnvil(Player p, boolean tryAgain) {
        // Item in the first slot of anvil
        List<String> lore = new ArrayList<>();
        ItemStack slot1 = new ItemStack(Material.PAPER); ItemMeta meta = slot1.getItemMeta();
        lore.add(ChatColor.GRAY + "Type in the new party leader's name and click on the output paper.");
        if (tryAgain) {
            lore.add("");
            lore.add(ChatColor.RED + "The previous player specified is not in the party!");
        }
        meta.setLore(lore);
        slot1.setItemMeta(meta);

        new AnvilGUI.Builder()
                .onClose(player -> {                        // called when the inventory is closing
                    if (Party.inParty.containsKey(player.getUniqueId())) {
                        if (leave) {
                            PartyLeave.execute(p, false);
                        }
                    }
                    leave = true;
                })
                .onComplete((player, text) -> {             // called when the inventory output slot is clicked
                    String newText = text.replaceAll("\\s","");
                    Player target = Bukkit.getPlayerExact(newText);
                    if (target == null || !Party.getParty(player).members.contains(target.getUniqueId())) {
                        wait(player);
                    } else if (Party.getParty(player).members.contains(target.getUniqueId())) {
                        PartyPromote.execute(player, newText);
                        PartyLeave.execute(player, false);
                    }
                    leave = false;
                    return AnvilGUI.Response.close();
                })
                .text("Username")                           // sets the text the GUI should start with
                .title("Promote Player")
                .item(slot1)                                // use a custom item for the first slot
                .plugin(PartyChat.instance)                 // set the plugin instance
                .open(p);                                   // opens the GUI for the player provided
    }

    private void partyRenameAnvil(Player p) {
        // Item in the first slot of anvil
        List<String> lore = new ArrayList<>();
        ItemStack slot1 = new ItemStack(Material.PAPER); ItemMeta meta = slot1.getItemMeta();
        lore.add(ChatColor.GRAY + "Type in the new party name and click on the output paper.");
        meta.setLore(lore);
        slot1.setItemMeta(meta);

        new AnvilGUI.Builder()
                .onClose(player -> {                        // called when the inventory is closing
                })
                .onComplete((player, text) -> {             // called when the inventory output slot is clicked
                    String newText = text.replaceAll("\\s","");
                    PartyRename.execute(player, newText);
                    return AnvilGUI.Response.close();
                })
                .text("Party Name")                         // sets the text the GUI should start with
                .title("Rename Your Party")
                .item(slot1)                                // use a custom item for the first slot
                .plugin(PartyChat.instance)                 // set the plugin instance
                .open(p);                                   // opens the GUI for the player provided
    }

    private void invitePlayerAnvil(Player p) {
        // Item in the first slot of anvil
        List<String> lore = new ArrayList<>();
        ItemStack slot1 = new ItemStack(Material.PAPER); ItemMeta meta = slot1.getItemMeta();
        lore.add(ChatColor.GRAY + "Type in the player's name and click on the output paper.");
        meta.setLore(lore);
        slot1.setItemMeta(meta);

        new AnvilGUI.Builder()
                .onClose(player -> {                        // called when the inventory is closing
                })
                .onComplete((player, text) -> {             // called when the inventory output slot is clicked
                    String newText = text.replaceAll("\\s","");
                    PartyAdd.execute(player, newText);
                    return AnvilGUI.Response.close();
                })
                .text("Username")                           // sets the text the GUI should start with
                .title("Invite New Player")
                .item(slot1)                                // use a custom item for the first slot
                .plugin(PartyChat.instance)                 // set the plugin instance
                .open(p);                                   // opens the GUI for the player provided
    }

    private void togglePublic(Player p) {
        Party party = Party.getParty(p);
        party.isPublic = true;
        populateInventory(p);
    }

    private void togglePrivate(Player p) {
        Party party = Party.getParty(p);
        party.isPublic = false;
        populateInventory(p);
    }

    private void leaveParty(Player p) {
        p.closeInventory();
        PartyLeave.execute(p, false);
    }

    private void summonParty(Player p) {
        p.closeInventory();
        PartySummon.execute(p);
    }

    private void disbandParty(Player p) {
        p.closeInventory();
        PartyDisband.execute(p);
    }

    private void wait(Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () -> leaderLeaveAnvil(player, true), 5L);
    }
}
