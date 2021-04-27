package dev.majek.pc.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Consumer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

/**
 * Used to check for plugin updates from the Spigot plugin page.
 */
public class UpdateChecker {

    private final JavaPlugin plugin;
    private final int resourceId;
    private final int currentVersion;
    private int spigotVersion;

    /**
     * Construct a new update checker.
     * @param plugin The main class of the plugin.
     * @param resourceId Plugin's resource id on Spigot.
     */
    public UpdateChecker(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.currentVersion = Integer.parseInt(plugin.getDescription().getVersion().replace(".", ""));
        getSpigotVersion(version -> this.spigotVersion = Integer.parseInt(version.replace(".", "")));
    }

    /**
     * Get the plugin version currently posted on Spigot.
     * @param consumer Used to retrieve version (takes a minute).
     */
    private void getSpigotVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource="
                    + this.resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext())
                    consumer.accept(scanner.next());
            } catch (IOException exception) {
                this.plugin.getLogger().info("Cannot look for updates: " + exception.getMessage());
            }
        });
    }

    public boolean isAheadOfSpigot() {
        return currentVersion > spigotVersion;
    }

    public boolean isBehindSpigot() {
        return spigotVersion > currentVersion;
    }

    public boolean isSpigotLatest() {
        return spigotVersion == currentVersion;
    }
}
