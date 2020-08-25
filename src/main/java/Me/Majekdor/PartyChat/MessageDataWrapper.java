package Me.Majekdor.PartyChat;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;

public class MessageDataWrapper {
    static DataManager MessageConfig;
    Main plugin;
    public MessageDataWrapper(Main instance) {
        plugin = instance;
        MessageDataWrapper.MessageConfig = new DataManager(instance, null, "messages.yml");
        MessageDataWrapper.MessageConfig.createFile(
                null,
                "PartyChat by Majekdor - Need help? Message me on Discord @Majekdor#6346\r\n" +
                "\r\n" +
                "=========================\r\n" +
                "| MESSAGE CONFIGURATION |\r\n" +
                "=========================\r\n" +
                "\r\n" +
                "Available Placeholders:\r\n" +
                "NOTE: Do NOT throw these into random messages. Only move them within the message they're in\r\n" +
                "and don't expect %player% to work how you want it to wherever it is.\r\n" +
                "\r\n" +
                "%partyName% - displays the name of the party\r\n" +
                "%leader% - displays the party leader\r\n" +
                "%player% - displays the player name\r\n" +
                "%prefix% - displays the PartyChat prefix as chosen above\r\n" +
                "%version% - displays the plugin version\r\n" +
                "\r\n" +
                "Messages:\r\n" +
                "");
        FileConfiguration m = MessageDataWrapper.MessageConfig.getConfig();

        m.addDefault("party-info", Arrays.asList(
                "&7&l----------------------------",
                "                 %prefix%",
                "          &7Created by: &bMajekdor",
                "                &7Version: &b%version%",
                "&7&l----------------------------"
        ));
        m.addDefault("party-help1", Arrays.asList(
                "&7--------------- %prefix% &bHelp &7---------------",
                "&7Commands with &c* &7are only available to the party leader.",
                "&b/party create <name> &7- Creates a new party for you.",
                "&b/party add <player> &7- Invites a player to your party.",
                "&b/party accept &7- Accepts the party invite.",
                "&b/party deny &7- Denies the party invite.",
                "&b/party remove <player> &7- Removes a player from the party. &c*",
                "&7You are on page 1, type &b/party help 2 &7to view other commands."
        ));
        m.addDefault("party-help2", Arrays.asList(
                "&7--------------- %prefix% &bHelp &7---------------",
                "&7Commands with &c* &7are only available to the party leader.",
                "&b/party promote <player> &7- Promotes a new player to leader. &c*",
                "&b/party leave &7- Leaves the current party.",
                "&b/party disband &7- Disbands the entire party. &c*",
                "&b/party info &7- Lists info about your current party.",
                "&b/partychat &7- Toggles party chat. Alias: &b/pc",
                "&7You are on page 2, type &b/party help 1 &7to view other commands."
        ));
        m.addDefault("party-created", Arrays.asList(
                "%prefix% &7You have created a party named &b&l%partyName%&7!",
                "%prefix% &7Use /party add <player> to invite another player!"
        ));
        m.addDefault("invite-message", Arrays.asList(
                "%prefix% &b%player% &7has invited you to join &b&l%partyName%&7!",
                "&7Type &b/party accept &7to join.",
                "&7Type &b/party deny &7to decline."
        ));
        m.addDefault("party-prefix", "&f[&bParty&eChat&f]");
        m.addDefault("message-format", "&f[&b%partyName%&f] &7%player% &f» ");
        m.addDefault("spy-format", "&e&oSpy&7&o: &f[&b%partyName%&f] &7%player% &f» ");
        m.addDefault("no-permission", "%prefix% &cYou don't have permission to use this command!");
        m.addDefault("unknown-command", "%prefix% &cUnknown command. Use /party help.");
        m.addDefault("in-party", "%prefix% &cYou are already in a party!");
        m.addDefault("not-in-party", "%prefix% &cYou are not in a party!");
        m.addDefault("not-leader", "%prefix% &cOnly the party leader can use this command!");
        m.addDefault("no-invites", "%prefix% &cYou have no pending invites!");
        m.addDefault("specify-player", "%prefix% &cPlease specify a player!");
        m.addDefault("no-name", "%prefix% &cPlease give your party a name!");
        m.addDefault("less-20", "%prefix% &cParty name must be less than 20 characters!");
        m.addDefault("name-taken", "%prefix% &cA party with that name already exists!");
        m.addDefault("inappropriate-name", "%prefix% &cThat name is inappropriate.");
        m.addDefault("name-only-one", "%prefix% &cYour party name can only be one word!");
        m.addDefault("info-leader", "%prefix% &b&l%partyName% &7- Leader: &b");
        m.addDefault("info-members", "%prefix% &b&l%partyName% &7- Leader: &b%player% &7Members: ");
        m.addDefault("not-online", "%prefix% &cPlayer is not online!");
        m.addDefault("player-in-party", "%prefix% &cSpecified player is already in the party!");
        m.addDefault("player-not-in-party", "%prefix% &cPlayer is not in the party!");
        m.addDefault("promote-self", "%prefix% &cYou cannot promote yourself!");
        m.addDefault("player-join", "%prefix% &b%player% &7has joined the party!");
        m.addDefault("you-join", "%prefix% &7You have joined &b&l%partyName%&7!");
        m.addDefault("decline-join", "%prefix% &b%player% &7has declined the party invite.");
        m.addDefault("you-decline", "%prefix% &7Party invited denied.");
        m.addDefault("player-leave", "%prefix% &b%player% &7has left the party.");
        m.addDefault("you-leave", "%prefix% &7You have left &b&l%partyName%&7.");
        m.addDefault("new-leader", "%prefix% &b%player% &7is now the party leader.");
        m.addDefault("you-leader", "%prefix% &7You are now the leader of the party.");
        m.addDefault("you-promoted", "%prefix% &b%player% &7has promoted you to party leader.");
        m.addDefault("player-removed", "%prefix% &b%player% &7has been removed from the party.");
        m.addDefault("you-removed", "%prefix% &b%player% &7has removed you from the party.");
        m.addDefault("party-disbanded", "%prefix% &b&l%partyName% &7has been disbanded.");
        m.addDefault("pc-enabled", "%prefix% &7Party chat &aenabled&7.");
        m.addDefault("pc-disabled", "%prefix% &7Party chat &cdisabled&7.");
        m.addDefault("invite-sent", "%prefix% &7Party invite sent to &b%player%&7.");
        m.addDefault("spy-enabled", "&2&oAdmin %prefix% &7Spy mode is now &aenabled&7.");
        m.addDefault("spy-disabled", "&2&oAdmin %prefix% &7Spy mode is now &cdisabled&7.");

        MessageDataWrapper.MessageConfig.saveConfig();
        MessageDataWrapper.MessageConfig.reloadConfig();
    }

}
