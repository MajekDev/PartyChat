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
package dev.majek.pc.data.object;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.PartyCommand;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents the amount of time between running commands.
 */
public class Cooldown {

  private int timeRemaining;
  private boolean finished = false;

  public Cooldown(PartyCommand partyCommand) {
    this.timeRemaining = partyCommand.getCooldown();
    run();
  }

  public void run() {
    BukkitRunnable runnable = new BukkitRunnable() {
      @Override
      public void run() {
        if (timeRemaining <= 0) {
          finished = true;
          cancel();
        }
        if (!finished)
          timeRemaining -= 1;
        if (finished)
          cancel();
      }
    };
    runnable.runTaskTimer(PartyChat.core(), 0L, 20L);
  }

  public int getTimeRemaining() {
    return timeRemaining;
  }

  public boolean isFinished() {
    return finished;
  }

  public void finish() {
    finished = true;
  }
}