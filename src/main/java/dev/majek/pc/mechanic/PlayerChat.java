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
package dev.majek.pc.mechanic;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.party.PartyAdd;
import dev.majek.pc.command.party.PartyCreate;
import dev.majek.pc.command.party.PartyLeave;
import dev.majek.pc.command.party.PartyRename;
import dev.majek.pc.data.Restrictions;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.chat.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Handles the player chat event.
 */
public class PlayerChat extends Mechanic {

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    Player player = event.getPlayer();
    User user = PartyChat.dataHandler().getUser(player);

    if (user.isChatInputCreate()) {
      if (PartyChat.commandHandler().getCommand(PartyCreate.class).canUse(player)) {
        Bukkit.getScheduler().runTask(PartyChat.core(), () -> {
          PartyCreate.execute(player, event.getMessage());
          user.setChatInputCreate(false);
        });
        event.setCancelled(true);
        return;
      }
    }

    if (user.isChatInputInvite()) {
      if (PartyChat.commandHandler().getCommand(PartyAdd.class).canUse(player)) {
        Bukkit.getScheduler().runTask(PartyChat.core(), () -> {
          PartyAdd.execute(player, event.getMessage());
          user.setChatInputInvite(false);
        });
        event.setCancelled(true);
        return;
      }
    }

    if (user.isChatInputRename()) {
      if (PartyChat.commandHandler().getCommand(PartyRename.class).canUse(player)) {
        Bukkit.getScheduler().runTask(PartyChat.core(), () -> {
          PartyRename.execute(player, event.getMessage());
          user.setChatInputRename(false);
        });
        event.setCancelled(true);
        return;
      }
    }

    if (user.isChatInputLeave()) {
      if (PartyChat.commandHandler().getCommand(PartyLeave.class).canUse(player)) {
        Bukkit.getScheduler().runTask(PartyChat.core(), () -> {
          PartyLeave.execute(user, PartyChat.dataHandler().getUser(event.getMessage()), false);
          user.setChatInputLeave(false);
        });
        event.setCancelled(true);
        return;
      }
    }

    if (user.isInParty() && user.partyChatToggle()) {
      event.setCancelled(true);
      Party party = user.getParty();
      String message = event.getMessage();

      // This should never happen, but I want to know if it does
      if (party == null) {
        PartyChat.error("Error: PC-CHT_1 | The plugin is fine, but please report this error " +
            "code here: https://discord.gg/CGgvDUz");
        PartyChat.messageHandler().sendMessage(user, "error");
        return;
      }

      if (Restrictions.isMuted(user.getPlayer())) {
        PartyChat.messageHandler().sendMessage(user, "muted");
        return;
      }

      PartyChat.partyHandler().sendMessageToPartyChat(party, user, message);
    } else {
      event.getRecipients().removeIf(recipient -> PartyChat.dataHandler().getUser(recipient).isPartyOnly());
      if (PartyChat.dataHandler().getConfigBoolean(PartyChat.dataHandler().mainConfig, "format-chat"))
        event.setMessage(ChatUtils.applyColorCodes(event.getMessage()));
    }
  }
}