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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

/**
 * SQLite setup for legacy {@link Database}.
 */
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