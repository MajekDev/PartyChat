package dev.majek.pc.api;

import dev.majek.pc.data.object.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PartyChatEvent extends Event implements Cancellable {

    private boolean cancelled;
    private final Player player;
    private final Party party;
    private String message;
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public PartyChatEvent(Player player, Party party, String message) {
        this.player = player;
        this.party = party;
        this.message = message;
    }

    /**
     * Get the player who is sending a message to party chat.
     * @return Chatting player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the party the player is sending the message to.
     * @return Party player messaging.
     */
    public Party getParty() {
        return party;
    }

    /**
     * Get the message the player is sending to party chat.
     * @return Player's message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the message the player is sending to party chat.
     * @param message New message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}