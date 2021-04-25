package dev.majek.pc.gui;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.party.PartyAdd;
import dev.majek.pc.command.party.PartyPromote;
import dev.majek.pc.command.party.PartyRename;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.util.Chat;
import dev.majek.pc.util.SkullCache;
import net.md_5.bungee.api.ChatColor;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static dev.majek.pc.command.PartyCommand.sendMessage;

public class GuiInParty extends Gui {

    public GuiInParty() {
        super("inParty", getConfigString("gui-title"), 9);
    }

    @Override
    protected void populateInventory(Player player) {
        User user = PartyChat.getDataHandler().getUser(player);
        Party party = user.getParty();

        // This should never happen, but I want to know if it does
        if (party == null) {
            PartyChat.error("Error: PC-GUI_IN | The plugin is fine, but please report this error " +
                    "code here: https://discord.gg/CGgvDUz");
            sendMessage(player, "error"); return;
        }

        // Player's head in the first slot
        ItemStack playerHead = SkullCache.getSkull(player);
        addLabel(0, playerHead);
        setDisplayName(0, ChatColor.AQUA + Chat.applyColorCodes(user.getNickname()));
        setLore(0, getConfigString("gui-current-party").replace("%party%", party.getName()));

        // Generate book item for members list - slot decided later
        ItemStack membersBook = new ItemStack(Material.BOOK);
        ItemMeta meta = membersBook.getItemMeta();
        meta.setDisplayName(Chat.applyColorCodes(getConfigString("gui-party-members")));
        meta.setLore(Collections.singletonList(Chat.applyColorCodes(getConfigString("gui-click-members"))));
        membersBook.setItemMeta(meta);

        // Invite player item - slot decided later
        ItemStack inviteStar = new ItemStack(Material.NETHER_STAR);
        meta = inviteStar.getItemMeta();
        meta.setDisplayName(Chat.applyColorCodes(getConfigString("gui-invite")));
        inviteStar.setItemMeta(meta);

        // Leave party item
        ItemStack leaveGlass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        addActionItem(6, leaveGlass, () -> leaveParty(user, false));
        setDisplayName(6, getConfigString("gui-leave"));

        // Close gui item in the last slot
        ItemStack closeBarrier = new ItemStack(Material.BARRIER);
        addActionItem(8, closeBarrier, player::closeInventory);
        setDisplayName(8, getConfigString("gui-close"));

        if (user.isLeader()) {
            addActionItem(1, membersBook, () -> new GuiMembers(user).openGui(player));
            addActionItem(2, inviteStar, () -> invitePlayer(user));

            // Summon party item
            ItemStack summonEye = new ItemStack(Material.ENDER_PEARL);
            addActionItem(3, summonEye, () -> summonParty(user));
            setDisplayName(3, getConfigString("gui-summon-party"));
            setLore(3, getConfigString("gui-summon-party-lore"));

            // Rename party item
            ItemStack renameSign = new ItemStack(Material.OAK_SIGN);
            addActionItem(4, renameSign, () -> renameParty(user));
            setDisplayName(4, getConfigString("gui-rename-party"));
            setLore(4, getConfigString("gui-rename-party-lore"));

            // Party toggle public/private item
            ItemStack toggleConcrete;
            if (party.isPublic()) {
                toggleConcrete = new ItemStack(Material.GREEN_CONCRETE);
                addActionItem(5, toggleConcrete, () -> togglePrivate(user));
                setDisplayName(5, getConfigString("gui-public-party"));
                setLore(5, getConfigString("gui-public-party-lore1"), getConfigString("gui-public-party-lore2"));
            } else {
                toggleConcrete = new ItemStack(Material.RED_CONCRETE);
                addActionItem(5, toggleConcrete, () -> togglePublic(user));
                setDisplayName(5, getConfigString("gui-private-party"));
                setLore(5, getConfigString("gui-private-party-lore1"), getConfigString("gui-private-party-lore2"));
            }

            // Disband party item
            ItemStack disbandTnt = new ItemStack(Material.TNT);
            addActionItem(7, disbandTnt, () -> disbandParty(user));
            setDisplayName(7, getConfigString("gui-disband-party"));
            setLore(7, getConfigString("gui-disband-party-lore"));

        } else {
            addActionItem(2, membersBook, () -> new GuiMembers(user).openGui(player));
            addActionItem(4, inviteStar, () -> invitePlayer(user));
        }

    }

    private void leaveParty(User user, boolean tryAgain) {
        ItemStack firstSlot = new ItemStack(Material.PAPER);
        ItemMeta meta = firstSlot.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + getConfigString("anvil-promote-prompt"));
        if (tryAgain) {
            lore.add(ChatColor.GRAY + "");
            lore.add(ChatColor.GRAY + getConfigString("anvil-promote-error"));
        }
        meta.setLore(lore);
        firstSlot.setItemMeta(meta);
        if (!user.isLeader() || Objects.requireNonNull(user.getParty()).getSize() == 1) {
            PartyChat.getCommandHandler().getCommand("leave").execute(user.getPlayer(), new String[0], false);
            Objects.requireNonNull(user.getPlayer()).closeInventory();
        } else {
            new AnvilGUI.Builder()
                    .onClose(player -> PartyChat.getCommandHandler().getCommand("leave").execute(player, new String[0], false))
                    .onComplete((player, text) -> {
                        text = text.replaceAll("\\s","");
                        boolean completed = PartyPromote.execute(user.getPlayer(), text, true);
                        if (!completed)
                            tryAgain(user);
                        return AnvilGUI.Response.close();
                    })
                    .text(getConfigString("anvil-username"))
                    .title(getConfigString("anvil-promote-player"))
                    .itemLeft(firstSlot)
                    .plugin(PartyChat.getCore())
                    .open(user.getPlayer());
        }
    }

    private void tryAgain(User user) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(PartyChat.instance, () -> leaveParty(user, true), 5L);
    }

    private void renameParty(User user) {
        ItemStack firstSlot = new ItemStack(Material.PAPER);
        ItemMeta meta = firstSlot.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + getConfigString("anvil-rename-prompt"));
        meta.setLore(lore);
        firstSlot.setItemMeta(meta);
        new AnvilGUI.Builder()
                .onClose(player -> {})
                .onComplete((player, text) -> {
                    text = text.replaceAll("\\s","-");
                    PartyRename.execute(user.getPlayer(), text);
                    return AnvilGUI.Response.close();
                })
                .text(getConfigString("anvil-party-name"))
                .title(getConfigString("anvil-rename"))
                .itemLeft(firstSlot)
                .plugin(PartyChat.getCore())
                .open(user.getPlayer());
    }

    private void invitePlayer(User user) {
        ItemStack firstSlot = new ItemStack(Material.PAPER);
        ItemMeta meta = firstSlot.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + getConfigString("anvil-invite-prompt"));
        meta.setLore(lore);
        firstSlot.setItemMeta(meta);
        new AnvilGUI.Builder()
                .onClose(player -> {})
                .onComplete((player, text) -> {
                    text = text.replaceAll("\\s","");
                    PartyAdd.execute(user.getPlayer(), text);
                    return AnvilGUI.Response.close();
                })
                .text(getConfigString("anvil-username"))
                .title(getConfigString("anvil-invite-player"))
                .itemLeft(firstSlot)
                .plugin(PartyChat.getCore())
                .open(user.getPlayer());
    }

    private void togglePublic(User user) {
        Objects.requireNonNull(user.getParty()).setPublic(true);
        populateInventory(user.getPlayer());
    }

    private void togglePrivate(User user) {
        Objects.requireNonNull(user.getParty()).setPublic(false);
        populateInventory(user.getPlayer());
    }

    private void summonParty(User user) {
        Objects.requireNonNull(user.getPlayer()).closeInventory();
        PartyChat.getCommandHandler().getCommand("summon").execute(user.getPlayer(), new String[0], false);
    }

    private void disbandParty(User user) {
        Objects.requireNonNull(user.getPlayer()).closeInventory();
        PartyChat.getCommandHandler().getCommand("disband").execute(user.getPlayer(), new String[0], false);
    }
}
