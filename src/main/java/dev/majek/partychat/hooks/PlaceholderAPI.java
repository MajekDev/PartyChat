package dev.majek.partychat.hooks;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import dev.majek.partychat.PartyChat;
import dev.majek.partychat.data.Party;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPI extends PlaceholderExpansion {

    private final PartyChat plugin;
    private String yes;
    private String no;

    public PlaceholderAPI(PartyChat plugin){
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
            return Integer.toString((int) Party.partyMap.values().stream().distinct().count());

        // %partychat_players_in_parties% - get the number of players in a party
        if (identifier.equalsIgnoreCase("playersInParty"))
            return Integer.toString(Party.partyMap.size());

        // %partychat_persistent_parties% - whether or not persistent parties is enabled
        if (identifier.equalsIgnoreCase("persistentParties"))
            return plugin.getConfig().getBoolean("persistent-parties") ? yes : no;

        // %partychat_player_partyName% - get the name of the party the player is in
        if (identifier.equalsIgnoreCase("playerPartyName"))
            return Party.getParty(player.getUniqueId()).name == null ? "Not in a party" :
                    Party.getRawName(Party.getParty(player.getUniqueId()));

        return null;
    }

}
