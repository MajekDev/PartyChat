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
package dev.majek.pc.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * Contains various unrelated utility functions.
 */
public final class Utils {
  public static final UUID UUID_00 = new UUID(0, 0);

  private static final Map<String, String> WORLD_NAME_ALIASES = new HashMap<>();

  static {
    WORLD_NAME_ALIASES.put("overworld", "world");
    WORLD_NAME_ALIASES.put("nether", "world_nether");
    WORLD_NAME_ALIASES.put("the_nether", "world_nether");
    WORLD_NAME_ALIASES.put("world_the_nether", "world_nether");
    WORLD_NAME_ALIASES.put("end", "world_the_end");
    WORLD_NAME_ALIASES.put("the_end", "world_the_end");
    WORLD_NAME_ALIASES.put("world_end", "world_the_end");
  }

  private Utils() {
  }

  /**
   * Converts certain 8-bit ASCII character codes to 6-bit codes. Characters that apply: <code>$0-9A-Z_a-z</code>.
   */
  private static final int[] IDENTIFIER_COMPRESSION = {
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 0, 0, 0, 0, 0, 0, 11, 12, 13, 14, 15,
      16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 0, 0, 0, 0, 37, 0, 38,
      39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63
  };

  /**
   * Converts 6-bit identifier character codes to Java characters.
   */
  private static final char[] IDENTIFIER_DECOMPRESSION = {
      '$', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
      'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', 'a', 'b', 'c', 'd', 'e', 'f',
      'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
  };

  /**
   * Compresses a string that matches the regex <code>[a-zA-Z0-9$_]+</code>. This compression method will fail by
   * returning null if either an invalid character is detected, or if the string is longer than 255 characters. This
   * algorithm is best suited for short identifiers, and will on average deflate the size of the object by 25%.
   * <p>
   * The format for the compressed string is as follows:
   * <ul>
   *     <li>One byte representing the length of the string.</li>
   *     <li>Sections of three bytes that contain 6-bit character codes, if there are any.</li>
   *     <li>A number of bytes equal to the length of the input string mod 4 which each contain one character code.</li>
   * </ul>
   *
   * @param string the string to compress.
   * @return the compressed byte array, or null if there was an error compressing the string.
   */
  static byte[] compressIdentifier(String string) {
    final int slen = string.length();
    if (slen == 0) // No computation needed
      return new byte[]{0};
    if (slen > 255)
      return null;

    final int loopCount = slen - (slen % 4);
    // Compute the size of the finalized byte array and initialize it
    final byte[] b = new byte[1 + ((3 * slen + (slen % 4)) / 4)];
    b[0] = (byte) slen;
    final char[] cs = string.toCharArray();
    int c1, c2, c3, c4; // The current characters we're handling
    int i = 0, k = 1; // i: first character index, k: compressed byte array index
    // Take batches of four characters, check them, and compress them into three bytes
    for (; i < loopCount; i += 4, k += 3) {
      c1 = cs[i];
      c2 = cs[i + 1];
      c3 = cs[i + 2];
      c4 = cs[i + 3];
      // Handle invalid characters
      if (((c1 < 'a' || c1 > 'z') && (c1 < 'A' || c1 > 'Z') && (c1 < '0' || c1 > '9') && c1 != '_' && c1 != '$') ||
          ((c2 < 'a' || c2 > 'z') && (c2 < 'A' || c2 > 'Z') && (c2 < '0' || c2 > '9') && c2 != '_' && c2 != '$') ||
          ((c3 < 'a' || c3 > 'z') && (c3 < 'A' || c3 > 'Z') && (c3 < '0' || c3 > '9') && c3 != '_' && c3 != '$') ||
          ((c4 < 'a' || c4 > 'z') && (c4 < 'A' || c4 > 'Z') && (c4 < '0' || c4 > '9') && c4 != '_' && c4 != '$')) {
        return null;
      }
      c2 = IDENTIFIER_COMPRESSION[c2];
      c3 = IDENTIFIER_COMPRESSION[c3];
      // Compression model (each character represents one bit):
      // 00aaaaaa 00bbbbbb 00cccccc 00dddddd -> aaaaaabb bbbbcccc ccdddddd
      b[k] = (byte) ((IDENTIFIER_COMPRESSION[c1] << 2) | ((c2 & 0x30) >>> 4));
      b[k + 1] = (byte) ((c2 << 4) | ((c3 & 0x3C) >>> 2));
      b[k + 2] = (byte) ((c3 << 6) | IDENTIFIER_COMPRESSION[c4]);
    }

    // Add the extra characters at the end (this does not affect the deflation percentage)
    for (c2 = 0; c2 < slen % 4; ++c2, ++i, ++k) {
      c1 = cs[i];
      if ((c1 < 'a' || c1 > 'z') && (c1 < 'A' || c1 > 'Z') && (c1 < '0' || c1 > '9') && c1 != '_' && c1 != '$')
        return null;
      b[k] = (byte) c1;
    }

    return b;
  }

  /**
   * Decompresses an identifier read from an input stream.
   * @param in the input stream.
   * @return the identifier.
   * @throws IOException if an I/O error occurs.
   */
  static String decompressIdentifier(InputStream in) throws IOException {
    final int length = in.read();
    if(length == 0)
      return "";
    final char[] cs = new char[length];
    final int loopCount = (3 * (length - length % 4)) / 4;
    int b1, b2, b3; // The current bytes being handled
    int i = 0, k = 1;
    for(;k < loopCount;i += 4, k += 3) {
      b1 = in.read();
      b2 = in.read();
      b3 = in.read();
      // Decompression model (each character represents one bit):
      // aaaaaabb bbbbcccc ccdddddd -> 00aaaaaa 00bbbbbb 00cccccc 00dddddd
      cs[i] = IDENTIFIER_DECOMPRESSION[(b1 >>> 2) & 0x3F];
      cs[i + 1] = IDENTIFIER_DECOMPRESSION[((b1 & 0x3) << 4) & 0x30 | (b2 >>> 4) & 0xF];
      cs[i + 2] = IDENTIFIER_DECOMPRESSION[((b2 & 0xF) << 2) & 0x3C | (b3 >>> 6) & 0x3];
      cs[i + 3] = IDENTIFIER_DECOMPRESSION[b3 & 0x3F];
    }
    // Add the extra characters at the end
    for(b1 = 0;b1 < length % 4;++ i, ++ k, ++ b1)
      cs[i] = (char)in.read();
    return new String(cs);
  }

  /**
   * Converts an {@link ItemStack} to a Json string
   * for sending with {@link net.md_5.bungee.api.chat.BaseComponent}'s.
   *
   * @param itemStack the item to convert
   * @return the Json string representation of the item
   */
  public static String convertItemStackToJson(ItemStack itemStack) {
    // ItemStack methods to get a net.minecraft.server.ItemStack object for serialization
    Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
    Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);

    // NMS Method to serialize a net.minecraft.server.ItemStack to a valid Json string
    Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
    Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
    Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

    Object nmsNbtTagCompoundObj; // This will just be an empty NBTTagCompound instance to invoke the saveNms method
    Object nmsItemStackObj; // This is the net.minecraft.server.ItemStack object received from the asNMSCopy method
    Object itemAsJsonObject; // This is the net.minecraft.server.ItemStack after being put through saveNmsItem method

    try {
      nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
      nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
      itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
    } catch (Throwable t) {
      Bukkit.getLogger().log(Level.SEVERE, "Failed to serialize itemstack to nms item", t);
      return null;
    }

    // Return a string representation of the serialized object
    return itemAsJsonObject.toString();
  }

  /**
   * Converts the given floating point number to a string and then truncates the decimal point to the given precision.
   *
   * @param d         the float point to convert to a string.
   * @param precision the number of decimal point the resulting string should have.
   * @return a string form of the given floating point number, with the decimal point truncated to the given
   * precision.
   */
  public static String doubleToString(double d, int precision) {
    String fp = Double.toString(d);
    return fp.contains(".") ? fp.substring(0, Math.min(fp.lastIndexOf('.') + precision + 1, fp.length())) +
        (fp.contains("E") ? fp.substring(fp.lastIndexOf('E')) : "") : fp;
  }

  /**
   * Constrains the given number between the given minimum and maximum value. If the given number n is outside the
   * given range then the closest bound is returned.
   *
   * @param n   the number to constrain.
   * @param min the minimum bound.
   * @param max the maximum bound.
   * @return the constrained number.
   * @throws IllegalArgumentException if the given maximum bound is less than the given minimum bound.
   */
  public static int constrain(int n, int min, int max) {
    if (max < min)
      throw new IllegalArgumentException("The maximum bound cannot be less than the minimum bound.");
    return Math.max(min, Math.min(n, max));
  }

  /**
   * Attempts to find the proper world name for the given alias. No pattern is necessarily used here, rather common
   * names for the various vanilla worlds are mapped to the correct names. If no world name could be found for the
   * given alias, the given alias is returned.
   *
   * @param alias the alias to map.
   * @return the correct world name for the given alias, or the given alias if no world name could be found.
   */
  public static String getWorldName(String alias) {
    String formattedAlias = alias.toLowerCase();
    if (WORLD_NAME_ALIASES.containsValue(formattedAlias))
      return formattedAlias;

    formattedAlias = formattedAlias.replaceAll("[\\-\\s]", "_");
    return WORLD_NAME_ALIASES.getOrDefault(formattedAlias, alias);
  }

  /**
   * Converts the given enumeration element's name (which should be all capitalized with underscores) and replaces the
   * underscores with hyphens and converts the string to lower case.
   *
   * @param e the enumeration element.
   * @return the formatted name of the given element as defined above.
   */
  public static String formattedName(Enum e) {
    return e.name().replaceAll("_", "-").toLowerCase();
  }

  /**
   * Capitalizes each word in the provided string. A word is defined as a cluster of characters separated on either
   * side by spaces or the end or beginning of a string.
   *
   * @param x the string to capitalize.
   * @return the capitalized string.
   */
  public static String capitalize(String x) {
    if (x == null || x.isEmpty())
      return x;

    String[] split = x.split(" ");
    for (int i = 0; i < split.length; ++i) {
      if (!split[i].isEmpty())
        split[i] = Character.toUpperCase(split[i].charAt(0)) + split[i].substring(1).toLowerCase();
    }

    return String.join(" ", split);
  }

  /**
   * Returns the given index if it is not equal to negative one, otherwise it returns the default value.
   *
   * @param index the index.
   * @param def   the default value.
   * @return the given index if it is not equal to negative one, otherwise it returns the default value.
   */
  public static int indexOfDefault(int index, int def) {
    return index == -1 ? def : index;
  }

  /**
   * Returns the value returned by the given function with the given input, or null if the function throws an
   * exception.
   *
   * @param valueOf the value-of function.
   * @param input   the input.
   * @param <T>     the return type.
   * @return the value returned by the given function with the given input, or null if the function throws an
   * exception.
   */
  public static <T> T safeValueOf(Function<String, T> valueOf, String input) {
    try {
      return valueOf.apply(input);
    } catch (Throwable t) {
      return null;
    }
  }

  /**
   * If the given block would damage the player (immediately or otherwise)
   * when placed at their foot level or eye level
   *
   * @param block the block to check
   * @return if a player can safely stand inside this block
   */
  private static boolean doesDamage(Block block) {
    return block.getType().isSolid() || block.isLiquid() ||
        block.getType() == Material.FIRE ||
        block.getType() == Material.CACTUS ||
        block.getType() == Material.SWEET_BERRY_BUSH ||
        block.getType() == Material.LAVA ||
        block.getType() == Material.WITHER_ROSE;
  }

  /**
   * If a player would be teleported to this location, confirm they cannot:
   * take damage from the block at    the location (body)
   * take damage from the block above the location (head)
   *
   * @param location the location to check
   * @return if a player would be damaged by any of the blocks at this location upon tp ignoring the block below
   */
  public static boolean isSafe(Location location) {
    return !(
        doesDamage(location.clone().add(0, 1, 0).getBlock()) ||
            doesDamage(location.clone().add(0, 2, 0).getBlock())
    );
  }

  /**
   * If a player would be teleported to the location of this block, confirm they cannot:
   * fall through or take damage
   * unless the block is water
   *
   * @param block the block to check
   * @return if the block can be stood on without the block damaging the player
   */
  public static boolean canStand(Block block) {
    return !(
        block.isPassable() ||
            block.getType() == Material.MAGMA_BLOCK ||
            block.getType() == Material.CACTUS
    ) || block.getType() == Material.WATER;
  }

  /**
   * Search for a safe location for a player to stand
   * We search a column at the x z of the origin
   * The algorithm treats this column as being sorted with ground at the bottom and sky above it
   * Because of this we can make assumptions that the ground will always have:
   * 2 non damaging blocks above (air|vines|etc) and
   * 1 block the player can stand on below (non passable and non damaging)
   * Because bottom and top specify search range for Y they should satisfy these ranges:
   * 0 < bottom < top < 256   for the overworld
   * 0 < bottom < top < 124   for the nether (if nether roof is not enabled)
   *
   * @param safe   the location to check for safety
   * @param bottom where to set the bottom of the "binary search", must comply with       0 < bottom < top
   * @param top    where to set the top    of the "binary search", must comply with  bottom < top    < 256
   * @return a safe location if found, else null
   */
  public static Location findSafe(Location safe, int bottom, int top) {
    World world = safe.getWorld();
    if (world == null)
      world = Bukkit.getWorlds().get(0);

    final int border = (int)world.getWorldBorder().getSize() - 1 >> 1;
    safe.setX(constrain(safe.getBlockX() - world.getWorldBorder().getCenter().getBlockX(), -border, border) +
        world.getWorldBorder().getCenter().getBlockX());
    safe.setZ(constrain(safe.getBlockZ() - world.getWorldBorder().getCenter().getBlockZ(), -border, border) +
        world.getWorldBorder().getCenter().getBlockZ());


    for (int i = safe.getBlockY(), c = 0; ; i += (c = (c & 1) == 0 ? c + 1 : ~c)) {
      safe.setY(i);
      if (canStand(safe.getBlock()) && isSafe(safe.clone()))
        return safe.add(0.5, 1, 0.5);

      if (bottom > i) {
        for (i += c + 1; i <= top; ++i) {
          safe.setY(i);
          if (canStand(safe.getBlock()) && isSafe(safe.clone()))
            return safe.add(0.5, 1, 0.5);
        }
        return null;
      }

      if (i > top) {
        for (i += ~c; bottom <= i; --i) {
          safe.setY(i);
          if (canStand(safe.getBlock()) && isSafe(safe.clone()))
            return safe.add(0.5, 1, 0.5);
        }
        return null;
      }
    }
  }

  /**
   * Search the nearby area
   *  for a location a player can safely stand before teleporting them
   * Set the location to be in the center of the block ready for teleportation
   * Calculate reasonable bottom and top values
   *
   * @param origin location to check for safety
   * @return a safe location near the origin
   */
  public static Location findSafeNear(Location origin, int bottom, int top) {
    Location safe;

    if ((safe = findSafe(origin.clone(), bottom, top)) != null)
      return safe;

    for (int i = 1, j; ; ++i) {
      for (j = i; j >= 1; --j)
        if ((safe = findSafe(origin.clone().add(   -j, 0, i - j), bottom, top)) != null)
          return safe;

      for (j = i; j >= 1; --j)
        if ((safe = findSafe(origin.clone().add(    j, 0, j - i), bottom, top)) != null)
          return safe;

      for (j = i; j >= 1; --j)
        if ((safe = findSafe(origin.clone().add(j - i, 0,    -j), bottom, top)) != null)
          return safe;

      for (j = i; j >= 1; --j)
        if ((safe = findSafe(origin.clone().add(i - j, 0,     j), bottom, top)) != null)
          return safe;
    }
  }

  /**
   * Search for a safe location using findSafe starting at and including origin in steps of dx dz where
   * these values are multiplied by 16 when there is Liquid at y62 as this usually indicates being in an ocean and
   * oceans provide a lower probability for findSafe to return a non null location.
   *
   * @param origin the location to start searching from
   * @param dx     the x offset we tend towards
   * @param dz     the x offset we tend towards
   * @return the first safe location we find
   */
  public static Location walk(Location origin, int dx, int dz) {
    Location copy = origin.clone();

    World world = copy.getWorld();
    if (world == null)
      world = Bukkit.getWorlds().get(0);

    final int bottom = 1,
        top    = world.getName().equals("world_nether") ? 124 : 255,
        xMin   = world.getWorldBorder().getCenter().getBlockX() - ((int) world.getWorldBorder().getSize() >> 1),
        xMax   = world.getWorldBorder().getCenter().getBlockX() + ((int) world.getWorldBorder().getSize() >> 1),
        zMin   = world.getWorldBorder().getCenter().getBlockZ() - ((int) world.getWorldBorder().getSize() >> 1),
        zMax   = world.getWorldBorder().getCenter().getBlockZ() + ((int) world.getWorldBorder().getSize() >> 1);


    for (Location safe = findSafe(copy, bottom, top); ; safe = findSafe(copy, bottom, top)) {
      if (safe != null)
        return safe;
      copy.add(dx, 0, dz);
      if (xMin >= copy.getX() || copy.getX() >= xMax)
        dx = -dx;
      if (zMin >= copy.getZ() || copy.getZ() >= zMax)
        dx = -dx;
    }
  }

  public static String serializeMembers(List<UUID> members) {
    StringBuilder sb = new StringBuilder();
    for (UUID member : members) {
      sb.append(member).append(",");
    }
    return sb.toString();
  }

  public static List<UUID> deserializeMembers(String members) {
    String[] str = members.split(",");
    List<String> memberUUIDs = new ArrayList<>(Arrays.asList(str));
    List<UUID> toReturn = new ArrayList<>();
    for (String uuid : memberUUIDs) {
      toReturn.add(UUID.fromString(uuid));
    }
    return toReturn;
  }
}