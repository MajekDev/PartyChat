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
import dev.majek.pc.chat.Paginate;
import dev.majek.pc.util.Utils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles <code>/party help</code>.
 */
public class PartyHelp extends PartyCommand {

  public PartyHelp() {
    super(
        "help", getSubCommandUsage("help"), getSubCommandDescription("help"),
        false, getSubCommandDisabled("help"), getSubCommandCooldown("help"),
        getSubCommandAliases("help")
    );
  }

  @Override
  public boolean execute(Player player, String[] args, boolean leftServer) {

    // Build list of lines
    List<String> lines = new ArrayList<>();
    for (PartyCommand partyCommand : PartyChat.commandHandler().getCommands()) {
      String line = "&9&l" + Utils.capitalize(partyCommand.getName()) + " &8»" + "${hover,&7 " + PartyChat
          .dataHandler().getConfigString(PartyChat.dataHandler().messages, "description") + ","
          + partyCommand.getDescription() + "}${hover, &7" + PartyChat.dataHandler()
          .getConfigString(PartyChat.dataHandler().messages, "usage") + "," +
          partyCommand.getUsage() + "}";
      lines.add(line);
    }

    // Create pagination from the lines
    Paginate paginate = new Paginate(lines, PartyChat.dataHandler().getConfigString(PartyChat.dataHandler()
        .messages, "header").replace("%prefix%", PartyChat.dataHandler().getConfigString(PartyChat
        .dataHandler().messages, "prefix")), 8, "party help ");
    String toSend = args.length == 1 ? paginate.getPage(1) : paginate.getPage(Integer.parseInt(args[1]));

    if (toSend == null) {
      sendMessageWithReplacement(player, "invalid-page", "%max%", String.valueOf(paginate.getMaxPage()));
      return true;
    }

    sendFormattedMessage(player, toSend);
    return true;
  }
}