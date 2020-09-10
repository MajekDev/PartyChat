package Me.Majekdor.PartyChat;

import Me.Majekdor.PartyChat.command.*;
import Me.Majekdor.PartyChat.data.ConfigUpdater;
import Me.Majekdor.PartyChat.listener.PartyListeners;
import Me.Majekdor.PartyChat.util.Chat;
import Me.Majekdor.PartyChat.data.DataManager;
import Me.Majekdor.PartyChat.data.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

// Created by Majekdor on 5/23/2020

public final class PartyChat extends JavaPlugin {
    public static DataManager messageData;
    public static PartyChat instance;
    public static PartyChat getInstance() { return instance; }
    PluginDescriptionFile pdf = PartyChat.instance.getDescription();

    public PartyChat() {
        instance = this;
    }

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage(Chat.colorize("&f[&bParty&eChat&f] &aLet's get this party started yo..."));
        messageData = new DataManager(instance, null, "messages.yml");

        // Update config and messages files
        this.saveDefaultConfig();
        File configFile = new File(getDataFolder(), "config.yml"); String[] foo = new String[0];
        try {
            ConfigUpdater.update(instance, "config.yml", configFile, Arrays.asList(foo));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.reloadConfig();
        messageData.saveDefaultConfig();
        File messagesFile = new File(getDataFolder(), "messages.yml");
        try {
            ConfigUpdater.update(instance, "messages.yml", messagesFile, Arrays.asList(foo));
        } catch (IOException e) {
            e.printStackTrace();
        }
        messageData.reloadConfig();

        this.getCommand("partychat").setExecutor(new CommandPartyChat());
        this.getCommand("pcreload").setExecutor(new CommandReload());
        this.getCommand("partychat").setTabCompleter(new TabComplete());
        this.getCommand("party").setExecutor(new CommandParty());
        this.getCommand("party").setTabCompleter(new TabComplete());
        this.getCommand("spy").setExecutor(new CommandSpy(this));
        this.getServer().getPluginManager().registerEvents(new PartyListeners(), this);
        int pluginId = 7667; Metrics metrics = new Metrics(this, pluginId); // Metric stuffs
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                Bukkit.getConsoleSender().sendMessage("[PCv2] Successfully loaded PartyChat version " +  pdf.getVersion()), 60L); // 3 second delay
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(Chat.format("&f[&bParty&eChat&f] &cDisbanding all parties for the night..."));
    }
}
