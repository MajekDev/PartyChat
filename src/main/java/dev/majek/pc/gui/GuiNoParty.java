package dev.majek.pc.gui;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.party.PartyCreate;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.util.Chat;
import dev.majek.pc.util.SkullCache;
import net.md_5.bungee.api.ChatColor;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GuiNoParty extends Gui {

    public GuiNoParty() {
        super("noParty", getConfigString("gui-title"), 9);
    }

    @Override
    protected void populateInventory(Player player) {
        User user = PartyChat.getDataHandler().getUser(player);

        // Player's head in the first slot
        ItemStack playerHead = SkullCache.getSkull(player);
        addLabel(1, playerHead);
        setDisplayName(1, ChatColor.AQUA + Chat.applyColorCodes(user.getNickname()));

        // Create new party item
        ItemStack createStar = new ItemStack(Material.NETHER_STAR);
        addActionItem(3, createStar, () -> createParty(user));
        setDisplayName(3, getConfigString("gui-create-party"));

        // Join an existing party item - if there are any
        if (PartyChat.getPartyHandler().getParties().stream().noneMatch(Party::isPublic)) {
            ItemStack noPartiesConcrete = new ItemStack(Material.RED_CONCRETE);
            addLabel(5, noPartiesConcrete);
            setDisplayName(5, getConfigString("gui-no-parties"));
        } else {
            ItemStack joinConcrete = new ItemStack(Material.GREEN_CONCRETE);
            addActionItem(5, joinConcrete, () -> new GuiJoinParty().openGui(player));
            setDisplayName(5, getConfigString("gui-join-party"));
        }

        // Close gui item in the last slot
        ItemStack closeBarrier = new ItemStack(Material.BARRIER);
        addActionItem(7, closeBarrier, player::closeInventory);
        setDisplayName(7, getConfigString("gui-close"));
    }

    private void createParty(User user) {
        ItemStack firstSlot = new ItemStack(Material.PAPER);
        ItemMeta meta = firstSlot.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + getConfigString("anvil-create-prompt"));
        meta.setLore(lore);
        firstSlot.setItemMeta(meta);
        new AnvilGUI.Builder()
                .onClose(player -> {})
                .onComplete((player, text) -> {
                    text = text.replaceAll("\\s","-");
                    if (PartyChat.getCommandHandler().getCommand("create").isEnabled())
                        PartyCreate.execute(user.getPlayer(), text);
                    return AnvilGUI.Response.close();
                })
                .text(getConfigString("anvil-party-name"))
                .title(getConfigString("anvil-create"))
                .itemLeft(firstSlot)
                .plugin(PartyChat.getCore())
                .open(user.getPlayer());
    }
}
