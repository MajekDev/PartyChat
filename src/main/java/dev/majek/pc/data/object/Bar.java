package dev.majek.pc.data.object;

import dev.majek.pc.PartyChat;
import dev.majek.pc.util.Chat;
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
        bar = Bukkit.createBossBar(Chat.applyColorCodes(PartyChat.getDataHandler().getConfigString(PartyChat
                .getDataHandler().messages, "teleport-bar-text")), BarColor.BLUE, BarStyle.SOLID);
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
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PartyChat.getCore(), new Runnable() {
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
