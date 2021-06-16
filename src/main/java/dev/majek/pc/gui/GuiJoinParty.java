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
import dev.majek.pc.command.party.PartyJoin;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.util.SkullCache;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the gui when a player is trying to join a party.
 */
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
    List<Party> parties = PartyChat.partyHandler().getParties().stream()
        .filter(Party::isPublic).collect(Collectors.toList());

    for (int i = 0; i < 45 && i + page * 45 < parties.size(); ++i) {
      Party party = parties.get(i + page * 45);
      ItemStack leaderHead = SkullCache.getSkull(party.getLeader().getPlayerID()).clone();
      addActionItem(i, leaderHead, () -> {
        if (PartyChat.commandHandler().getCommand("join").isEnabled())
          PartyJoin.execute(player, party.getRawName());
        player.closeInventory();
      });
      setDisplayName(i, party.getName());
      setLore(i, (getConfigString("gui-leader") + party.getLeader().getNickname()),
          (getConfigString("gui-members") + party.getSize()));
    }

    int totalPages = totalPages();

    addActionItem(49, getItemStack(handler().getToggle("close-gui")),
        getConfigString("gui-go-back"), () -> new GuiNoParty().openGui(player));

    if(page < totalPages - 1)
      addActionItem(53, getItemStack(handler().getToggle("next-page")),
          getConfigString("gui-next"), () -> changeInventory(1));
    else
      addLabel(53, getItemStack(handler().getToggle("previous-page")),
          getConfigString("gui-no-next"));

    if(page > 0)
      addActionItem(45, getItemStack(handler().getToggle("next-page")),
          getConfigString("gui-previous"), () -> changeInventory(-1));
    else
      addLabel(45, getItemStack(handler().getToggle("previous-page")),
          getConfigString("gui-no-previous"));
  }

  private static String windowName(int page) {
    return getConfigString("gui-join-page").replace("%current%", String.valueOf(page + 1))
        .replace("%total%", String.valueOf(totalPages()));
  }

  private static int totalPages() {
    return 1 + PartyChat.partyHandler().getParties().size() / 46;
  }
}