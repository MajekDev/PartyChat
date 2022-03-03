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
package dev.majek.pc.command.party;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.bukkit.entity.Player;

/**
 * Handles <code>/party toggle</code>
 */
public class PartyToggle extends PartyCommand {

  public PartyToggle() {
    super(
        "toggle", getSubCommandUsage("toggle"), getSubCommandDescription("toggle"),
        true, getSubCommandDisabled("toggle"), getSubCommandCooldown("toggle"),
        getSubCommandAliases("toggle")
    );
  }

  @Override
  public boolean execute(Player player, String[] args, boolean leftServer) {
    User user = PartyChat.dataHandler().getUser(player);

    // Check if the player is not in a party
    if (!user.isInParty()) {
      sendMessage(player, "not-in-party");
      return false;
    }

    if (args.length == 1) {
      sendMessage(player, "choose-toggle");
      return false;
    }

    Party party = user.getParty();

    // This should never happen, but I want to know if it does
    if (party == null) {
      PartyChat.error("Error: PC-TOG_1 | The plugin is fine, but please report this error " +
          "code here: https://discord.gg/CGgvDUz");
      sendMessage(player, "error");
      return false;
    }

    // Apply toggles
    if (args[1].equalsIgnoreCase("public")) {
      if (args.length == 2) {
        sendMessageWithReplacement(player, "public-party-status", "%status%",
            party.isPublic() ? "public" : "private");
      } else {
        if (args[2].equalsIgnoreCase("true")) {
          sendMessage(player, "toggle-public");
          party.setPublic(true);
        } else if (args[2].equalsIgnoreCase("false")) {
          sendMessage(player, "toggle-private");
          party.setPublic(false);
        }
      }
    } else if (args[1].equalsIgnoreCase("friendly-fire")) {
      if (args.length == 2) {
        sendMessageWithReplacement(player, "friendly-fire-status", "%status%",
            party.allowsFriendlyFire() ? "allow" : "deny");
      } else {
        if (args[2].equalsIgnoreCase("allow")) {
          sendMessage(player, "friendly-fire-set-enabled");
          party.setFriendlyFire(true);
        } else if (args[2].equalsIgnoreCase("deny")) {
          sendMessage(player, "friendly-fire-set-disabled");
          party.setFriendlyFire(false);
        }
      }
    }
    return true;
  }
}