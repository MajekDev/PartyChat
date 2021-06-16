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
import dev.majek.pc.api.PartyRenameEvent;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.Restrictions;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.chat.ChatUtils;
import org.bukkit.entity.Player;

/**
 * Handles <code>/party rename</code>.
 */
public class PartyRename extends PartyCommand {

  public PartyRename() {
    super(
        "rename", getSubCommandUsage("rename"), getSubCommandDescription("rename"),
        true, getSubCommandDisabled("rename"), getSubCommandCooldown("rename"),
        getSubCommandAliases("rename")
    );
  }

  @Override
  public boolean execute(Player player, String[] args, boolean leftServer) {

    // Check if the player is already in a party
    if (!PartyChat.dataHandler().getUser(player).isInParty()) {
      sendMessage(player, "not-in-party");
      return false;
    }

    // Make sure the player specifies a party name
    if (args.length == 1) {
      sendMessage(player, "no-name");
      return false;
    }

    return execute(player, args[1]);
  }

  public static boolean execute(Player player, String newName) {
    User user = PartyChat.dataHandler().getUser(player);
    Party party = user.getParty();

    // This should never happen, but I want to know if it does
    if (party == null) {
      PartyChat.error("Error: PC-REN_1 | The plugin is fine, but please report this error " +
          "code here: https://discord.gg/CGgvDUz");
      PartyChat.messageHandler().sendMessage(player, "error");
      return false;
    }

    // Check if a party with that name already exists
    if (PartyChat.partyHandler().isNameTaken(newName)) {
      PartyChat.messageHandler().sendMessage(player, "name-taken");
      return false;
    }

    // Check if the server is blocking inappropriate names and block them if the name contains them
    if (PartyChat.dataHandler().getConfigBoolean(mainConfig, "block-inappropriate-names")) {
      if (Restrictions.containsCensoredWord(newName)) {
        PartyChat.messageHandler().sendMessage(player, "inappropriate-name");
        return false;
      }
    }

    // Check if the party name exceeds the character limit defined in the config file
    if (ChatUtils.removeColorCodes(newName).length() > PartyChat.dataHandler()
        .getConfigInt(mainConfig, "max-name-length")) {
      PartyChat.messageHandler().sendMessage(player, "name-too-long");
      return false;
    }

    PartyRenameEvent event = new PartyRenameEvent(player, party, newName);
    PartyChat.core().getServer().getPluginManager().callEvent(event);
    if (event.isCancelled())
      return true;

    party.setName(event.getNewName());
    PartyChat.messageHandler().sendMessageWithReplacement(player, "party-rename", "%partyName%", event.getNewName());

    // Update the database if persistent parties is enabled
    if (PartyChat.dataHandler().persistentParties)
      PartyChat.partyHandler().saveParty(party);
    return true;
  }
}