package me.majekdor.partychat.sqlite;

import me.majekdor.partychat.PartyChat;

import java.util.logging.Level;

public class Error {
    public static void execute(PartyChat plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }
    public static void close(PartyChat plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}
