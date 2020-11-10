package me.majekdor.partychat.api;

import me.majekdor.partychat.data.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PartyCreateEvent extends Event implements Cancellable {

    private final Player creator;
    private final Party party;
    private boolean isCancelled;

    public PartyCreateEvent(Player creator, Party party) {
        this.creator = creator;
        this.party = party;
        this.isCancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Returns the player who created the party.
     * @return Player who created the party.
     */
    public Player getCreator() {
        return this.creator;
    }

    /**
     * Returns the party that was created.
     * @return Party that was created.
     */
    public Party getParty() {
        return this.party;
    }

}
