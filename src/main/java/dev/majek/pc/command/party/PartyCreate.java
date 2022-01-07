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
package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.Restrictions;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.api.PartyCreateEvent;
import dev.majek.pc.message.ChatUtils;
import dev.majek.pc.util.TabCompleterBase;
import org.bukkit.entity.Player;

/**
 * Handles <code>/party create</code>.
 */
public class PartyCreate extends PartyCommand {

  public PartyCreate() {
    super(
        "create", getSubCommandUsage("create"), getSubCommandDescription("create"),
        false, getSubCommandDisabled("create"), getSubCommandCooldown("create"),
        getSubCommandAliases("create")
    );
  }

  @Override
  public boolean execute(Player player, String[] args, boolean leftServer) {

    // Check if the player is already in a party
    if (PartyChat.dataHandler().getUser(player).isInParty()) {
      sendMessage(player, "in-party");
      return false;
    }

    // Make sure the player specifies a party name
    if (args.length == 1) {
      sendMessage(player, "no-name");
      return false;
    }

    // Continue in a separate method, passing through party name
    return execute(player, TabCompleterBase.joinArgsBeyond(0, "-", args));
  }

  public static boolean execute(Player player, String name) {
    // Check if a party with that name already exists
    if (PartyChat.partyHandler().isNameTaken(name)) {
      PartyChat.messageHandler().sendMessage(player, "name-taken");
      return false;
    }

    // Check if the server is blocking inappropriate names and block them if the name contains them
    if (PartyChat.dataHandler().getConfigBoolean(mainConfig, "block-inappropriate-names")) {
      if (Restrictions.containsCensoredWord(name)) {
        PartyChat.messageHandler().sendMessage(player, "inappropriate-name");
        return false;
      }
    }

    // Check if the party name exceeds the character limit defined in the config file
    int max = PartyChat.dataHandler().getConfigInt(mainConfig, "max-name-length");
    if (ChatUtils.removeColorCodes(name).length() > max && max > 0) {
      PartyChat.messageHandler().sendMessage(player, "name-too-long");
      return false;
    }

    // Passed all requirements, create new Party object
    Party party = new Party(player, name);

    // Run PartyCreateEvent
    PartyCreateEvent event = new PartyCreateEvent(player, party);
    PartyChat.core().getServer().getPluginManager().callEvent(event);
    if (event.isCancelled())
      return false;

    // Put the player in the main party map and send message
    PartyChat.partyHandler().addToPartyMap(party.getId(), party);
    PartyChat.dataHandler().getUser(player).setPartyID(party.getId());
    PartyChat.dataHandler().getUser(player).setInParty(true);
    for (String message : PartyChat.dataHandler().getConfigStringList(PartyChat
        .dataHandler().messages, "party-created")) {
      player.sendMessage(ChatUtils.applyColorCodes(message.replace("%prefix%", PartyChat
          .dataHandler().getConfigString(PartyChat.dataHandler().messages, "prefix"))
          .replace("%partyName%", name)));
    }

    // Update the database if persistent parties is enabled
    if (PartyChat.dataHandler().persistentParties) {
      PartyChat.partyHandler().saveParty(party);
    }
    return true;
  }
}