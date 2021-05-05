package dev.majek.pc.api;

import dev.majek.pc.command.party.PartyRename;
import dev.majek.pc.data.object.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PartyRenameEvent extends Event implements Cancellable {

    private boolean cancelled;
    private final Player player;
    private final Party party;
    private String newName;
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public PartyRenameEvent(Player player, Party party, String newName) {
        this.player = player;
        this.party = party;
        this.newName = newName;
    }

    /**
     * Get the player who is renaming the party.
     * @return Player renaming the party.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the party that is being renamed.
     * @return Party being renamed.
     */
    public Party getParty() {
        return party;
    }

    /**
     * Get the party's previous name. This will return the formatted name including color codes.
     * @return Party's previous name.
     */
    public String getOldName() {
        return party.getName();
    }

    /**
     * Get the party's new name. This will return the formatted name including color codes.
     * @return Party's new name.
     */
    public String getNewName() {
        return newName;
    }

    /**
     * Set the new name of the party. This may include color codes.
     * @param newName The party's new name.
     */
    public void setNewName(String newName) {
        this.newName = newName;
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
