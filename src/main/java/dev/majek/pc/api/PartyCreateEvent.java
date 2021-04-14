package dev.majek.pc.api;

import dev.majek.pc.data.object.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PartyCreateEvent extends Event implements Cancellable {

    private boolean cancelled;
    private final Player creator;
    private final Party party;
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public PartyCreateEvent(Player creator, Party party) {
        this.creator = creator;
        this.party = party;
    }

    /**
     * Get the player who is creating the party.
     * @return Party creator.
     */
    public Player getCreator() {
        return creator;
    }

    /**
     * Get the party that is being created.
     * @return Party being created.
     */
    public Party getParty() {
        return party;
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
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
