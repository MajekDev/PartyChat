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
package dev.majek.pc.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Handles fetching and caching player skulls for easier use in GUIs.
 */
public class SkullCache {

  /**
   * Skulls and time are stored by uuid regardless of how they're cached or accessed.
   */
  private static final HashMap<UUID, ItemStack> skullMap = new HashMap<>();
  private static final HashMap<UUID, Long> timeMap = new HashMap<>();

  /**
   * Cache a skull from a uuid.
   * @param uuid The player's uuid.
   */
  public static void cacheSkull(UUID uuid) {
    skullMap.put(uuid, skullFromUuid(uuid));
    timeMap.put(uuid, System.currentTimeMillis());
  }

  /**
   * Cache a skull from an offline player.
   * @param offlinePlayer The offline player.
   */
  public static void cacheSkull(OfflinePlayer offlinePlayer) {
    cacheSkull(offlinePlayer.getUniqueId());
  }

  /**
   * Cache a skull from an online player.
   * @param player The online player.
   */
  public static void cacheSkull(Player player) {
    cacheSkull(player.getUniqueId());
  }

  /**
   * Cache an array of skulls from uuids.
   * Task will run asynchronously in an attempt to prevent server lag.
   * @param uuids Array of uuids.
   */
  public static void cacheSkulls(UUID[] uuids) {
    new Thread(() -> {
      long start = System.currentTimeMillis();
      for (UUID uuid : uuids) {
        skullMap.put(uuid, skullFromUuid(uuid));
        timeMap.put(uuid, System.currentTimeMillis());
      }
      // Generate an inventory off the rip to try to fix the hashmap
      Inventory inventory  = Bukkit.createInventory(null, 54, "Skull Cache Test");
      for (int i = 0; i < Math.min(54, uuids.length); i++) {
        inventory.setItem(i, SkullCache.getSkull(uuids[i]).clone());
      }
      inventory.clear();
      Bukkit.getLogger().log(Level.INFO, ChatColor.GREEN + "[SkullCache] Cached " + uuids.length
          + " skulls in " + (System.currentTimeMillis() - start) + "ms.");
    }).start();
  }

  /**
   * Cache an array of skulls from offline players.
   * @param offlinePlayers Array of offline players.
   */
  public static void cacheSkulls(OfflinePlayer[] offlinePlayers) {
    cacheSkulls((UUID[]) Arrays.stream(offlinePlayers).map(OfflinePlayer::getUniqueId).toArray());
  }

  /**
   * Cache an array of skulls from online players.
   * @param players Array of online players.
   */
  public static void cacheSkulls(Player[] players) {
    cacheSkulls((UUID[]) Arrays.stream(players).map(Player::getUniqueId).toArray());
  }

  /**
   * Get a skull from a uuid. If the skull is not saved in memory it will
   * be fetched from Mojang and then cached for future use.
   * @param uuid  The player's uuid.
   * @return      ItemStack of the player's skull.
   */
  public static ItemStack getSkull(UUID uuid) {
    timeMap.put(uuid, System.currentTimeMillis());
    ItemStack skull = skullMap.get(uuid);
    if (skull == null) {
      skull = skullFromUuid(uuid);
      cacheSkull(uuid);
    }
    return skull;
  }

  /**
   * Get a skull from an offline player. If the skull is not saved in
   * memory it will be fetched from Mojang and then cached for future use.
   * @param offlinePlayer The offline player.
   * @return              ItemStack of the offline player's skull.
   */
  public static ItemStack getSkull(OfflinePlayer offlinePlayer) {
    return getSkull(offlinePlayer.getUniqueId());
  }

  /**
   * Get a skull from an online player. If the skull is not saved in
   * memory it will be fetched from Mojang and then cached for future use.
   * @param player    The online player.
   * @return          ItemStack of the online player's skull.
   */
  public static ItemStack getSkull(Player player) {
    return getSkull(player.getUniqueId());
  }

  /**
   * Get an array of player skulls from uuids.
   * @param uuids Array of uuids.
   * @return      ItemStack array of skulls.
   */
  public static ItemStack[] getSkulls(UUID[] uuids) {
    ItemStack[] itemStacks = new ItemStack[uuids.length];
    for (int i = 0; i < uuids.length; i++) {
      timeMap.put(uuids[i], System.currentTimeMillis());
      itemStacks[i] = getSkull(uuids[i]);
    }
    return itemStacks;
  }

  /**
   * Get an array of offline player skulls from offline players.
   * @param offlinePlayers    Array of offline players.
   * @return                  ItemStack array of skulls.
   */
  public static ItemStack[] getSkulls(OfflinePlayer[] offlinePlayers) {
    return getSkulls((UUID[]) Arrays.stream(offlinePlayers).map(OfflinePlayer::getUniqueId).toArray());
  }

  /**
   * Get an array of online player skulls from online players.
   * @param players   Array of online players.
   * @return          ItemStack array of skulls.
   */
  public static ItemStack[] getSkulls(Player[] players) {
    return getSkulls((UUID[]) Arrays.stream(players).map(Player::getUniqueId).toArray());
  }

  /**
   * Remove skulls from memory if they haven't been cached or accessed within the specified amount of time.
   * @param milliseconds Duration of time given in milliseconds.
   */
  public static void flush(long milliseconds) {
    for (UUID uuid : skullMap.keySet()) {
      if (System.currentTimeMillis() - timeMap.get(uuid) > milliseconds) {
        skullMap.remove(uuid);
        timeMap.remove(uuid);
      }
    }
  }

  /**
   * Remove skulls from memory that haven't been cached or accessed within a week.
   */
  public static void flushWeek() {
    flush(604800000);
  }

  /**
   * Creates a new player head item stack.
   * @return Player head.
   */
  public static ItemStack createSkull() {
    return new ItemStack(Material.PLAYER_HEAD);
  }

  /**
   * Creates a player skull item with the skin based on a player's UUID.
   *
   * @param id    The player's UUID.
   * @return      The head of the player.
   */
  public static ItemStack skullFromUuid(UUID id) {
    try {
      return itemWithUuid(createSkull(), id);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    return null;
  }

  private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
  private static final JSONParser jsonParser = new JSONParser();

  /**
   * Modifies a skull to use the skin of the player with a given uuid.
   *
   * @param item  The item to apply the name to. Must be a player skull.
   * @param id    The player's uuid.
   * @return      The head of the player.
   * @throws Exception if the player is not found
   */
  @SuppressWarnings("deprecation")
  public static ItemStack itemWithUuid(ItemStack item, UUID id) throws Exception {
    notNull(item, "item");
    notNull(id, "id");
    SkullMeta meta = (SkullMeta) item.getItemMeta();
    if (Bukkit.getOfflinePlayer(id).hasPlayedBefore())
      meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
    else {
      HttpURLConnection connection = (HttpURLConnection) new URL(PROFILE_URL + id.toString()
          .replace("-", "")).openConnection();
      JSONObject response = (JSONObject) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
      String name = (String) response.get("name");
      String cause = (String) response.get("cause");
      String errorMessage = (String) response.get("errorMessage");
      if (cause != null && cause.length() > 0)
        throw new IllegalStateException(errorMessage);
      meta.setOwner(name);
    }
    item.setItemMeta(meta);
    return item;
  }

  private static void notNull(Object o, String name) {
    if (o == null)
      throw new NullPointerException(name + " should not be null!");
  }
}