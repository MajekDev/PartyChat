package dev.majek.pc.api;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyChatCommand;
import dev.majek.pc.command.PartyCommand;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.UUID;

public class PartyChatAPI {

    /**
     * Reloads the PartyChat plugin. If changes have been made to config files these will be applied.
     */
    public void reloadPlugin() {
        PartyChat.getDataHandler().reload();
        PartyChat.getCommandHandler().reload();
        PartyCommand.reload();
        PartyChatCommand.reload();
    }

    /**
     * Create a new party with a party name and user as the leader.
     * This is a manual creation and will not trigger {@link PartyCreateEvent}.
     *
     * @param name The name of the party.
     * @param leader The {@link User} who will be leader.
     */
    public void createParty(@NotNull String name, @NotNull User leader) {
        Party party = new Party(
                name,
                leader.getPlayerID().toString(),
                Collections.singletonList(leader),
                PartyChat.getDataHandler().getConfigBoolean(PartyChat.getDataHandler().mainConfig, "public-on-creation"),
                PartyChat.getDataHandler().getConfigBoolean(PartyChat.getDataHandler().mainConfig, "default-friendly-fire")
        );

        PartyChat.getPartyHandler().addToPartyMap(party.getId(), party);
        leader.setPartyID(party.getId());
        leader.setInParty(true);

        if (PartyChat.getDataHandler().persistentParties)
            PartyChat.getPartyHandler().saveParty(party);
    }

    /**
     * Get a {@link Party} from a {@link User} in the party.
     *
     * @param user The user who must be in the party. Will return null if user is not in a party.
     * @return The {@link Party} the {@link User} is in, if in one.
     */
    @Nullable
    public Party getParty(@NotNull User user) {
        return PartyChat.getPartyHandler().getPartyMap().get(user.getPartyID());
    }

    /**
     * Get a {@link Party} from the party's unique id.
     *
     * @param party The party's unique id, note this may NOT be a player id. Will return null if no party with
     *              the specified id exists.
     * @return The {@link Party}, if it exists.
     */
    @Nullable
    public Party getParty(@NotNull UUID party) {
        return PartyChat.getPartyHandler().getPartyMap().get(party);
    }

}
