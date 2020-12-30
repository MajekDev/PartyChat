package dev.majek.partychat.sqlite;

import dev.majek.partychat.PartyChat;
import dev.majek.partychat.data.Party;
import dev.majek.partychat.util.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public abstract class Database {

    PartyChat plugin;
    Connection connection;
    public String table = "parties";

    public Database(PartyChat instance) {
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize() {
        connection = getSQLConnection();
    }

    public void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }

    public void clearTable() {
        Connection conn;
        PreparedStatement ps;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM parties");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        }
    }

    public void updateParty(Party party) {
        Connection conn = null;
        PreparedStatement ps = null;
        // Delete the current entry for the party
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM parties WHERE name = '"+party.name+"';");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        // Add the updated party information if the party wasn't deleted
        if (Party.partyMap.containsValue(party))
            addParty(party.name, party.leader, Utils.serializeMembers(party.members), party.size, party.isPublic);
    }

    public void addParty(String name, UUID leader, String serializedMembers, Integer size, boolean isPublic) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("INSERT INTO " + table + " (name,leaderUUID,serializedMembers," +
                    "size,isPublic) VALUES(?,?,?,?,?)");
            ps.setString(1, name);
            ps.setString(2, leader.toString());
            ps.setString(3, serializedMembers);
            ps.setInt(4, size);
            ps.setInt(5, isPublic ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }

    }

    List<String> partyNames = new ArrayList<>();

    public void getPartyNames() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table);
            rs = ps.executeQuery();
            while (rs.next()) {
                partyNames.add(rs.getString("name"));
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    public void getParties() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        String name = null, uuidString = null, serializedMembers = null; int size = 0, isPublic = 0;
        for (String partyName : partyNames) {
            try {
                conn = getSQLConnection();
                ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE name = '"+partyName+"';");
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getString("name").equalsIgnoreCase(partyName)) {
                        name = rs.getString("name");
                        uuidString = rs.getString("leaderUUID");
                        serializedMembers = rs.getString("serializedMembers");
                        size = rs.getInt("size");
                        isPublic = rs.getInt("isPublic");
                    }
                }
                List<UUID> members = new ArrayList<>(Utils.deserializeMembers(serializedMembers));
                Party party = new Party(name, uuidString, members, size, isPublic == 1);
                for (UUID member : members) {
                    Party.partyMap.put(member, party);
                }
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
            } finally {
                try {
                    if (ps != null)
                        ps.close();
                    if (conn != null)
                        conn.close();
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
                }
            }
        }
    }

}
