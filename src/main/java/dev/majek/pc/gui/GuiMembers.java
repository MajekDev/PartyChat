package dev.majek.pc.gui;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.Restrictions;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.util.Chat;
import dev.majek.pc.util.SkullCache;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.majek.pc.command.PartyCommand.sendMessage;

public class GuiMembers extends Gui {

    private int page;

    public GuiMembers(User user) {
        super("partyMembers", windowName(0, user), 54);
    }

    private void changeInventory(int move, User user) {
        page += move;
        newInventory(54, windowName(page, user));
    }

    @Override
    protected void populateInventory(Player player) {
        User user = PartyChat.getDataHandler().getUser(player);
        Party party = user.getParty();

        // This should never happen, but I want to know if it does
        if (party == null) {
            PartyChat.error("Error: PC-GUI_MEM | The plugin is fine, but please report this error " +
                    "code here: https://discord.gg/CGgvDUz");
            sendMessage(player, "error"); return;
        }

        List<User> members = party.getMembers();
        for (int i = 0; i < 45 && i + page * 45 < members.size(); ++i) {
            User member = members.get(i + page * 45);
            if (member.getPlayer() != null) {
                if (Restrictions.isVanished(member.getPlayer()) && PartyChat.getDataHandler().getConfigBoolean(PartyChat
                        .getDataHandler().mainConfig, "hide-vanished-players"))
                    continue;
            }
            ItemStack playerHead = SkullCache.getSkull(member.getPlayerID()).clone();
            ItemMeta meta = playerHead.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + Chat.applyColorCodes(member.getNickname()));
            List<String> lore = new ArrayList<>();
            if (user.isLeader()) {
                lore.add(Chat.applyColorCodes(getConfigString("gui-manage-player")));
                meta.setLore(lore);
                playerHead.setItemMeta(meta);
                addActionItem(i, playerHead, () -> new GuiManagePlayer(member).openGui(player));
            } else {
                playerHead.setItemMeta(meta);
                addLabel(i, playerHead);
            }
        }

        int totalPages = totalPages(user);

        addActionItem(49, Material.BARRIER, getConfigString("gui-go-back"), () -> new GuiInParty().openGui(player));

        if(page < totalPages - 1)
            addActionItem(53, Material.EMERALD_BLOCK, getConfigString("gui-next"), () -> changeInventory(1, user));
        else
            addLabel(53, Material.REDSTONE_BLOCK, getConfigString("gui-no-next"));

        if(page > 0)
            addActionItem(45, Material.EMERALD_BLOCK, getConfigString("gui-previous"), () -> changeInventory(-1, user));
        else
            addLabel(45, Material.REDSTONE_BLOCK, getConfigString("gui-no-previous"));
    }

    private static String windowName(int page, User user) {
        return getConfigString("gui-members-page").replace("%current%", String.valueOf(page + 1))
                .replace("%total%", String.valueOf(totalPages(user)));
    }

    private static int totalPages(User user) {
        return 1 + Objects.requireNonNull(user.getParty()).getSize() / 46;
    }
}
