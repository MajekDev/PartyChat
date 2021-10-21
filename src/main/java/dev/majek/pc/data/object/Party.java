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
package dev.majek.pc.data.object;

import dev.majek.pc.PartyChat;
import dev.majek.pc.chat.ChatUtils;
import dev.majek.pc.util.Pair;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A party. Storing things such as members, the leader, and party config options.
 */
public class Party {

  private String                              name;
  private final UUID                          id;
  private boolean                             isPublic;
  private boolean                             friendlyFire;
  private User                                leader;
  private final List<User>                    members;
  private final List<Player>                  pendingJoinRequests;
  private final List<Pair<Player, Player>>    pendingInvitations;
  private final List<Player>                  blockedPlayers;
  private final List<Player>                  pendingSummons;

  /**
   * Constructed when an online player creates a new party.
   * @param leader The online player creating the party.
   * @param partyName The provided party name.
   */
  public Party(Player leader, String partyName) {
    this.name = partyName;
    this.id = UUID.randomUUID();
    this.leader = new User(leader);
    this.members = new CopyOnWriteArrayList<>();
    this.members.add(PartyChat.dataHandler().getUser(leader));
    this.isPublic = PartyChat.dataHandler().getConfigBoolean(
        PartyChat.dataHandler().mainConfig, "public-on-creation");
    this.friendlyFire = PartyChat.dataHandler().getConfigBoolean(
        PartyChat.dataHandler().mainConfig, "default-friendly-fire");
    this.pendingInvitations = new CopyOnWriteArrayList<>();
    this.pendingJoinRequests = new CopyOnWriteArrayList<>();
    this.blockedPlayers = new CopyOnWriteArrayList<>();
    this.pendingSummons = new CopyOnWriteArrayList<>();
  }

  /**
   * Constructed when a party is recreated after restart.
   * @param partyName The name of the party.
   * @param leaderUUID The leader of the party's unique id.
   * @param members The list of party members.
   * @param isPublic Whether the party is public.
   * @param friendlyFire Whether the party allows friendly fire.
   */
  public Party(String partyName, String leaderUUID, List<User> members, Boolean isPublic, Boolean friendlyFire) {
    this.name = partyName;
    this.id = UUID.randomUUID();
    User leader = new User(UUID.fromString(leaderUUID));
    this.leader = leader;
    leader.setPartyID(this.id);
    PartyChat.dataHandler().addToUserMap(leader);
    if (members == null)
      this.members = new CopyOnWriteArrayList<>();
    else
      this.members = new CopyOnWriteArrayList<>(members);
    this.members.add(leader);
    this.isPublic = isPublic;
    this.friendlyFire = friendlyFire;
    this.pendingInvitations = new CopyOnWriteArrayList<>();
    this.pendingJoinRequests = new CopyOnWriteArrayList<>();
    this.blockedPlayers = new CopyOnWriteArrayList<>();
    this.pendingSummons = new CopyOnWriteArrayList<>();
  }

  /**
   * Get the party's name.
   * @return Party name.
   */
  public String getName() {
    return name;
  }

  /**
   * Get the party's name without color codes.
   * @return Raw party name.
   */
  public String getRawName() {
    return ChatUtils.removeColorCodes(name);
  }

  /**
   * Set the party's name.
   * @param name New name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the party's unique id.
   * @return Party id.
   */
  public UUID getId() {
    return id;
  }

  /**
   * Get a list of the party member's unique ids.
   * @return List of members.
   */
  public List<User> getMembers() {
    return members;
  }

  /**
   * Add a new party member.
   * @param user New user.
   */
  public void addMember(User user) {
    members.add(user);
  }

  /**
   * Remove a party member.
   * @param user User to remove.
   */
  public void removeMember(User user) {
    members.remove(user);
  }

  /**
   * Get the size of the party.
   * @return Party size.
   */
  public int getSize() {
    return members.size();
  }

  /**
   * Get party status: public or private.
   * @return true if public | false if private
   */
  public boolean isPublic() {
    return isPublic;
  }

  /**
   * Set the public status.
   * @param isPublic Whether the party should be public.
   */
  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  /**
   * Get whether the party allows friendly fire between members.
   * @return true if allows | false if denies
   */
  public boolean allowsFriendlyFire() {
    return friendlyFire;
  }

  /**
   * Set the party's friendly fire toggle.
   * @param friendlyFire Whether the party should allow friendly fire.
   */
  public void setFriendlyFire(boolean friendlyFire) {
    this.friendlyFire = friendlyFire;
  }

  /**
   * Get the unique id of the party leader.
   * @return Party leader's unique id.
   */
  public User getLeader() {
    return leader;
  }

  /**
   * Set a new party leader.
   * @param leader New leader's unique id.
   */
  public void setLeader(User leader) {
    this.leader = leader;
  }

  /**
   * Get a list of players who have pending join requests.
   * These are players who have requested to join the party.
   * @return List of players.
   */
  public List<Player> getPendingJoinRequests() {
    return pendingJoinRequests;
  }

  /**
   * Add a player to the list of pending join requests.
   * @param player Player to add.
   */
  public void addPendingJoinRequest(Player player) {
    pendingJoinRequests.add(player);
  }

  /**
   * Remove a player from the list of pending join requests.
   * @param player Player to remove.
   */
  public void removePendingJoinRequest(Player player) {
    pendingJoinRequests.remove(player);
  }

  /**
   * Get a list of players who have pending invitations.
   * These are players who have been invited to join the party.
   * @return List of players.
   */
  public List<Pair<Player, Player>> getPendingInvitations() {
    return pendingInvitations;
  }

  /**
   * Add a player to the list of pending invitations.
   * @param invited Player to add.
   * @param inviter The player inviting the other player.
   */
  public void addPendingInvitation(Player invited, Player inviter) {
    pendingInvitations.add(new Pair<>(invited, inviter));
  }

  /**
   * Remove a player from the list of pending invitations.
   * @param player Player to remove.
   */
  public void removePendingInvitation(Player player) {
    pendingInvitations.removeIf(toRemove -> toRemove.getFirst() == player);
  }

  /**
   * Get a list of players who are temporarily blocked from requesting to join the party.
   * @return List of players.
   */
  public List<Player> getBlockedPlayers() {
    return blockedPlayers;
  }

  /**
   * Add a player to the list of temporarily blocked players.
   * @param player Player to add.
   */
  public void addBlockedPlayer(Player player) {
    blockedPlayers.add(player);
  }

  /**
   * Remove a player from the list of temporarily blocked players.
   * @param player Player to remove.
   */
  public void removeBlockedPlayer(Player player) {
    blockedPlayers.remove(player);
  }

  /**
   * Get a list of players who have pending summon requests from the party leader.
   * @return List of players.
   */
  public List<Player> getPendingSummons() {
    return pendingSummons;
  }

  /**
   * Add a player to the list of players with pending summon requests.
   * @param player Player to add.
   */
  public void addPendingSummons(Player player) {
    pendingSummons.add(player);
  }

  /**
   * Remove a player from the list of players with pending summon requests.
   * @param player Player to remove.
   */
  public void removePendingSummons(Player player) {
    pendingSummons.remove(player);
  }
}