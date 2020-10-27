package me.majekdor.partychat.data;

import me.majekdor.partychat.PartyChat;
import me.majekdor.partychat.util.Chat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Party {

    public static Map<Player, String> inParty = new HashMap<>();
    public static Map<String, Party> parties = new HashMap<>();
    public static List<Player> noMove = new ArrayList<>();

    public String name;
    public List<Player> members;
    public int size;
    public boolean isPublic;
    public Player leader;
    public List<Player> pendingJoinRequests;
    public List<Player> pendingInvitations;
    public List<Player> blockedPlayers;
    public List<Player> pendingSummons;

    FileConfiguration c = PartyChat.getInstance().getConfig();

    public Party(Player leader, String partyName) {
        this.name = partyName;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.members.add(leader);
        this.size = 1;
        this.isPublic = c.getBoolean("public-on-creation");
        this.pendingInvitations = new ArrayList<>();
        this.pendingJoinRequests = new ArrayList<>();
        this.blockedPlayers = new ArrayList<>();
        this.pendingSummons = new ArrayList<>();
    }

    public static Party getParty(Player player) {
        String partyName = inParty.get(player);
        return parties.get(partyName);
    }

    public static String getRawName(Party party) {
        return Chat.removeColorCodes(party.name);
    }

    public static void changeName(Party party, String name) {
        party.name = name;
    }

    public static boolean isLeader(Party party, Player player) {
        return party.leader == player;
    }
}
