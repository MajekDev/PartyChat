/*
 * This file is part of PartyChat, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 Majekdor
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.majek.pc.gui;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.Restrictions;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.message.ChatUtils;
import dev.majek.pc.util.SkullCache;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handles the gui when a player is viewing the party members.
 */
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
    User user = PartyChat.dataHandler().getUser(player);
    Party party = user.getParty();

    // This should never happen, but I want to know if it does
    if (party == null) {
      PartyChat.error("Error: PC-GUI_MEM | The plugin is fine, but please report this error " +
          "code here: https://discord.gg/CGgvDUz");
      PartyChat.messageHandler().sendMessage(player, "error"); return;
    }

    List<User> members = party.getMembers();
    for (int i = 0; i < 45 && i + page * 45 < members.size(); ++i) {
      User member = members.get(i + page * 45);
      if (member.getPlayer() != null) {
        if (Restrictions.isVanished(member.getPlayer()) && PartyChat.dataHandler().getConfigBoolean(PartyChat
            .dataHandler().mainConfig, "hide-vanished-players"))
          continue;
      }
      ItemStack playerHead = SkullCache.getSkull(member.getPlayerID()).clone();
      ItemMeta meta = playerHead.getItemMeta();
      meta.setDisplayName(ChatColor.AQUA + ChatUtils.applyColorCodes(member.getNickname()));
      List<String> lore = new ArrayList<>();
      if (user.isLeader()) {
        lore.add(ChatUtils.applyColorCodes(getConfigString("gui-manage-player")));
        meta.setLore(lore);
        playerHead.setItemMeta(meta);
        addActionItem(i, playerHead, () -> new GuiManagePlayer(member).openGui(player));
      } else {
        playerHead.setItemMeta(meta);
        addLabel(i, playerHead);
      }
    }

    int totalPages = totalPages(user);

    addActionItem(49, getItemStack(handler().getToggle("close-gui")),
        getConfigString("gui-go-back"), () -> new GuiInParty().openGui(player));

    if(page < totalPages - 1)
      addActionItem(53, getItemStack(handler().getToggle("next-page")),
          getConfigString("gui-next"), () -> changeInventory(1, user));
    else
      addLabel(53, getItemStack(handler().getToggle("previous-page")),
          getConfigString("gui-no-next"));

    if(page > 0)
      addActionItem(45, getItemStack(handler().getToggle("next-page")),
          getConfigString("gui-previous"), () -> changeInventory(-1, user));
    else
      addLabel(45, getItemStack(handler().getToggle("previous-page")),
          getConfigString("gui-no-previous"));
  }

  private static String windowName(int page, User user) {
    return getConfigString("gui-members-page").replace("%current%",
        String.valueOf(page + 1)).replace("%total%", String.valueOf(totalPages(user)));
  }

  private static int totalPages(User user) {
    return 1 + Objects.requireNonNull(user.getParty()).getSize() / 46;
  }
}