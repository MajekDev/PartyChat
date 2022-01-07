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
package dev.majek.pc.message;

import dev.majek.pc.PartyChat;
import dev.majek.pc.data.DataHandler;
import dev.majek.pc.data.object.User;
import dev.majek.pc.hooks.PAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Handles all plugin messages.
 */
public class MessageHandler {

  /**
   * This will apply PlaceholderAPI placeholders and then parse the message to a Component using ChatParser.
   *
   * @param sender The person to send the message to.
   * @param message The message to send the person.
   */
  @SuppressWarnings("deprecation")
  public void sendFormattedMessage(CommandSender sender, String message) {
    if (PartyChat.hasPapi && (sender instanceof Player))
      message = PAPI.applyPlaceholders((Player) sender, message);
    ChatParser parser = new ChatParser().parse(ChatUtils.applyColorCodes(message));
    if (PartyChat.dataHandler().messageType == null) {
      try {
        sender.sendMessage(parser.getAsComponent());
        PartyChat.dataHandler().messageType = DataHandler.MessageType.COMPONENT;
        PartyChat.dataHandler().logToFile("Set message type to Component", "INFO");
      } catch (NoSuchMethodError error) {
        try {
          sender.spigot().sendMessage(parser.getAsBaseComponent());
          PartyChat.dataHandler().messageType = DataHandler.MessageType.BASECOMPONENT;
          PartyChat.dataHandler().logToFile("Set message type to BaseComponent", "INFO");
        } catch (NoSuchMethodError error1) {
          sender.sendMessage(parser.getAsRawString());
          PartyChat.dataHandler().messageType = DataHandler.MessageType.RAW;
          PartyChat.dataHandler().logToFile("Set message type to raw", "INFO");
        }
      }
    } else {
      switch (PartyChat.dataHandler().messageType) {
        case COMPONENT:
          sender.sendMessage(parser.getAsComponent());
          break;
        case BASECOMPONENT:
          sender.spigot().sendMessage(parser.getAsBaseComponent());
          break;
        case RAW:
        default:
          sender.sendMessage(parser.getAsRawString());
      }
    }
  }

  /**
   * Send a player a message from the messages config file (this will be set to the correct language).
   * Replace the %prefix% placeholder with the defined prefix in the file.
   *
   * @param sender The player/console to send the message to.
   * @param path The path to get the message from in the file.
   */
  public void sendMessage(CommandSender sender, String path) {
    String prefix = PartyChat.dataHandler().getConfigString(PartyChat.dataHandler().messages, "prefix");
    String message = PartyChat.dataHandler().getConfigString(PartyChat.dataHandler().messages, path);
    message = message.replace("%prefix%", prefix);
    sendFormattedMessage(sender, message);
  }

  /**
   * Send a player a message from the messages config file (this will be set to the correct language)
   * while replacing a target with a defined string.
   *
   * @param sender The player/console to send the message to.
   * @param path The path to get the message from in the file.
   * @param target The target string to be replaced.
   * @param replacement The replacement for the target string.
   */
  public void sendMessageWithReplacement(CommandSender sender, String path, String target, String replacement) {
    String prefix = PartyChat.dataHandler().getConfigString(PartyChat.dataHandler().messages, "prefix");
    String message = PartyChat.dataHandler().getConfigString(PartyChat.dataHandler().messages, path);
    message = message.replace(target, replacement);
    message = message.replace("%prefix%", prefix);
    sendFormattedMessage(sender, message);
  }

  /**
   * Send a message to a {@link CommandSender} with multiple replacements.
   *
   * @param sender The person to send the message to.
   * @param path The path to the message in the lang file.
   * @param replacements The map of replacements.
   */
  public void sendMessageWithReplacements(CommandSender sender, String path, Map<String, String> replacements) {
    String prefix = PartyChat.dataHandler().getConfigString(PartyChat.dataHandler().messages, "prefix");
    String message = PartyChat.dataHandler().getConfigString(PartyChat.dataHandler().messages, path);
    message = message.replace("%prefix%", prefix);
    for (String key : replacements.keySet())
      message = message.replace(key, replacements.get(key));
    sendFormattedMessage(sender, message);
  }

  /**
   * Send a message with replacements and message to send to PartyChat.
   *
   * @param sender Player/console to send the message to.
   * @param path Path to get from messages config file.
   * @param target1 First placeholder to find and replace.
   * @param replacement1 Replacement for first placeholder.
   * @param target2 Second placeholder to find and replace.
   * @param replacement2 Replacement for second placeholder.
   * @param toAdd Message to add to the end of everything.
   */
  public void sendMessageWithEverything(CommandSender sender, String path, String target1, String replacement1,
                                               String target2, String replacement2, String toAdd) {
    String prefix = PartyChat.dataHandler().getConfigString(PartyChat.dataHandler().messages, "prefix");
    String message = PartyChat.dataHandler().getConfigString(PartyChat.dataHandler().messages, path);
    message = message.replace(target1, replacement1).replace(target2, replacement2) + toAdd;
    message = message.replace("%prefix%", prefix);
    sendFormattedMessage(sender, message);
  }

  public void sendMessage(User user, String path) {
    if (user.isOnline())
      sendMessage(user.getPlayer(), path);
  }
}