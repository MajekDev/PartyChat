package dev.majek.pc.event;

import dev.majek.pc.data.object.Party;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PartyDeleteEvent extends Event implements Cancellable {

    private boolean cancelled;
    private final boolean empty;
    private final Party party;
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public PartyDeleteEvent(Party party) {
        this.empty = party.getSize() == 0;
        this.party = party;
    }

    /**
     * Whether or not the party is being deleted because it is empty. If the party is not empty then it is being
     * deleted because the leader disbanded it.
     * @return Whether or not the party is empty.
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * Get the party that is being deleted.
     * @return Party being deleted.
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
}
