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
import dev.majek.pc.mechanic.Mechanic;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles registering active GUIs and managing inventory events.
 */
public class GuiHandler extends Mechanic {

  private final List<Gui> activeGuis;
  private final Map<String, GuiToggle> guiToggles;

  public GuiHandler() {
    this.activeGuis = new CopyOnWriteArrayList<>();
    this.guiToggles = new HashMap<>();
  }

  /**
   * Add a {@link Gui} to the list of active GUIs.
   * @param gui The GUI to add.
   */
  public void registerActiveGui(Gui gui) {
    activeGuis.add(gui);
  }

  /**
   * Remove a {@link Gui} from the list of active GUIs.
   * @param gui The GUI to remove.
   */
  public void removeActiveGui(Gui gui) {
    activeGuis.remove(gui);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    activeGuis.stream().filter(gui -> gui.matches(event)).forEach(gui -> gui.onItemClick(event));
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    activeGuis.stream().filter(gui -> gui.matches(event)).forEach(Gui::onInventoryClosed);
  }

  public void registerToggles() {
    guiToggles.clear();
    registerToggle(new GuiToggle("view-members", getConfigInteger("view-members.hdb-id"),
        Material.getMaterial(getConfigString("view-members.material")),
        Material.BOOK, getConfigBoolean("view-members.visible")));
    registerToggle(new GuiToggle("invite-player", getConfigInteger("invite-player.hdb-id"),
        Material.getMaterial(getConfigString("invite-player.material")),
        Material.NETHER_STAR, getConfigBoolean("invite-player.visible")));
    registerToggle(new GuiToggle("leave-party", getConfigInteger("leave-party.hdb-id"),
        Material.getMaterial(getConfigString("leave-party.material")),
        Material.RED_STAINED_GLASS_PANE, getConfigBoolean("leave-party.visible")));
    registerToggle(new GuiToggle("close-gui", getConfigInteger("close-gui.hdb-id"),
        Material.getMaterial(getConfigString("close-gui.material")), Material.BARRIER, true));
    registerToggle(new GuiToggle("summon-party", getConfigInteger("summon-party.hdb-id"),
        Material.getMaterial(getConfigString("summon-party.material")),
        Material.ENDER_PEARL, getConfigBoolean("summon-party.visible")));
    registerToggle(new GuiToggle("rename-party", getConfigInteger("rename-party.hdb-id"),
        Material.getMaterial(getConfigString("rename-party.material")),
        Material.OAK_SIGN, getConfigBoolean("rename-party.visible")));
    registerToggle(new GuiToggle("public-party", getConfigInteger("public-private.hdb-id-public"),
        Material.getMaterial(getConfigString("public-private.material-public")),
        Material.GREEN_CONCRETE, getConfigBoolean("public-private.visible")));
    registerToggle(new GuiToggle("private-party", getConfigInteger("public-private.hdb-id-private"),
        Material.getMaterial(getConfigString("public-private.material-private")),
        Material.RED_CONCRETE, getConfigBoolean("public-private.visible")));
    registerToggle(new GuiToggle("disband-party", getConfigInteger("disband-party.hdb-id"),
        Material.getMaterial(getConfigString("disband-party.material")),
        Material.TNT, getConfigBoolean("disband-party.visible")));
    registerToggle(new GuiToggle("next-page", getConfigInteger("next-page.hdb-id"),
        Material.getMaterial(getConfigString("next-page.material")), Material.EMERALD_BLOCK, true));
    registerToggle(new GuiToggle("previous-page", getConfigInteger("previous-page.hdb-id"),
        Material.getMaterial(getConfigString("previous-page.material")), Material.REDSTONE_BLOCK, true));
    registerToggle(new GuiToggle("promote-player", getConfigInteger("promote-player.hdb-id"),
        Material.getMaterial(getConfigString("promote-player.material")),
        Material.NETHER_STAR, getConfigBoolean("promote-player.visible")));
    registerToggle(new GuiToggle("remove-player", getConfigInteger("remove-player.hdb-id"),
        Material.getMaterial(getConfigString("remove-player.material")),
        Material.IRON_AXE, getConfigBoolean("remove-player.visible")));
    registerToggle(new GuiToggle("create-party", getConfigInteger("create-party.hdb-id"),
        Material.getMaterial(getConfigString("create-party.material")),
        Material.NETHER_STAR, getConfigBoolean("create-party.visible")));
    registerToggle(new GuiToggle("no-parties", getConfigInteger("no-parties.hdb-id"),
        Material.getMaterial(getConfigString("no-parties.material")),
        Material.RED_CONCRETE, getConfigBoolean("no-parties.visible")));
    registerToggle(new GuiToggle("join-party", getConfigInteger("join-party.hdb-id"),
        Material.getMaterial(getConfigString("join-party.material")),
        Material.GREEN_CONCRETE, getConfigBoolean("join-party.visible")));
  }

  public void registerToggle(GuiToggle guiToggle) {
    guiToggles.put(guiToggle.name(), guiToggle);
  }

  public GuiToggle getToggle(String name) {
    return guiToggles.get(name);
  }

  private String getConfigString(String path) {
    return PartyChat.dataHandler().getConfigString(PartyChat.dataHandler().guiConfig, path);
  }

  private int getConfigInteger(String path) {
    return PartyChat.dataHandler().getConfigInt(PartyChat.dataHandler().guiConfig, path);
  }

  private boolean getConfigBoolean(String path) {
    return PartyChat.dataHandler().getConfigBoolean(PartyChat.dataHandler().guiConfig, path);
  }
}