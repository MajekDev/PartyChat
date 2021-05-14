package dev.majek.pc.gui;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.party.PartyJoin;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.util.SkullCache;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class GuiJoinParty extends Gui {

    private int page;

    public GuiJoinParty() {
        super("joinParty", windowName(0), 54);
    }

    private void changeInventory(int move) {
        page += move;
        newInventory(54, windowName(page));
    }

    @Override
    protected void populateInventory(Player player) {
        List<Party> parties = PartyChat.getPartyHandler().getParties().stream().filter(Party::isPublic).collect(Collectors.toList());

        for (int i = 0; i < 45 && i + page * 45 < parties.size(); ++i) {
            Party party = parties.get(i + page * 45);
            ItemStack leaderHead = SkullCache.getSkull(party.getLeader().getPlayerID()).clone();
            addActionItem(i, leaderHead, () -> {
                if (PartyChat.getCommandHandler().getCommand("join").isEnabled())
                    PartyJoin.execute(player, party.getRawName());
                player.closeInventory();
            });
            setDisplayName(i, party.getName());
            setLore(i, (getConfigString("gui-leader") + party.getLeader().getNickname()),
                    (getConfigString("gui-members") + party.getSize()));
        }

        int totalPages = totalPages();

        addActionItem(49, Material.BARRIER, getConfigString("gui-go-back"), () -> new GuiNoParty().openGui(player));

        if(page < totalPages - 1)
            addActionItem(53, Material.EMERALD_BLOCK, getConfigString("gui-next"), () -> changeInventory(1));
        else
            addLabel(53, Material.REDSTONE_BLOCK, getConfigString("gui-no-next"));

        if(page > 0)
            addActionItem(45, Material.EMERALD_BLOCK, getConfigString("gui-previous"), () -> changeInventory(-1));
        else
            addLabel(45, Material.REDSTONE_BLOCK, getConfigString("gui-no-previous"));
    }

    private static String windowName(int page) {
        return getConfigString("gui-join-page").replace("%current%", String.valueOf(page + 1))
                .replace("%total%", String.valueOf(totalPages()));
    }

    private static int totalPages() {
        return 1 + PartyChat.getPartyHandler().getParties().size() / 46;
    }
}
