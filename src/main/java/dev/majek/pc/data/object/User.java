/*
 * This file is part of PartyChat, licensed under the MIT License.
 *
 * Copyright (c) 2020-2022 Majekdor
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
import dev.majek.pc.hooks.Vault;
import dev.majek.pc.mechanic.Mechanic;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A PartyChat user. Constructed when a player joins or is loaded from party JSON storage.
 */
public class User extends Mechanic {

  private UUID      playerID;
  private Player    player;
  private String    username;
  private String    nickname;
  private boolean   isOnline;
  private UUID      partyID;
  private boolean   inParty;
  private boolean   partyChatToggle;
  private boolean   isStaff;
  private boolean   spyToggle;
  private boolean   noMove;
  private boolean   partyOnly;
  private boolean   chatInputCreate;
  private boolean   chatInputInvite;
  private boolean   chatInputRename;
  private boolean   chatInputLeave;

  public User() {}

  /**
   * Constructed when a player joins and isn't already in memory.
   * @param player The player who joins.
   */
  public User(Player player) {
    this.playerID = player.getUniqueId();
    this.player = player;
    this.username = player.getName();
    this.nickname = (PartyChat.dataHandler().useVault ? Vault.getPlayerDisplayName(player)
        : PartyChat.dataHandler().useDisplayNames ? player.getDisplayName() : player.getName());
    this.isOnline = true;
    this.partyID = null;
    this.inParty = false;
    this.partyChatToggle = false;
    this.isStaff = player.hasPermission("partychat.admin");
    this.spyToggle = player.hasPermission("partychat.admin") && PartyChat.dataHandler()
        .getConfigBoolean(PartyChat.dataHandler().mainConfig, "auto-spy");
    this.noMove = false;
    this.partyOnly = PartyChat.dataHandler().getConfigBoolean(PartyChat.dataHandler().mainConfig, "party-only-in-party");
    PartyChat.dataHandler().addToUserMap(this);
    this.chatInputCreate = false;
    this.chatInputInvite = false;
    this.chatInputRename = false;
    this.chatInputLeave = false;
  }

  /**
   * Constructed when the server restarts and saved parties are pulled from JSON.
   * @param uuid The player's unique id.
   */
  public User(UUID uuid) {
    this.playerID = uuid;
    this.player = null;
    this.username = Bukkit.getOfflinePlayer(uuid).getName();
    this.nickname = null;
    this.isOnline = false;
    this.partyID = null;
    this.inParty = true;
    this.partyChatToggle = false;
    this.isStaff = false;
    this.spyToggle = false;
    this.noMove = false;
    this.partyOnly = false;
    this.chatInputCreate = false;
    this.chatInputInvite = false;
    this.chatInputRename = false;
    this.chatInputLeave = false;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (!PartyChat.dataHandler().getUserMap().containsKey(event.getPlayer().getUniqueId())) {
      PartyChat.dataHandler().addToUserMap(new User(event.getPlayer()));
      return;
    }
    User user = PartyChat.dataHandler().getUser(event.getPlayer());
    user.setNickname((PartyChat.dataHandler().useVault ? Vault.getPlayerDisplayName(event.getPlayer())
        : PartyChat.dataHandler().useDisplayNames ? event.getPlayer().getDisplayName() : event.getPlayer().getName()) + "");
    user.setPlayer(event.getPlayer());
    user.setStaff(event.getPlayer().hasPermission("partychat.admin"));
    user.setSpyToggle(event.getPlayer().hasPermission("partychat.admin") && PartyChat.dataHandler()
        .getConfigBoolean(PartyChat.dataHandler().mainConfig, "auto-spy"));
    user.setNoMove(false);
    user.setOnline(true);
    user.setPartyOnly(PartyChat.dataHandler().getConfigBoolean(PartyChat.dataHandler().mainConfig, "party-only-in-party"));
    PartyChat.dataHandler().addToUserMap(user);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    User user = PartyChat.dataHandler().getUser(event.getPlayer());
    user.setPlayer(null);
    user.setOnline(false);
    PartyChat.dataHandler().addToUserMap(user);
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

  public @NotNull String getNickname() {
    String ret;
    if (nickname == null || nickname.length() == 0) {
      ret = username;
    } else {
      ret = nickname;
    }
    return ret == null ? "" : ret;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
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

  public boolean isPartyOnly() {
    return partyOnly;
  }

  public void setPartyOnly(boolean partyOnly) {
    this.partyOnly = partyOnly;
  }

  public boolean flipPartyOnly() {
    this.partyOnly = !this.partyOnly;
    return this.partyOnly;
  }

  public boolean isLeader() {
    if (!isInParty() || getPartyID() == null)
      return false;
    Party party = PartyChat.partyHandler().getParty(getPartyID());
    if (party == null)
      return false;
    return party.getLeader().getPlayerID().equals(getPlayerID());
  }

  @Nullable
  public Party getParty() {
    if (getPartyID() == null || !isInParty())
      return null;
    else
      return PartyChat.partyHandler().getParty(getPartyID());
  }

  public void sendMessage(Component message) {
    if (!isOnline || player == null)
      return;
    try {
      player.sendMessage(message);  // paper
    } catch (NoSuchMethodError error) {
      BukkitAudiences.create(PartyChat.core()).player(player).sendMessage(message); // spigot
    }
  }

  public boolean isChatInputCreate() {
    return chatInputCreate;
  }

  public void setChatInputCreate(boolean chatInputCreate) {
    this.chatInputCreate = chatInputCreate;
  }

  public boolean isChatInputInvite() {
    return chatInputInvite;
  }

  public void setChatInputInvite(boolean chatInputInvite) {
    this.chatInputInvite = chatInputInvite;
  }

  public boolean isChatInputRename() {
    return chatInputRename;
  }

  public void setChatInputRename(boolean chatInputRename) {
    this.chatInputRename = chatInputRename;
  }

  public boolean isChatInputLeave() {
    return chatInputLeave;
  }

  public void setChatInputLeave(boolean chatInputLeave) {
    this.chatInputLeave = chatInputLeave;
  }
}