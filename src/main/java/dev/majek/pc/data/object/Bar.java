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
package dev.majek.pc.data.object;

import dev.majek.pc.PartyChat;
import dev.majek.pc.chat.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

/**
 * Create the teleportation BossBar for /party summon.
 */
public class Bar {

  private BossBar bar;
  private int taskID;

  public void addPlayer(Player player) {
    bar.addPlayer(player);
  }

  public void removePlayer(Player player) {
    bar.removePlayer(player);
  }

  public void createBar(int delay) {
    bar = Bukkit.createBossBar(ChatUtils.applyColorCodes(PartyChat.dataHandler().getConfigString(PartyChat
        .dataHandler().messages, "teleport-bar-text")), BarColor.BLUE, BarStyle.SOLID);
    bar.setVisible(true);
    cast(delay);
  }

  public void removeBar() {
    bar.setVisible(false);
  }

  public BossBar getBar() {
    return bar;
  }

  public void cast(int delay) {
    taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PartyChat.core(), new Runnable() {
      double progress = 1.0;
      final double time = 1.0 / (delay * 20);
      @Override
      public void run() {
        bar.setProgress(progress);
        progress = progress - time;
        if (progress <= 0.0)
          Bukkit.getScheduler().cancelTask(taskID);
      }
    }, 0, 0);
  }
}