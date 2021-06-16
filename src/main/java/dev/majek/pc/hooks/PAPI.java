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
package dev.majek.pc.hooks;

import dev.majek.pc.PartyChat;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Handles integration with PlaceholderAPI.
 */
public class PAPI extends PlaceholderExpansion {

  private final PartyChat plugin;
  private String yes;
  private String no;

  public PAPI(PartyChat plugin){
    this.plugin = plugin;
    try {
      yes = PlaceholderAPIPlugin.booleanTrue();
      no = PlaceholderAPIPlugin.booleanFalse();
    } catch (Exception err) {
      plugin.getLogger().info("Unable to hook into PAPI API for boolean results. Defaulting...");
    }
  }

  @Override
  public boolean canRegister(){
    return true;
  }

  @Override
  public boolean persist(){
    return true;
  }

  @Override
  public @NotNull String getAuthor(){
    return plugin.getDescription().getAuthors().get(0);
  }

  @Override
  public @NotNull String getIdentifier(){
    return plugin.getDescription().getName().toLowerCase();
  }

  @Override
  public @NotNull String getVersion(){
    return plugin.getDescription().getVersion();
  }

  @Override
  public String onRequest(OfflinePlayer player, @NotNull String identifier) {

    // %partychat_active_parties% - get number of active parties
    if (identifier.equalsIgnoreCase("activeParties"))
      return Integer.toString((int) PartyChat.partyHandler().getPartyMap().values().stream().distinct().count());

    // %partychat_players_in_parties% - get the number of players in a party
    if (identifier.equalsIgnoreCase("playersInParty"))
      return Integer.toString(PartyChat.partyHandler().getPartyMap().size());

    // %partychat_persistent_parties% - whether or not persistent parties is enabled
    if (identifier.equalsIgnoreCase("persistentParties"))
      return plugin.getConfig().getBoolean("persistent-parties") ? yes : no;

    // %partychat_player_partyName% - get the name of the party the player is in
    if (identifier.equalsIgnoreCase("playerPartyName"))
      return PartyChat.dataHandler().getUser(player.getUniqueId()).getParty().getName() == null ?
          "Not in a party" : PartyChat.dataHandler().getUser(player.getUniqueId()).getParty().getRawName();

    return null;
  }

  public static String applyPlaceholders(Player player, String message) {
    return PlaceholderAPI.setPlaceholders(player, message);
  }
}