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
        runnable.runTaskTimer(PartyChat.getCore(), 0L, 20L);
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
