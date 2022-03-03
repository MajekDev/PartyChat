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
package dev.majek.pc.api;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyChatCommand;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;

/**
 * Handles PartyChat api methods.
 */
public class PartyChatApi {

  /**
   * Reloads the PartyChat plugin. If changes have been made to config files these will be applied.
   */
  public void reloadPlugin() {
    PartyChat.dataHandler().reload();
    PartyChat.commandHandler().reload();
    PartyCommand.reload();
    PartyChatCommand.reload();
  }

  /**
   * Create a new party with a party name and user as the leader.
   * This is a manual creation and will not trigger {@link PartyCreateEvent}.
   *
   * @param name   The name of the party.
   * @param leader The {@link User} who will be leader.
   */
  public void createParty(@NotNull String name, @NotNull User leader) {
    Party party = new Party(
        name,
        leader.getPlayerID().toString(),
        Collections.singletonList(leader),
        PartyChat.dataHandler().getConfigBoolean(PartyChat.dataHandler().mainConfig, "public-on-creation"),
        PartyChat.dataHandler().getConfigBoolean(PartyChat.dataHandler().mainConfig, "default-friendly-fire")
    );

    PartyChat.partyHandler().addToPartyMap(party.getId(), party);
    leader.setPartyID(party.getId());
    leader.setInParty(true);

    if (PartyChat.dataHandler().persistentParties)
      PartyChat.partyHandler().saveParty(party);
  }

  /**
   * Get a {@link Party} from a {@link User} in the party.
   *
   * @param user The user who must be in the party. Will return null if user is not in a party.
   * @return The {@link Party} the {@link User} is in, if in one.
   */
  @Nullable
  public Party getParty(@NotNull User user) {
    return PartyChat.partyHandler().getPartyMap().get(user.getPartyID());
  }

  /**
   * Get a {@link Party} from the party's unique id.
   *
   * @param party The party's unique id, note this may NOT be a player id.
   *              Will return null if no party with the specified id exists.
   * @return The {@link Party}, if it exists.
   */
  @Nullable
  public Party getParty(@NotNull UUID party) {
    return PartyChat.partyHandler().getPartyMap().get(party);
  }
}