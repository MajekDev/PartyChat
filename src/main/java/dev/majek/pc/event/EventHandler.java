package dev.majek.pc.event;

import dev.majek.pc.PartyChat;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles plugin {@link Event}s.
 */
public class EventHandler implements Listener {

  private final Set<Event> events;

  public EventHandler() {
    this.events = new HashSet<>();
  }

  /**
   * Register plugin events.
   */
  public void registerEvents() {
    PartyChat.core().getServer().getPluginManager().registerEvents(this, PartyChat.core());

    // Handlers
    //registerEvent(PartyChat.dataHandler());
    //registerEvent(PartyChat.guiHandler());
    //registerEvent(PartyChat.languageHandler());
    //registerEvent(PartyChat.commandHandler());
    //registerEvent(PartyChat.partyHandler());

    // Events
    //registerEvent(new User());
    //registerEvent(new PvPEvent());
    //registerEvent(new PlayerChat());
    //registerEvent(new PlayerMove());
    //registerEvent(new PlayerQuit());

    PartyChat.log("Finished registering events.");
  }

  /**
   * Register a plugin event.
   *
   * @param event the event
   */
  public void registerEvent(@NotNull Event event) {
    events.add(event);
    PartyChat.core().getServer().getPluginManager().registerEvents(event, PartyChat.core());
  }

  /**
   * Fire startup events.
   *
   * @param event the enable event
   */
  @org.bukkit.event.EventHandler
  public void onPluginEnable(@NotNull PluginEnableEvent event) {
    if (PartyChat.class.equals(event.getPlugin().getClass()))
      this.events.forEach(Event::onStartup);
  }

  /**
   * Fire shutdown events.
   *
   * @param event the disable event
   */
  @org.bukkit.event.EventHandler
  public void onPluginDisable(@NotNull PluginDisableEvent event) {
    if (PartyChat.class.equals(event.getPlugin().getClass()))
      this.events.forEach(Event::onShutdown);
  }

  /**
   * Fire player join events.
   *
   * @param event the join event
   */
  @org.bukkit.event.EventHandler
  public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
    this.events.forEach(e -> e.onPlayerJoin(event.getPlayer()));
  }

  /**
   * Fire player quit events.
   *
   * @param event the quit event
   */
  @org.bukkit.event.EventHandler
  public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
    this.events.forEach(e -> e.onPlayerQuit(event.getPlayer()));
  }
}
