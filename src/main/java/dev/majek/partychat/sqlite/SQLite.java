package dev.majek.partychat.sqlite;

import dev.majek.partychat.PartyChat;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQLite extends Database {

    String dbname;
    public SQLite(PartyChat instance) {
        super(instance);
        dbname = plugin.getConfig().getString("SQLite.Filename", "parties");
    }

    public String createTable = "CREATE TABLE IF NOT EXISTS parties (" +
            "'name' varchar(500) NOT NULL," +
            "'leaderUUID' varchar(64) NOT NULL," +
            "'serializedMembers' varchar(10000) NOT NULL," +
            "'size' int(1000) NOT NULL," +
            "'isPublic' int(2) NOT NULL," +
            "PRIMARY KEY ('name'));";

    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), dbname+".db");
        if (!dataFolder.exists()){
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: "+dbname+".db");
            }
        }
        try {
            if(connection!=null&&!connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    public void load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(createTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
}
