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
import dev.majek.pc.data.object.User;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles the event fired when a player leaves a party.
 */
public class PartyLeaveEvent extends Event implements Cancellable {

  private boolean cancelled;
  private final OfflinePlayer player;
  private final Party party;
  private final boolean isLeader;
  private User newLeader;
  private static final HandlerList HANDLER_LIST = new HandlerList();

  /**
   * Fires when an in-game player leaves a party.
   *
   * @param player    The in-game player leaving the party.
   * @param party     The party the player is leaving.
   * @param newLeader The new leader of the party if there is one.
   */
  public PartyLeaveEvent(OfflinePlayer player, Party party, @Nullable User newLeader) {
    this.player = player;
    this.party = party;
    this.isLeader = party.getLeader().getPlayerID().equals(player.getUniqueId());
    this.newLeader = newLeader;
  }

  /**
   * Get the player who is leaving the party.
   *
   * @return Leaving player.
   */
  public OfflinePlayer getPlayer() {
    return player;
  }

  /**
   * Get the party the player is leaving.
   *
   * @return Party player is leaving.
   */
  public Party getParty() {
    return party;
  }

  /**
   * Whether or not the player leaving the party is currently the leader.
   *
   * @return Whether or not player is leader.
   */
  public boolean isLeader() {
    return isLeader;
  }

  /**
   * The randomly chosen player who will be the new party leader. Can be null
   * if the leader is the only player in the party or the player leaving is not the leader.
   *
   * @return The new party leader.
   */
  @Nullable
  public User getNewLeader() {
    return newLeader;
  }

  /**
   * Set a different player to be the new party leader.
   *
   * @param player New leader.
   */
  public void setNewLeader(User player) {
    newLeader = player;
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