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
package dev.majek.pc.api;

import dev.majek.pc.data.object.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the event fired when a player sends a message to party chat.
 */
public class PartyChatEvent extends Event implements Cancellable {

  private boolean cancelled;
  private final Player player;
  private final Party party;
  private String message;
  private static final HandlerList HANDLER_LIST = new HandlerList();

  /**
   * Fired when a player sends a message to party chat.
   *
   * @param player  The in-game player sending the message.
   * @param party   The party the message is being sent to.
   * @param message The message being sent to the party.
   */
  public PartyChatEvent(Player player, Party party, String message) {
    this.player = player;
    this.party = party;
    this.message = message;
  }

  /**
   * Get the player who is sending a message to party chat.
   *
   * @return Chatting player.
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * Get the party the player is sending the message to.
   *
   * @return Party player messaging.
   */
  public Party getParty() {
    return party;
  }

  /**
   * Get the message the player is sending to party chat.
   *
   * @return Player's message.
   */
  public String getMessage() {
    return message;
  }

  /**
   * Set the message the player is sending to party chat.
   *
   * @param message New message.
   */
  public void setMessage(String message) {
    this.message = message;
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