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
import dev.majek.pc.hooks.HeadDatabase;
import dev.majek.pc.chat.ChatUtils;
import dev.majek.pc.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Used for constructing GUIs for a player to see and interact with.
 */
public abstract class Gui {

  public static final Runnable NO_ACTION = () -> { };

  protected final String guiName;
  protected final Map<Integer, Pair<Runnable, Boolean>> clickActions;
  protected Inventory inv;
  protected Player user;
  private boolean ignoreClose;

  protected Gui(String guiName, String displayName, int size) {
    this.guiName = guiName;
    this.clickActions = new HashMap<>();
    this.inv = Bukkit.createInventory(null, size, displayName);
    this.user = null;
    this.ignoreClose = false;
  }

  public Inventory getInventory() {
    return inv;
  }

  public boolean matches(InventoryEvent event) {
    return inv.equals(event.getInventory());
  }

  protected abstract void populateInventory(Player p);

  public void openGui(Player player) {
    user = player;
    populateInventory(user);
    player.openInventory(inv);
    PartyChat.guiHandler().registerActiveGui(this);
  }

  protected void setItem(int slot, Material material, String name, String... lore) {
    setItem(slot, new ItemStack(material), name, lore);
  }

  protected void setItem(int slot, ItemStack stack, String name, String... lore) {
    ItemMeta meta = stack.getItemMeta();
    meta.setDisplayName(ChatColor.RESET + ChatUtils.applyColorCodes(name) + ChatColor.RESET);
    meta.setLore(Arrays.asList(lore));
    stack.setItemMeta(meta);
    inv.setItem(slot, stack);
  }

  protected void setDisplayName(int slot, String name) {
    ItemStack stack = inv.getItem(slot);
    ItemMeta meta = stack.getItemMeta();
    meta.setDisplayName(ChatUtils.applyColorCodes(name));
    stack.setItemMeta(meta);
  }

  protected void setLore(int slot, String... lore) {
    ItemStack stack = inv.getItem(slot);
    ItemMeta meta = stack.getItemMeta();
    meta.setLore(Arrays.stream(lore).map(ChatUtils::applyColorCodes).collect(Collectors.toList()));
    stack.setItemMeta(meta);
  }

  protected void addActionItem(int slot, Material material, String name, Runnable action, boolean rightClickOnly, String... lore) {
    setItem(slot, material, name, lore);
    clickActions.put(slot, new Pair<>(action == null ? NO_ACTION : action, rightClickOnly));
  }

  protected void addActionItem(int slot, Material material, String name, Runnable action, String... lore) {
    addActionItem(slot, material, name, action, false, lore);
  }

  protected void addActionItem(int slot, ItemStack stack, String name, Runnable action, String... lore) {
    setItem(slot, stack, name, lore);
    clickActions.put(slot, new Pair<>(action == null ? NO_ACTION : action, false));
  }

  protected void addLabel(int slot, Material material, String name, String... lore) {
    addActionItem(slot, material, name, NO_ACTION, lore);
  }

  protected void addLabel(int slot, ItemStack stack, String name, String... lore) {
    addActionItem(slot, stack, name, NO_ACTION, lore);
  }

  protected  void addLabel(int slot, ItemStack item) {
    addActionItem(slot, item, NO_ACTION);
  }

  protected void addActionItem(int slot, ItemStack stack, Runnable action, boolean rightClickOnly) {
    inv.setItem(slot, stack);
    clickActions.put(slot, new Pair<>(action == null ? NO_ACTION : action, rightClickOnly));
  }

  protected void addActionItem(int slot, ItemStack stack, Runnable action) {
    addActionItem(slot, stack, action, false);
  }

  protected void newInventory(int size, String displayName) {
    ignoreClose = true;
    user.closeInventory();
    inv = Bukkit.createInventory(null, size, displayName);
    user.openInventory(inv);
    refreshInventory();
  }

  protected void refreshInventory() {
    inv.clear();
    clickActions.clear();
    populateInventory(user);
  }

  public void onItemClick(InventoryClickEvent event) {
    Pair<Runnable, Boolean> action = clickActions.get(event.getRawSlot());
    if(action != null && (!action.getSecond() || event.isRightClick())) {
      action.getFirst().run();
      event.setCancelled(true);
    }
  }

  public void onInventoryClosed() {
    if(ignoreClose) {
      ignoreClose = false;
    }else{
      onClose();
      PartyChat.guiHandler().removeActiveGui(this);
    }
  }

  protected void onClose() { }

  protected static ItemStack clone(ItemStack stack) {
    return stack == null ? null : stack.clone();
  }

  public static String getConfigString(String path) {
    return PartyChat.dataHandler().getConfigString(PartyChat.dataHandler().messages, path);
  }

  @Nullable
  public ItemStack getItemStack(GuiToggle guiToggle) {
    if (!guiToggle.isVisible())
      return null;
    if (guiToggle.hasHdbId())
      return HeadDatabase.isHead(guiToggle.hdbId()) ? HeadDatabase.getHead(guiToggle.hdbId())
          : new ItemStack(guiToggle.material());
    else
      return new ItemStack(guiToggle.material());
  }

  public GuiHandler handler() {
    return PartyChat.guiHandler();
  }
}