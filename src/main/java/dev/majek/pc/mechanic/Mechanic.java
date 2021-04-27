package dev.majek.pc.mechanic;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Represents a mechanic such as a {@link org.bukkit.event.player.PlayerQuitEvent} and is used in classes that need
 * to run code on plugin startup.
 */
public class Mechanic implements Listener {
    /**
     * Runs when the plugin is being enabled.
     */
    public void onStartup() { }

    /**
     * Runs when the plugin is being shutdown.
     */
    public void onShutdown() { }

    /**
     * Runs when the a player joins the server.
     */
    public void onPlayerJoin(Player player) { }

    /**
     * Runs when the a player quits the server.
     */
    public void onPlayerQuit(Player player) { }
}
