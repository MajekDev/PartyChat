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
package dev.majek.pc.data.legacy;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.object.Party;
import dev.majek.pc.data.object.User;
import dev.majek.pc.util.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Legacy PartyChat database for persistent party storage.
 */
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
    String name = null, uuidString = null, serializedMembers = null; int isPublic = 0;
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
            isPublic = rs.getInt("isPublic");
          }
        }
        String finalUuidString = uuidString;
        List<UUID> memberIDs = Utils.deserializeMembers(serializedMembers).stream()
            .filter(uuid -> !uuid.toString().equals(finalUuidString)).collect(Collectors.toList());
        List<User> members = memberIDs.stream().map(User::new).collect(Collectors.toList());
        Party party = new Party(
            name,
            uuidString,
            members,
            isPublic == 1,
            false
        );
        PartyChat.partyHandler().getPartyMap().put(party.getId(), party);
        PartyChat.partyHandler().saveParty(party);
        members.forEach(member -> {
          member.setPartyID(party.getId());
          PartyChat.dataHandler().addToUserMap(member);
        });
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