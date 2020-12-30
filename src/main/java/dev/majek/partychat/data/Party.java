package dev.majek.partychat.data;

import dev.majek.partychat.PartyChat;
import dev.majek.partychat.util.Chat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class Party {

    public static Map<UUID, Party> partyMap = new HashMap<>();
    public static List<Player> noMove = new ArrayList<>();

    public String name;
    public List<UUID> members;
    public int size;
    public boolean isPublic;
    public UUID leader;
    public List<Player> pendingJoinRequests;
    public List<Player> pendingInvitations;
    public List<Player> blockedPlayers;
    public List<Player> pendingSummons;

    FileConfiguration c = PartyChat.getInstance().getConfig();

    public Party(Player leader, String partyName) {
        this.name = partyName;
        this.leader = leader.getUniqueId();
        this.members = new ArrayList<>();
        this.members.add(leader.getUniqueId());
        this.size = 1;
        this.isPublic = c.getBoolean("public-on-creation");
        this.pendingInvitations = new ArrayList<>();
        this.pendingJoinRequests = new ArrayList<>();
        this.blockedPlayers = new ArrayList<>();
        this.pendingSummons = new ArrayList<>();
    }

    public Party(String partyName, String leaderUUID, List<UUID> members, Integer size, Boolean isPublic) {
        this.name = partyName;
        this.leader = UUID.fromString(leaderUUID);
        this.members = new ArrayList<>(members);
        this.size = size;
        this.isPublic = isPublic;
        this.pendingInvitations = new ArrayList<>();
        this.pendingJoinRequests = new ArrayList<>();
        this.blockedPlayers = new ArrayList<>();
        this.pendingSummons = new ArrayList<>();
    }

    public static Party getParty(Player player) {
        return partyMap.get(player.getUniqueId());
    }

    public static Party getParty(UUID uuid) {
        return partyMap.get(uuid);
    }

    public static String getRawName(Party party) {
        return Chat.removeColorCodes(party.name);
    }

    public static void changeName(Party party, String name) {
        party.name = name;
    }

    public static boolean isLeader(Player player) {
        Party party = getParty(player.getUniqueId());
        if (party == null) return false;
        return party.leader == player.getUniqueId();
    }

    public static boolean isLeader(UUID uuid) {
        Party party = getParty(uuid);
        if (party == null) return false;
        return party.leader == uuid;
    }

    public static boolean inParty(Player player) {
        return partyMap.containsKey(player.getUniqueId());
    }

    public static boolean nameTaken(String name) {
        for (Party party : partyMap.values()) {
            if (party.name.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }
}
