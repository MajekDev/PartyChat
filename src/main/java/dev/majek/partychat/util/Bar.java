package dev.majek.partychat.util;

import dev.majek.partychat.PartyChat;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Bar {

    private static BossBar bar;
    private int taskID;
    FileConfiguration m = PartyChat.messageData.getConfig();

    public void addPlayer(Player p) {
        bar.addPlayer(p);
    }

    public void removePlayer(Player p) {
        bar.removePlayer(p);
    }

    public void createBar() {
        bar = Bukkit.createBossBar(Chat.colorize(m.getString("teleport-bar-text")), BarColor.BLUE, BarStyle.SOLID);
        bar.setVisible(true); cast();
    }

    public void removeBar() {
        bar.setVisible(false);
    }

    public BossBar getBar() {
        return bar;
    }

    public void cast() {
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PartyChat.instance, new Runnable() {
            double progress = 1.0; final double time = 1.0 / (3 * 20);
            @Override
            public void run() {
                bar.setProgress(progress);
                progress = progress - time;
                if (progress <= 0.0) {
                    Bukkit.getScheduler().cancelTask(taskID);
                }
            }
        }, 0, 0);
    }

}
