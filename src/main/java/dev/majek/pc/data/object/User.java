package dev.majek.pc.data.object;

import dev.majek.pc.PartyChat;
import dev.majek.pc.mechanic.Mechanic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nullable;
import java.util.UUID;

public class User extends Mechanic {

    private UUID      playerID;
    private Player    player;
    private String    username;
    private boolean   isOnline;
    private UUID      partyID;
    private boolean   inParty;
    private boolean   partyChatToggle;
    private boolean   isStaff;
    private boolean   spyToggle;
    private boolean   noMove;

    public User() {}

    /**
     * Constructed when a player joins and isn't already in memory.
     * @param player The player who joins.
     */
    public User(Player player) {
        this.playerID = player.getUniqueId();
        this.player = player;
        this.username = player.getName();
        this.isOnline = true;
        this.partyID = null;
        this.inParty = false;
        this.partyChatToggle = false;
        this.isStaff = player.hasPermission("partychat.admin");
        this.spyToggle = player.hasPermission("partychat.admin") && PartyChat.getDataHandler()
                .getConfigBoolean(PartyChat.getDataHandler().mainConfig, "auto-spy");
        this.noMove = false;
        PartyChat.getDataHandler().addToUserMap(this);
    }

    /**
     * Constructed when the server restarts and saved parties are pulled from JSON.
     * @param uuid The player's unique id.
     */
    public User(UUID uuid) {
        this.playerID = uuid;
        this.player = null;
        this.username = Bukkit.getOfflinePlayer(uuid).getName();
        this.isOnline = false;
        this.partyID = null;
        this.inParty = true;
        this.partyChatToggle = false;
        this.isStaff = false;
        this.spyToggle = false;
        this.noMove = false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!PartyChat.getDataHandler().getUserMap().containsKey(event.getPlayer().getUniqueId())) {
            PartyChat.getDataHandler().addToUserMap(new User(event.getPlayer()));
            return;
        }
        User user = PartyChat.getDataHandler().getUser(event.getPlayer());
        user.setPlayer(event.getPlayer());
        user.setStaff(event.getPlayer().hasPermission("partychat.admin"));
        user.setSpyToggle(event.getPlayer().hasPermission("partychat.admin") && PartyChat.getDataHandler()
                .getConfigBoolean(PartyChat.getDataHandler().mainConfig, "auto-spy"));
        user.setNoMove(false);
        PartyChat.getDataHandler().addToUserMap(user);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        User user = PartyChat.getDataHandler().getUser(event.getPlayer());
        user.setPlayer(null);
        user.setOnline(false);
        PartyChat.getDataHandler().addToUserMap(user);
    }

    public UUID getPlayerID() {
        return playerID;
    }

    public void setPlayerID(UUID playerID) {
        this.playerID = playerID;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getUsername() {
        return username;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        this.isOnline = online;
    }

    @Nullable
    public UUID getPartyID() {
        return partyID;
    }

    public void setPartyID(UUID partyID) {
        this.partyID = partyID;
    }

    public boolean isInParty() {
        return inParty;
    }

    public void setInParty(boolean inParty) {
        this.inParty = inParty;
    }

    public boolean partyChatToggle() {
        return partyChatToggle;
    }

    public void setPartyChatToggle(boolean toggle) {
        this.partyChatToggle = toggle;
    }

    public void flipPartyChatToggle() {
        this.partyChatToggle = !this.partyChatToggle;
    }

    public boolean isStaff() {
        return isStaff;
    }

    public void setStaff(boolean staff) {
        this.isStaff = staff;
    }

    public boolean isSpyToggle() {
        return spyToggle;
    }

    public void setSpyToggle(boolean spyToggle) {
        this.spyToggle = spyToggle;
    }

    public void flipSpyToggle() {
        this.spyToggle = !this.spyToggle;
    }

    public boolean isNoMove() {
        return noMove;
    }

    public void setNoMove(boolean noMove) {
        this.noMove = noMove;
    }

    public boolean isLeader() {
        if (!isInParty() || getPartyID() == null)
            return false;
        Party party = PartyChat.getPartyHandler().getParty(getPartyID());
        if (party == null)
            return false;
        return party.getLeader() == getPlayerID();
    }

    @Nullable
    public Party getParty() {
        if (getPartyID() == null || !isInParty())
            return null;
        else
            return PartyChat.getPartyHandler().getParty(getPartyID());
    }
}
