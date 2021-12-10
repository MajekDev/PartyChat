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
import dev.majek.pc.command.party.PartyLeave;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.chat.ChatUtils;
import dev.majek.pc.util.SkullCache;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Objects;

/**
 * Handles the gui shown when a player is in a party.
 */
public class GuiInParty extends Gui {

  public GuiInParty() {
    super("inParty", getConfigString("gui-title"), 9);
  }

  @Override
  protected void populateInventory(Player player) {
    User user = PartyChat.dataHandler().getUser(player);
    Party party = user.getParty();

    // This should never happen, but I want to know if it does
    if (party == null) {
      PartyChat.error("Error: PC-GUI_IN | The plugin is fine, but please report this error " +
          "code here: https://discord.gg/CGgvDUz");
      PartyChat.messageHandler().sendMessage(player, "error"); return;
    }

    // Player's head in the first slot
    ItemStack playerHead = SkullCache.getSkull(player);
    if (playerHead != null) {
      addLabel(0, playerHead);
      setDisplayName(0, ChatColor.AQUA + ChatUtils.applyColorCodes(user.getNickname()));
      setLore(0, getConfigString("gui-current-party").replace("%party%", party.getName()));
    }

    // Generate book item for members list - slot decided later
    ItemStack membersBook = getItemStack(handler().getToggle("view-members"));
    if (membersBook != null) {
      ItemMeta meta = membersBook.getItemMeta();
      meta.setDisplayName(ChatUtils.applyColorCodes(getConfigString("gui-party-members")));
      meta.setLore(Collections.singletonList(ChatUtils.applyColorCodes(
          getConfigString("gui-click-members"))));
      membersBook.setItemMeta(meta);
    }

    // Invite player item - slot decided later
    ItemStack inviteStar = getItemStack(handler().getToggle("invite-player"));
    if (inviteStar != null) {
      ItemMeta meta = inviteStar.getItemMeta();
      meta.setDisplayName(ChatUtils.applyColorCodes(getConfigString("gui-invite")));
      inviteStar.setItemMeta(meta);
    }

    // Leave party item
    ItemStack leaveGlass = getItemStack(handler().getToggle("leave-party"));
    if (leaveGlass != null) {
      addActionItem(6, leaveGlass, () -> leaveParty(user));
      setDisplayName(6, getConfigString("gui-leave"));
    }

    // Close gui item in the last slot
    ItemStack closeBarrier = getItemStack(handler().getToggle("close-gui"));
    if (closeBarrier != null) {
      addActionItem(8, closeBarrier, player::closeInventory);
      setDisplayName(8, getConfigString("gui-close"));
    }

    if (user.isLeader()) {
      if (membersBook != null)
        addActionItem(1, membersBook, () -> new GuiMembers(user).openGui(player));
      if (inviteStar != null)
        addActionItem(2, inviteStar, () -> invitePlayer(user));

      // Summon party item
      ItemStack summonEye = getItemStack(handler().getToggle("summon-party"));
      if (summonEye != null) {
        addActionItem(3, summonEye, () -> summonParty(user));
        setDisplayName(3, getConfigString("gui-summon-party"));
        setLore(3, getConfigString("gui-summon-party-lore"));
      }

      // Rename party item
      ItemStack renameSign = getItemStack(handler().getToggle("rename-party"));
      if (renameSign != null) {
        addActionItem(4, renameSign, () -> renameParty(user));
        setDisplayName(4, getConfigString("gui-rename-party"));
        setLore(4, getConfigString("gui-rename-party-lore"));
      }

      // Party toggle public/private item
      ItemStack toggleConcrete;
      if (party.isPublic()) {
        toggleConcrete = getItemStack(handler().getToggle("public-party"));
        if (toggleConcrete != null) {
          addActionItem(5, toggleConcrete, () -> togglePrivate(user));
          setDisplayName(5, getConfigString("gui-public-party"));
          setLore(5, getConfigString("gui-public-party-lore1"),
              getConfigString("gui-public-party-lore2"));
        }
      } else {
        toggleConcrete = getItemStack(handler().getToggle("private-party"));
        if (toggleConcrete != null) {
          addActionItem(5, toggleConcrete, () -> togglePublic(user));
          setDisplayName(5, getConfigString("gui-private-party"));
          setLore(5, getConfigString("gui-private-party-lore1"),
              getConfigString("gui-private-party-lore2"));
        }
      }

      // Disband party item
      ItemStack disbandTnt = getItemStack(handler().getToggle("disband-party"));
      if (disbandTnt != null) {
        addActionItem(7, disbandTnt, () -> disbandParty(user));
        setDisplayName(7, getConfigString("gui-disband-party"));
        setLore(7, getConfigString("gui-disband-party-lore"));
      }

    } else {
      if (membersBook != null)
        addActionItem(2, membersBook, () -> new GuiMembers(user).openGui(player));
      if (inviteStar != null)
        addActionItem(4, inviteStar, () -> invitePlayer(user));
    }
  }

  private void leaveParty(User user) {
    if (user.getParty().getSize() > 1 && user.isLeader()) {
      user.setChatInputLeave(true);
      user.getPlayer().closeInventory();
      PartyChat.messageHandler().sendMessage(user, "type-leader-name");
      Bukkit.getScheduler().scheduleSyncDelayedTask(PartyChat.core(), () -> {
        if (user.isChatInputLeave()) {
          PartyLeave.execute(user, null, false);
          user.setChatInputLeave(false);
        }
      }, 200L);
    } else {
      PartyLeave.execute(user, null, false);
    }
  }

  private void renameParty(User user) {
    user.setChatInputRename(true);
    user.getPlayer().closeInventory();
    PartyChat.messageHandler().sendMessage(user, "type-party-name-new");
  }

  private void invitePlayer(User user) {
    user.setChatInputInvite(true);
    user.getPlayer().closeInventory();
    PartyChat.messageHandler().sendMessage(user, "type-player-name");
  }

  private void togglePublic(User user) {
    if (PartyChat.commandHandler().getCommand("toggle").isEnabled())
      Objects.requireNonNull(user.getParty()).setPublic(true);
    populateInventory(user.getPlayer());
  }

  private void togglePrivate(User user) {
    if (PartyChat.commandHandler().getCommand("toggle").isEnabled())
      Objects.requireNonNull(user.getParty()).setPublic(false);
    populateInventory(user.getPlayer());
  }

  private void summonParty(User user) {
    Objects.requireNonNull(user.getPlayer()).closeInventory();
    if (PartyChat.commandHandler().getCommand("summon").isEnabled())
      PartyChat.commandHandler().getCommand("summon")
          .execute(user.getPlayer(), new String[0], false);
  }

  private void disbandParty(User user) {
    Objects.requireNonNull(user.getPlayer()).closeInventory();
    if (PartyChat.commandHandler().getCommand("disband").isEnabled())
      PartyChat.commandHandler().getCommand("disband")
          .execute(user.getPlayer(), new String[0], false);
  }
}