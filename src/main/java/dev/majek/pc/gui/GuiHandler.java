package dev.majek.pc.gui;

import dev.majek.pc.mechanic.Mechanic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles registering active GUIs and managing inventory events.
 */
public class GuiHandler extends Mechanic {

    private final List<Gui> activeGuis;

    public GuiHandler() {
        this.activeGuis = new CopyOnWriteArrayList<>();
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
}
