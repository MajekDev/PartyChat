package me.majekdor.partychat.gui;

import me.majekdor.partychat.PartyChat;
import me.majekdor.partychat.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        PartyChat.getGuiHandler().registerActiveGui(this);
    }

    protected void setItem(int slot, Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + name + ChatColor.RESET);
        meta.setLore(Arrays.asList(lore));
        stack.setItemMeta(meta);
        inv.setItem(slot, stack);
    }

    protected void setLore(int slot, String... lore) {
        ItemStack stack = inv.getItem(slot);
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(Arrays.asList(lore));
        stack.setItemMeta(meta);
    }

    protected void addActionItem(int slot, Material material, String name, Runnable action, boolean rightClickOnly, String... lore) {
        setItem(slot, material, name, lore);
        clickActions.put(slot, new Pair<>(action == null ? NO_ACTION : action, rightClickOnly));
    }

    protected void addActionItem(int slot, Material material, String name, Runnable action, String... lore) {
        addActionItem(slot, material, name, action, false, lore);
    }

    protected void addLabel(int slot, Material material, String name, String... lore) {
        addActionItem(slot, material, name, NO_ACTION, lore);
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
            PartyChat.getGuiHandler().removeActiveGui(this);
        }
    }

    protected void onClose() { }

    protected static ItemStack clone(ItemStack stack) {
        return stack == null ? null : stack.clone();
    }

}
