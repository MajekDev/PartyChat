/*
 * This file is part of PartyChat, licensed under the MIT License.
 *
 * Copyright (c) 2020-2022 Majekdor
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
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.chat.ChatUtils;
import dev.majek.pc.util.SkullCache;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Handles the gui when a player is not in a party.
 */
public class GuiNoParty extends Gui {

  public GuiNoParty() {
    super("noParty", getConfigString("gui-title"), 9);
  }

  @Override
  protected void populateInventory(Player player) {
    User user = PartyChat.dataHandler().getUser(player);

    // Player's head in the first slot
    ItemStack playerHead = SkullCache.getSkull(player).clone();
    addLabel(1, playerHead);
    setDisplayName(1, ChatColor.AQUA + ChatUtils.applyColorCodes(user.getNickname()));

    // Create new party item
    ItemStack createStar = getItemStack(handler().getToggle("create-party"));
    if (createStar != null) {
      addActionItem(3, createStar, () -> createParty(user));
      setDisplayName(3, getConfigString("gui-create-party"));
    }

    // Join an existing party item - if there are any
    if (PartyChat.partyHandler().getParties().stream().noneMatch(Party::isPublic)) {
      ItemStack noPartiesConcrete = getItemStack(handler().getToggle("no-parties"));
      if (noPartiesConcrete != null) {
        addLabel(5, noPartiesConcrete);
        setDisplayName(5, getConfigString("gui-no-parties"));
      }
    } else {
      ItemStack joinConcrete = getItemStack(handler().getToggle("join-party"));
      if (joinConcrete != null) {
        addActionItem(5, joinConcrete, () -> new GuiJoinParty().openGui(player));
        setDisplayName(5, getConfigString("gui-join-party"));
      }
    }

    // Close gui item in the last slot
    ItemStack closeBarrier = getItemStack(handler().getToggle("close-gui"));
    addActionItem(7, closeBarrier, player::closeInventory);
    setDisplayName(7, getConfigString("gui-close"));
  }

  private void createParty(User user) {
    user.setChatInputCreate(true);
    user.getPlayer().closeInventory();
    PartyChat.messageHandler().sendMessage(user, "type-party-name");
   }
}