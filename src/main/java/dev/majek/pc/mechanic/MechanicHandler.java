package dev.majek.pc.mechanic;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.object.User;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles all game mechanics and registers it's sub classes.
 */
public class MechanicHandler implements Listener {

    private final List<Mechanic> mechanics;

    public MechanicHandler() {
        this.mechanics = new ArrayList<>();
    }

    public void registerMechanics() {
        Bukkit.getPluginManager().registerEvents(this, PartyChat.getCore());

        // Handlers
        registerMechanic(PartyChat.getDataHandler());
        registerMechanic(PartyChat.getGuiHandler());
        registerMechanic(PartyChat.getLanguageHandler());
        registerMechanic(PartyChat.getCommandHandler());
        registerMechanic(PartyChat.getPartyHandler());

        // Feature mechanics
        registerMechanic(new User());
        registerMechanic(new PvPEvent());
        registerMechanic(new PlayerChat());
        registerMechanic(new PlayerMove());
        registerMechanic(new PlayerQuit());

        PartyChat.log("Finished registering mechanics.");
    }

    private void registerMechanic(Mechanic mechanic) {
        mechanics.add(mechanic);
        Bukkit.getPluginManager().registerEvents(mechanic, PartyChat.getCore());
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (PartyChat.class.equals(event.getPlugin().getClass()))
            mechanics.forEach(Mechanic::onStartup);
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (PartyChat.class.equals(event.getPlugin().getClass()))
            mechanics.forEach(Mechanic::onShutdown);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        mechanics.forEach(mechanic -> mechanic.onPlayerJoin(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        mechanics.forEach(mechanic -> mechanic.onPlayerQuit(event.getPlayer()));
    }
}
