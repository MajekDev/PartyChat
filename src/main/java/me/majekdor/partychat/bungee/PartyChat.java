package me.majekdor.partychat.bungee;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.logging.Level;

public class PartyChat extends Plugin {

    @Override
    public void onEnable() {
        this.getLogger().log(Level.INFO, "Detected Bungeecord... yay!");

    }

    @Override
    public void onDisable() {

    }
}
