package Me.Majekdor.PartyChat;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Created by Majekdor on 5/23/2020

public final class Main extends JavaPlugin {
    public MessagesManager messageData;
    //public static String format(String format) {
    //    return ChatColor.translateAlternateColorCodes('&', format);
    //}

    public static final Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");

    public static String format(String msg) {
        if (Bukkit.getVersion().contains("1.16")) {
            Matcher match = pattern.matcher(msg);
            while (match.find()) {
                String color = msg.substring(match.start(), match.end());
                msg = msg.replace(color, ChatColor.of(color) + "");
                match = pattern.matcher(msg);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    @Override
    public void onEnable() {
        new MessageDataWrapper(this);
        this.getServer().getPluginManager().registerEvents(new PartyListeners(this), this);
        Bukkit.getConsoleSender().sendMessage(format("&f[&bParty&eChat&f] &aLet's get this party started yo..."));
        final FileConfiguration config = this.getConfig();
        this.saveDefaultConfig();
        this.getCommand("partychat").setExecutor(new PartyCommands(this));
        this.getCommand("partychat").setTabCompleter(new PCTab());
        this.getCommand("party").setExecutor(new PartyCommands(this));
        this.getCommand("party").setTabCompleter(new PartyTab());
        this.getCommand("spy").setExecutor(new SpyCommand(this));
        //bstats stuff
        int pluginId = 7667; Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(format("&f[&bParty&eChat&f] &cDisbanding all parties for the night..."));
    }
}
