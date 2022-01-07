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
package dev.majek.pc.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Represents a mechanic such as a {@link org.bukkit.event.player.PlayerQuitEvent} and
 * is used in classes that need to run code on plugin startup.
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
   * Runs when a player joins the server.
   *
   * @param player the joining player
   */
  public void onPlayerJoin(Player player) { }

  /**
   * Runs when a player quits the server.
   *
   * @param player the quitting player
   */
  public void onPlayerQuit(Player player) { }
}