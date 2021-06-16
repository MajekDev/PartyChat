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
import dev.majek.pc.command.party.PartyPromote;
import dev.majek.pc.command.party.PartyRemove;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.chat.ChatUtils;
import dev.majek.pc.util.SkullCache;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handles the gui when a leader is managing a player.
 */
public class GuiManagePlayer extends Gui {

  private final User target;

  public GuiManagePlayer(User user) {
    super("managePlayer", getConfigString("gui-manage-player-title")
        .replace("%player%", user.getUsername()), 9);
    target = user;
  }

  @Override
  protected void populateInventory(Player player) {
    User user = PartyChat.dataHandler().getUser(player);
    Party party = user.getParty();

    // This should never happen, but I want to know if it does
    if (party == null) {
      PartyChat.error("Error: PC-GUI_MAN | The plugin is fine, but please report this error " +
          "code here: https://discord.gg/CGgvDUz");
      PartyChat.messageHandler().sendMessage(player, "error"); return;
    }

    // Target player's head
    ItemStack playerHead = SkullCache.getSkull(target.getPlayerID()).clone();
    ItemMeta meta = playerHead.getItemMeta();
    meta.setDisplayName(ChatColor.RESET + ChatUtils.applyColorCodes(target.getNickname()));
    playerHead.setItemMeta(meta);
    addLabel(1, playerHead);

    // Nether star to promote player to leader
    ItemStack promoteStar = getItemStack(handler().getToggle("promote-player"));
    List<String> lore = new ArrayList<>();
    if (promoteStar != null) {
      meta = promoteStar.getItemMeta();
      meta.setDisplayName(ChatUtils.applyColorCodes(getConfigString("gui-promote-player")));
      lore.add(ChatUtils.applyColorCodes(getConfigString("gui-promote-player-lore"))
          .replace("%player%", target.getNickname()));
      meta.setLore(lore);
      promoteStar.setItemMeta(meta);
      lore.clear();
      addActionItem(3, promoteStar, () -> promotePlayer(user, target));
    }

    // Iron axe to remove player from the party
    ItemStack removeAxe = getItemStack(handler().getToggle("remove-player"));
    if (removeAxe != null) {
      meta = removeAxe.getItemMeta();
      meta.setDisplayName(ChatUtils.applyColorCodes(getConfigString("gui-remove-player")));
      lore.add(ChatUtils.applyColorCodes(getConfigString("gui-remove-player-lore"))
          .replace("%player%", target.getNickname()));
      meta.setLore(lore);
      removeAxe.setItemMeta(meta);
      lore.clear();
      addActionItem(5, removeAxe, () -> removePlayer(user, target));
    }

    // Barrier to go back to members gui
    ItemStack closeBarrier = getItemStack(handler().getToggle("close-gui"));
    addActionItem(7, closeBarrier, () -> new GuiMembers(user).openGui(player));
    setDisplayName(7, ChatUtils.applyColorCodes(getConfigString("gui-go-back")));
  }

  private void removePlayer(User leader, User target) {
    Objects.requireNonNull(leader.getPlayer()).closeInventory();
    if (PartyChat.commandHandler().getCommand("remove").isEnabled())
      PartyRemove.execute(leader.getPlayer(), target.getUsername());
  }

  private void promotePlayer(User leader, User target) {
    Objects.requireNonNull(leader.getPlayer()).closeInventory();
    if (PartyChat.commandHandler().getCommand("promote").isEnabled())
      PartyPromote.execute(leader.getPlayer(), target.getUsername(), false);
  }
}