package dev.majek.pc.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * Implement this class instead of {@link Listener} for easier access to certain events.
 */
public interface Event extends Listener {

  /**
   * Runs when the plugin is being enabled.
   */
  default void onStartup() {

  }

  /**
   * Runs when the plugin is being shutdown.
   */
  default void onShutdown() {

  }

  /**
   * Runs when a player joins the server.
   *
   * @param player the joining player
   */
  @SuppressWarnings("unused")
  default void onPlayerJoin(@NotNull Player player) {

  }

  /**
   * Runs when a player quits the server.
   *
   * @param player the quitting player
   */
  @SuppressWarnings("unused")
  default void onPlayerQuit(@NotNull Player player) {

  }
}
