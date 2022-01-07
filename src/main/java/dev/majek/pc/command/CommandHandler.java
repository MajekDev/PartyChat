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
package dev.majek.pc.command;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.party.*;
import dev.majek.pc.event.Mechanic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles command registration, fetching, and managing aliases.
 */
public class CommandHandler extends Mechanic {

  private final Map<String, PartyCommand> commandMap;

  public CommandHandler() {
    commandMap = new HashMap<>();
  }

  /*
  @Override
  @SuppressWarnings("ConstantConditions")
  public void onStartup() {

    // Set aliases
    if (PartyChat.dataHandler().minecraftVersion > 12) {
      try {
        final Field bukkitCommandMap = PartyChat.core().getServer().getClass().getDeclaredField("commandMap");

        bukkitCommandMap.setAccessible(true);
        CommandMap commandMap = (CommandMap) bukkitCommandMap.get(PartyChat.core().getServer());

        Command partyCommand = PartyChat.core().getCommand("party");
        partyCommand.unregister(commandMap);
        partyCommand.setAliases(PartyChat.dataHandler().getConfigStringList(PartyChat.dataHandler()
            .commandConfig, "party.aliases"));

        Command partyChatCommand = PartyChat.core().getCommand("partychat");
        partyChatCommand.unregister(commandMap);
        partyChatCommand.setAliases(PartyChat.dataHandler().getConfigStringList(PartyChat.dataHandler()
            .commandConfig, "partychat.aliases"));

        commandMap.register("partychat", partyCommand);
        commandMap.register("partychat", partyChatCommand);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        e.printStackTrace();
      }
    }

    // Register /party subcommands
    //registerCommands();

    PartyChat.core().getCommand("partychat").setExecutor(new PartyChatCommand());
    PartyChat.core().getCommand("partychat").setTabCompleter(new PartyChatCommand());
  }

   */

  @SuppressWarnings("ConstantConditions")
  private void registerCommand(PartyCommand command) {
    commandMap.put(command.getName(), command);
    PartyChat.core().getCommand("party").setExecutor(command);
    PartyChat.core().getCommand("party").setTabCompleter(command);
  }

  public PartyCommand getCommand(String name) {
    PartyCommand command = commandMap.get(name);
    if (command == null)
      command = commandMap.values().stream().filter(c -> c.getAliases().contains(name)).findAny().orElse(null);
    return command;
  }

  @SuppressWarnings("unchecked")
  public <T extends PartyCommand> T getCommand(Class<T> clazz) {
    return (T) commandMap.values().stream().filter(c -> clazz.equals(c.getClass())).findAny().orElse(null);
  }

  public List<PartyCommand> getCommands() {
    return new ArrayList<>(commandMap.values());
  }

  public void registerCommands() {
    registerCommand(new PartyAccept());
    registerCommand(new PartyAdd());
    registerCommand(new PartyCreate());
    registerCommand(new PartyDeny());
    registerCommand(new PartyDisband());
    registerCommand(new PartyHelp());
    registerCommand(new PartyInfo());
    registerCommand(new PartyJoin());
    registerCommand(new PartyLeave());
    registerCommand(new PartyPromote());
    registerCommand(new PartyRemove());
    registerCommand(new PartyRename());
    registerCommand(new PartySummon());
    registerCommand(new PartyToggle());
    registerCommand(new PartyVersion());
  }

  public void reload() {
    commandMap.clear();
    //registerCommands();
  }

  public List<String> getAllCommandsAndAliases() {
    List<String> all = getCommands().stream().filter(PartyCommand::isEnabled).filter(PartyCommand::anyoneCanUse)
        .map(PartyCommand::getName).collect(Collectors.toList());
    getCommands().stream().filter(PartyCommand::isEnabled).filter(PartyCommand::anyoneCanUse)
        .forEach(partyCommand -> all.addAll(partyCommand.getAliases()));
    return all;
  }

  public List<String> getLeaderCommandsAndAliases() {
    List<String> all = getCommands().stream().filter(PartyCommand::isEnabled).map(PartyCommand::getName)
        .collect(Collectors.toList());
    getCommands().stream().filter(PartyCommand::isEnabled).forEach(partyCommand ->
        all.addAll(partyCommand.getAliases())
    );
    return all;
  }
}