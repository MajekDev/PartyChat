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
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Useful utility for accessing net.minecraft.server or org.bukkit.craftbukkit classes/methods/fields.
 */
public class ReflectionUtil {

  /*
   * The server version string to location NMS & OBC classes
   */
  private static String versionString;

  /*
   * Cache of NMS classes that we've searched for
   */
  private static final Map<String, Class<?>> loadedNMSClasses = new HashMap<>();

  /*
   * Cache of OBS classes that we've searched for
   */
  private static final Map<String, Class<?>> loadedOBCClasses = new HashMap<>();

  /*
   * Cache of methods that we've found in particular classes
   */
  private static final Map<Class<?>, Map<String, Method>> loadedMethods = new HashMap<>();

  /*
   * Cache of fields that we've found in particular classes
   */
  private static final Map<Class<?>, Map<String, Field>> loadedFields = new HashMap<>();

  /**
   * Gets the version string for NMS and OBC class paths
   *
   * @return The version string of OBC and NMS packages
   */
  public static String getVersion() {
    if (versionString == null) {
      String name = Bukkit.getServer().getClass().getPackage().getName();
      versionString = name.substring(name.lastIndexOf('.') + 1) + ".";
    }

    return versionString;
  }

  /**
   * Get an NMS Class
   *
   * @param nmsClassName The name of the class
   * @return The class
   */
  public static Class<?> getNMSClass(String nmsClassName) {
    if (loadedNMSClasses.containsKey(nmsClassName)) {
      return loadedNMSClasses.get(nmsClassName);
    }

    String clazzName = "net.minecraft.server." + getVersion() + nmsClassName;
    Class<?> clazz;

    try {
      clazz = Class.forName(clazzName);
    } catch (Throwable t) {
      t.printStackTrace();
      return loadedNMSClasses.put(nmsClassName, null);
    }

    loadedNMSClasses.put(nmsClassName, clazz);
    return clazz;
  }

  /**
   * Get a class from the org.bukkit.craftbukkit package
   *
   * @param obcClassName the path to the class
   * @return the found class at the specified path
   */
  public synchronized static Class<?> getOBCClass(String obcClassName) {
    if (loadedOBCClasses.containsKey(obcClassName)) {
      return loadedOBCClasses.get(obcClassName);
    }

    String clazzName = "org.bukkit.craftbukkit." + getVersion() + obcClassName;
    Class<?> clazz;

    try {
      clazz = Class.forName(clazzName);
    } catch (Throwable t) {
      t.printStackTrace();
      loadedOBCClasses.put(obcClassName, null);
      return null;
    }

    loadedOBCClasses.put(obcClassName, clazz);
    return clazz;
  }

  /**
   * Get a Bukkit {@link Player} players NMS playerConnection object
   *
   * @param player The player
   * @return The players connection
   */
  public static Object getConnection(Player player) {
    Method getHandleMethod = getMethod(player.getClass(), "getHandle");

    if (getHandleMethod != null) {
      try {
        Object nmsPlayer = getHandleMethod.invoke(player);
        Field playerConField = getField(nmsPlayer.getClass(), "playerConnection");
        return playerConField.get(nmsPlayer);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return null;
  }

  /**
   * Get a classes constructor
   *
   * @param clazz  The constructor class
   * @param params The parameters in the constructor
   * @return The constructor object
   */
  public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... params) {
    try {
      return clazz.getConstructor(params);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  /**
   * Get a method from a class that has the specific paramaters
   *
   * @param clazz      The class we are searching
   * @param methodName The name of the method
   * @param params     Any parameters that the method has
   * @return The method with appropriate paramaters
   */
  public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
    if (!loadedMethods.containsKey(clazz)) {
      loadedMethods.put(clazz, new HashMap<>());
    }

    Map<String, Method> methods = loadedMethods.get(clazz);

    if (methods.containsKey(methodName)) {
      return methods.get(methodName);
    }

    try {
      Method method = clazz.getMethod(methodName, params);
      methods.put(methodName, method);
      loadedMethods.put(clazz, methods);
      return method;
    } catch (Exception e) {
      e.printStackTrace();
      methods.put(methodName, null);
      loadedMethods.put(clazz, methods);
      return null;
    }
  }

  /**
   * Get a field with a particular name from a class
   *
   * @param clazz     The class
   * @param fieldName The name of the field
   * @return The field object
   */
  public static Field getField(Class<?> clazz, String fieldName) {
    if (!loadedFields.containsKey(clazz)) {
      loadedFields.put(clazz, new HashMap<>());
    }

    Map<String, Field> fields = loadedFields.get(clazz);

    if (fields.containsKey(fieldName)) {
      return fields.get(fieldName);
    }

    try {
      Field field = clazz.getField(fieldName);
      fields.put(fieldName, field);
      loadedFields.put(clazz, fields);
      return field;
    } catch (Exception e) {
      e.printStackTrace();
      fields.put(fieldName, null);
      loadedFields.put(clazz, fields);
      return null;
    }
  }
}