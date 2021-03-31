package dev.majek.pc.data;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.data.storage.JSONConfig;
import dev.majek.pc.mechanic.Mechanic;
import dev.majek.pc.util.Utils;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PartyHandler extends Mechanic {

    private final Map<UUID, Party> partyMap;
    private final JSONConfig config;

    public PartyHandler() {
        partyMap = new HashMap<>();
        config = new JSONConfig(PartyChat.getCore().getDataFolder(), "parties");
        try {
            config.createConfig();
        } catch (FileNotFoundException e) {
            PartyChat.error("Unable to create parties.json storage file!");
            e.printStackTrace();
        }
    }

    @Override
    public void onStartup() {
        if (PartyChat.getDataHandler().persistentParties) {
            loadParties();
            PartyChat.log("Loaded " + partyMap.values().size() + " parties from JSON.");
        }
    }

    /**
     * Runs on plugin shutdown. Save parties if persistent parties is enabled.
     */
    @Override
    public void onShutdown() {
        // Update all saved parties if persistent parties is enabled
        if (PartyChat.getDataHandler().persistentParties) {
            for (Party party : partyMap.values())
                saveParty(party);
            PartyChat.log("Saved " + partyMap.values().size() + " parties to JSON.");
        }
    }

    public void loadParties() {
        JSONObject fileContents;
        try {
            fileContents = config.toJSONObject();
        } catch (IOException | ParseException e) {
            PartyChat.error("Critical error loading saved parties from parties.json");
            e.printStackTrace();
            return;
        }
        for (Object key : fileContents.keySet()) {
            JSONObject partyJSON = (JSONObject) fileContents.get(key);
            List<UUID> memberIDs = Utils.deserializeMembers(partyJSON.get("memberIDs").toString());
            List<User> members = memberIDs.stream().map(User::new).collect(Collectors.toList());
            Party party = new Party(
                    partyJSON.get("name").toString(),
                    partyJSON.get("leaderID").toString(),
                    members,
                    partyJSON.get("isPublic").toString().equals("true"),
                    partyJSON.get("friendlyFire").toString().equals("true")
            );
            partyMap.put(party.getId(), party);
            members.forEach(member -> {
                member.setPartyID(party.getId());
                PartyChat.getDataHandler().addToUserMap(member);
            });
        }
    }

    @SuppressWarnings("unchecked")
    public void saveParty(Party party) {
        JSONObject partyMeta = new JSONObject();
        partyMeta.put("name", party.getName());
        partyMeta.put("leaderID", party.getLeader().toString());
        partyMeta.put("memberIDs", Utils.serializeMembers(party.getMembers().stream()
                .map(User::getPlayerID).collect(Collectors.toList())));
        partyMeta.put("isPublic", String.valueOf(party.isPublic()));
        partyMeta.put("friendlyFire", String.valueOf(party.allowsFriendlyFire()));

        JSONObject partyJson = new JSONObject();
        partyJson.put(party.getRawName(), partyMeta);

        try {
            config.putInJSONObject(partyJson);
        } catch (IOException | ParseException e) {
            PartyChat.error("Unable to save party \"" + party.getRawName() + "\" to parties.json");
            e.printStackTrace();
        }
    }

    public void deleteParty(Party party) {
        party.getMembers().forEach(member -> {
            member.setInParty(false);
            member.setPartyID(null);
            member.setPartyChatToggle(false);
        });
        removeFromPartyMap(party.getId());
        try {
            config.removeFromJSONObject(party.getRawName());
        } catch (IOException | ParseException e) {
            PartyChat.error("Unable to remove party \"" + party.getRawName() + "\" from parties.json");
            e.printStackTrace();
        }
    }

    /**
     * Check if a certain party name is already taken.
     * @param name Name to check.
     * @return true -> taken | false -> free
     */
    public boolean isNameTaken(String name) {
        for (Party party : partyMap.values()) {
            if (party.getName().equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    @Nullable
    public Party getParty(User user) {
        return partyMap.get(user.getPartyID());
    }

    @Nullable
    public Party getParty(UUID uuid) {
        return partyMap.get(uuid);
    }

    @Nullable
    public Party getParty(Player player) {
        return partyMap.get(PartyChat.getDataHandler().getUser(player).getPartyID());
    }

    /**
     * Get main party map.
     */
    public Map<UUID, Party> getPartyMap() {
        return partyMap;
    }

    /**
     * Add a party id and the party to the party map.
     * @param uuid The party's unique id.
     * @param party The party to add.
     */
    public void addToPartyMap(UUID uuid, Party party) {
        partyMap.put(uuid, party);
    }

    /**
     * Remove a player from the party map.
     * @param uuid The unique id of the player to remove.
     */
    public void removeFromPartyMap(UUID uuid) {
        partyMap.remove(uuid);
    }
}
