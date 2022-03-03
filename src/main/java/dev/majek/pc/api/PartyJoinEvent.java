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
package dev.majek.pc.api;

import dev.majek.pc.data.object.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the event fired when a player joins a party.
 */
public class PartyJoinEvent extends Event implements Cancellable {

  private boolean cancelled;
  private final Player player;
  private final Party party;
  private static final HandlerList HANDLER_LIST = new HandlerList();

  /**
   * Fires when an in-game player joins an existing party.
   *
   * @param player  The player joining the party.
   * @param party   The party the player is joining.
   */
  public PartyJoinEvent(Player player, Party party) {
    this.player = player;
    this.party = party;
  }

  /**
   * Get the player who is joining the party.
   *
   * @return Joining player.
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * Get the party the player is joining.
   *
   * @return Party player is joining.
   */
  public Party getParty() {
    return party;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCancelled(boolean cancel) {
    cancelled = cancel;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }
}