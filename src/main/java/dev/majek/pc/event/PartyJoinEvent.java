package dev.majek.pc.event;

import dev.majek.pc.data.object.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PartyJoinEvent extends Event implements Cancellable {

    private boolean cancelled;
    private final Player player;
    private final Party party;
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public PartyJoinEvent(Player player, Party party) {
        this.player = player;
        this.party = party;
    }

    /**
     * Get the player who is joining the party.
     * @return Joining player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the party the player is joining.
     * @return Party player is joining.
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
