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
 * Handles all game mechanics and registers it's sub classes.
 */
public class MechanicHandler implements Listener {

  private final List<Mechanic> mechanics;

  public MechanicHandler() {
    this.mechanics = new ArrayList<>();
  }

  public void registerMechanics() {
    Bukkit.getPluginManager().registerEvents(this, PartyChat.core());

    // Handlers
    registerMechanic(PartyChat.dataHandler());
    registerMechanic(PartyChat.guiHandler());
    registerMechanic(PartyChat.languageHandler());
    registerMechanic(PartyChat.commandHandler());
    registerMechanic(PartyChat.partyHandler());

    // Feature mechanics
    registerMechanic(new User());
    registerMechanic(new PvPEvent());
    registerMechanic(new PlayerChat());
    registerMechanic(new PlayerMove());
    registerMechanic(new PlayerQuit());

    PartyChat.log("Finished registering mechanics.");
  }

  public void registerMechanic(Mechanic mechanic) {
    mechanics.add(mechanic);
    Bukkit.getPluginManager().registerEvents(mechanic, PartyChat.core());
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