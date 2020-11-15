package me.majekdor.partychat.api;

import me.majekdor.partychat.data.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerPartyJoinEvent extends Event implements Cancellable {

    /*
    This event is fired when a player is joining a party. Not when the request to join is sent. Can be canceled.
     */

    private final Player player;
    private final Party party;
    private boolean isCancelled;

    public PlayerPartyJoinEvent(Player player, Party party) {
        this.player = player;
        this.party = party;
        this.isCancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
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
     * Returns the player involved in this event.
     * @return Player who is involved in this event.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Returns the party involved in this event.
     * @return Party that is involved in this event.
     */
    public Party getParty() {
        return this.party;
    }
}
