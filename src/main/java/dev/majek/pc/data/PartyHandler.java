/*
 * This file is part of PartyChat, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 Majekdor
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.majek.pc.data;

import com.google.gson.JsonObject;
import dev.majek.pc.PartyChat;
import dev.majek.pc.api.PartyChatEvent;
import dev.majek.pc.api.PartyDeleteEvent;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.data.storage.JsonConfig;
import dev.majek.pc.mechanic.Mechanic;
import dev.majek.pc.chat.ChatUtils;
import dev.majek.pc.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles party saving, loading, and storage.
 */
public class PartyHandler extends Mechanic {

  private final Map<UUID, Party> partyMap;
  private final JsonConfig config;

  public PartyHandler() {
    partyMap = new HashMap<>();
    config = new JsonConfig(PartyChat.core().getDataFolder(), "parties");
    try {
      config.createConfig();
    } catch (FileNotFoundException e) {
      PartyChat.error("Unable to create parties.json storage file!");
      e.printStackTrace();
    }
  }

  @Override
  public void onStartup() {
    if (PartyChat.dataHandler().persistentParties) {
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
    if (PartyChat.dataHandler().persistentParties) {
      for (Party party : partyMap.values())
        saveParty(party);
      PartyChat.log("Saved " + partyMap.values().size() + " parties to JSON.");
    }
  }

  /**
   * Load parties from JSON file storage.
   */
  public void loadParties() {
    JsonObject fileContents;
    try {
      fileContents = config.toJSONObject();
    } catch (IOException e) {
      PartyChat.error("Critical error loading saved parties from parties.json");
      e.printStackTrace();
      return;
    }
    for (String key : fileContents.keySet()) {
      JsonObject partyJSON = fileContents.get(key).getAsJsonObject();
      List<UUID> memberIDs = Utils.deserializeMembers(partyJSON.get("memberIDs").getAsString()).stream()
          .filter(uuid -> !uuid.toString().equals(partyJSON.get("leaderID").getAsString()))
          .collect(Collectors.toList());
      List<User> members = memberIDs.stream().map(User::new).collect(Collectors.toList());
      Party party = new Party(
          partyJSON.get("name").getAsString(),
          partyJSON.get("leaderID").getAsString(),
          members,
          partyJSON.get("isPublic").getAsBoolean(),
          partyJSON.get("friendlyFire").getAsBoolean()
      );
      partyMap.put(party.getId(), party);
      members.forEach(member -> {
        member.setPartyID(party.getId());
        PartyChat.dataHandler().addToUserMap(member);
      });
    }
  }

  /**
   * Save a party to the JSON file storage.
   * @param party Party so save.
   */
  public void saveParty(Party party) {
    JsonObject partyMeta = new JsonObject();
    partyMeta.addProperty("name", party.getName());
    partyMeta.addProperty("leaderID", party.getLeader().getPlayerID().toString());
    partyMeta.addProperty("memberIDs", Utils.serializeMembers(party.getMembers().stream()
        .map(User::getPlayerID).collect(Collectors.toList())));
    partyMeta.addProperty("isPublic", party.isPublic());
    partyMeta.addProperty("friendlyFire", party.allowsFriendlyFire());

    try {
      config.putInJsonObject(party.getRawName(), partyMeta);
    } catch (IOException e) {
      PartyChat.error("Unable to save party \"" + party.getRawName() + "\" to parties.json");
      e.printStackTrace();
    }
  }

  /**
   * Delete a party from the JSON file storage.
   * @param party Party to delete.
   */
  public void deleteParty(Party party) {
    PartyDeleteEvent event = new PartyDeleteEvent(party);
    PartyChat.core().getServer().getPluginManager().callEvent(event);
    if (event.isCancelled())
      return;
    party.getMembers().forEach(member -> {
      member.setInParty(false);
      member.setPartyID(null);
      member.setPartyChatToggle(false);
    });
    removeFromPartyMap(party.getId());
    try {
      config.removeFromJsonObject(party.getRawName());
    } catch (IOException e) {
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

  /**
   * Get a party from a {@link User}. May be null if the user is not in a party.
   * @param user User to get party from.
   * @return Party, if it exists.
   */
  @Nullable
  public Party getParty(User user) {
    return partyMap.get(user.getPartyID());
  }

  /**
   * Get a party from a party unique id. May be null if the party doesn't exist.
   * @param uuid Party's unique id.
   * @return Party, if it exists.
   */
  @Nullable
  public Party getParty(UUID uuid) {
    return partyMap.get(uuid);
  }

  /**
   * Get a party from a player. May be null if the player is not in a party.
   * @param player Player to get party from.
   * @return Party, if it exists.
   */
  @Nullable
  public Party getParty(Player player) {
    return partyMap.get(PartyChat.dataHandler().getUser(player).getPartyID());
  }

  /**
   * Get a list of all active parties.
   * @return All active parties.
   */
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

  /**
   * Send a message to a party. This will fire the {@link PartyChatEvent}.
   * @param party The party to send the message to.
   * @param sender The {@link User} sending the message.
   * @param message The message being sent.
   */
  public void sendMessageToPartyChat(Party party, User sender, String message) {

    // Run the event sync
    Bukkit.getScheduler().runTask(PartyChat.core(), () -> {
      // Run PartyChatEvent
      PartyChatEvent event = new PartyChatEvent(sender.getPlayer(), party, message);
      PartyChat.core().getServer().getPluginManager().callEvent(event);
      if (event.isCancelled())
        return;

      String finalMessage = event.getMessage();

      // Check for inappropriate words
      if (PartyChat.dataHandler().getConfigBoolean(PartyChat.dataHandler().mainConfig, "block-inappropriate-chat")) {
        if (Restrictions.containsCensoredWord(finalMessage)) {
          PartyChat.messageHandler().sendMessage(sender, "inappropriate-message");
          return;
        }
      }

      // This is used so staff don't get the message twice
      List<Player> messageReceived = new ArrayList<>();

      // Log message to console if that's enabled
      if (PartyChat.dataHandler().getConfigBoolean(PartyChat.dataHandler().mainConfig, "console-log"))
        PartyChat.messageHandler().sendMessageWithEverything(Bukkit.getConsoleSender(), "spy-format", "%partyName%",
                ChatUtils.removeColorCodes(party.getName()), "%player%", sender.getUsername(), finalMessage);

      // Send message to party members
      party.getMembers().stream().map(User::getPlayer).filter(Objects::nonNull).forEach(member -> {
        PartyChat.messageHandler().sendMessageWithEverything(member, "message-format", "%partyName%",
                party.getName(), "%player%", sender.getNickname(), finalMessage);
        messageReceived.add(member);
      });

      // Send message to server staff
      PartyChat.dataHandler().getUserMap().values().stream().filter(User::isSpyToggle).map(User::getPlayer)
              .filter(Objects::nonNull).filter(staff -> !messageReceived.contains(staff))
              .forEach(staff -> PartyChat.messageHandler().sendMessageWithEverything(staff, "spy-format",
                      "%partyName%", ChatUtils.removeColorCodes(party.getRawName()), "%player%",
                      sender.getUsername(), finalMessage));

      //   Log to discord if enabled
      if (PartyChat.dataHandler().getConfigBoolean(PartyChat.dataHandler().mainConfig, "log-to-discord")
              && PartyChat.jda() != null) {
        PartyChat.logToDiscord(ChatUtils.removeColorCodes(PartyChat.dataHandler().getConfigString(PartyChat
                .dataHandler().messages, "message-format").replace("%partyName%", party.getName())
                .replace("%player%", sender.getNickname()) + finalMessage
                .replace("_", "\\_").replace("*", "\\*")));
      }
    });
  }
}