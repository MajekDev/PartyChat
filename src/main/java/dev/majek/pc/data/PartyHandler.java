package dev.majek.pc.data;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.data.storage.JSONConfig;
import dev.majek.pc.mechanic.Mechanic;
import dev.majek.pc.util.Chat;
import dev.majek.pc.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static dev.majek.pc.command.PartyCommand.sendMessage;
import static dev.majek.pc.command.PartyCommand.sendMessageWithEverything;

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
            List<UUID> memberIDs = Utils.deserializeMembers(partyJSON.get("memberIDs").toString()).stream()
                    .filter(uuid -> !uuid.toString().equals(partyJSON.get("leaderID").toString())).collect(Collectors.toList());
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
        partyMeta.put("leaderID", party.getLeader().getPlayerID().toString());
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

    public List<Party> getParties() {
        return new ArrayList<>(partyMap.values());
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

    public void sendMessageToPartyChat(Party party, User sender, String message) {

        // Check for inappropriate words
        if (PartyChat.getDataHandler().getConfigBoolean(PartyChat.getDataHandler().mainConfig, "block-inappropriate-chat")) {
            if (Restrictions.containsCensoredWord(message)) {
                sendMessage(sender, "inappropriate-message");
                return;
            }
        }

        // This is used so staff don't get the message twice
        List<Player> messageReceived = new ArrayList<>();

        // Log message to console if that's enabled
        if (PartyChat.getDataHandler().getConfigBoolean(PartyChat.getDataHandler().mainConfig, "console-log"))
            sendMessageWithEverything(Bukkit.getConsoleSender(), "spy-format", "%partyName%",
                    Chat.removeColorCodes(party.getName()), "%player%", sender.getUsername(), message);

        // Send message to party members
        party.getMembers().stream().map(User::getPlayer).filter(Objects::nonNull).forEach(member -> {
            sendMessageWithEverything(member, "message-format", "%partyName%",
                    party.getName(), "%player%", sender.getNickname(), message);
            messageReceived.add(member);
        });

        // Send message to server staff
        PartyChat.getDataHandler().getUserMap().values().stream().filter(User::isSpyToggle).map(User::getPlayer)
                .filter(Objects::nonNull).filter(staff -> !messageReceived.contains(staff))
                .forEach(staff -> sendMessageWithEverything(staff, "spy-format",
                        "%partyName%", Chat.removeColorCodes(party.getRawName()), "%player%",
                        sender.getUsername(), message));

        if (PartyChat.getDataHandler().getConfigBoolean(PartyChat.getDataHandler().mainConfig, "log-to-discord")) {
            PartyChat.logToDiscord(Chat.removeColorCodes(PartyChat.getDataHandler().getConfigString(PartyChat
                    .getDataHandler().messages, "message-format").replace("%partyName%", party.getName())
                    .replace("%player%", sender.getNickname()) + message));
        }
    }
}
