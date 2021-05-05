package dev.majek.pc.api;

import dev.majek.pc.data.object.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Not working yet.
 */
@Deprecated
public class PartyLeaveEvent extends Event implements Cancellable {

    private boolean cancelled;
    private final Player player;
    private final Party party;
    private final boolean isLeader;
    private Player newLeader;
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public PartyLeaveEvent(Player player, Party party, Player newLeader) {
        this.player = player;
        this.party = party;
        this.isLeader = party.getLeader().getPlayerID().equals(player.getUniqueId());
        this.newLeader = newLeader;
    }

    /**
     * Get the player who is leaving the party.
     * @return Leaving player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the party the player is leaving.
     * @return Party player is leaving.
     */
    public Party getParty() {
        return party;
    }

    /**
     * Whether or not the player leaving the party is currently the leader.
     * @return Whether or not player is leader.
     */
    public boolean isLeader() {
        return isLeader;
    }

    /**
     * The randomly chosen player who will be the new party leader. Can be null if the leader is the only
     * player in the party or the player leaving is not the leader.
     * @return The new party leader.
     */
    @Nullable
    public Player getNewLeader() {
        return newLeader;
    }

    /**
     * Set a different player to be the new party leader.
     * @param player New leader.
     */
    public void setNewLeader(Player player) {
        newLeader = player;
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
