package dev.majek.pc.gui;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.party.PartyPromote;
import dev.majek.pc.command.party.PartyRemove;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.util.Chat;
import dev.majek.pc.util.SkullCache;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.majek.pc.command.PartyCommand.sendMessage;

public class GuiManagePlayer extends Gui {

    private final User target;

    public GuiManagePlayer(User user) {
        super("managePlayer", getConfigString("gui-manage-player-title")
                .replace("%player%", user.getUsername()), 9);
        target = user;
    }

    @Override
    protected void populateInventory(Player player) {
        User user = PartyChat.getDataHandler().getUser(player);
        Party party = user.getParty();

        // This should never happen, but I want to know if it does
        if (party == null) {
            PartyChat.error("Error: PC-GUI_MAN | The plugin is fine, but please report this error " +
                    "code here: https://discord.gg/CGgvDUz");
            sendMessage(player, "error"); return;
        }

        // Target player's head
        ItemStack playerHead = SkullCache.getSkull(target.getPlayerID());
        ItemMeta meta = playerHead.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + Chat.applyColorCodes(target.getNickname()));
        playerHead.setItemMeta(meta);
        addLabel(1, playerHead);

        // Nether star to promote player to leader
        ItemStack promoteStar = new ItemStack(Material.NETHER_STAR);
        meta = promoteStar.getItemMeta();
        meta.setDisplayName(Chat.applyColorCodes(getConfigString("gui-promote-player")));
        List<String> lore = new ArrayList<>();
        lore.add(Chat.applyColorCodes(getConfigString("gui-promote-player-lore")).replace("%player%", target.getNickname()));
        meta.setLore(lore);
        promoteStar.setItemMeta(meta);
        lore.clear();
        addActionItem(3, promoteStar, () -> promotePlayer(user, target));

        // Iron axe to remove player from the party
        ItemStack removeAxe = new ItemStack(Material.IRON_AXE);
        meta = removeAxe.getItemMeta();
        meta.setDisplayName(Chat.applyColorCodes(getConfigString("gui-remove-player")));
        lore.add(Chat.applyColorCodes(getConfigString("gui-remove-player-lore")).replace("%player%", target.getNickname()));
        meta.setLore(lore);
        removeAxe.setItemMeta(meta);
        lore.clear();
        addActionItem(5, removeAxe, () -> removePlayer(user, target));

        // Barrier to go back to members gui
        ItemStack closeBarrier = new ItemStack(Material.BARRIER);
        addActionItem(7, closeBarrier, () -> new GuiMembers(user).openGui(player));
        setDisplayName(7, Chat.applyColorCodes(getConfigString("gui-go-back")));
    }

    private void removePlayer(User leader, User target) {
        Objects.requireNonNull(leader.getPlayer()).closeInventory();
        PartyRemove.execute(leader.getPlayer(), target.getUsername());
    }

    private void promotePlayer(User leader, User target) {
        Objects.requireNonNull(leader.getPlayer()).closeInventory();
        PartyPromote.execute(leader.getPlayer(), target.getUsername(), false);
    }
}
