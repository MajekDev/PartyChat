package dev.majek.pc.command;

import dev.majek.pc.PartyChat;
import dev.majek.pc.command.party.*;
import dev.majek.pc.mechanic.Mechanic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandHandler extends Mechanic {

    private final Map<String, PartyCommand> commandMap;

    public CommandHandler() {
        commandMap = new HashMap<>();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onStartup() {

        // Set aliases
        try {
            final Field bukkitCommandMap = PartyChat.getCore().getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(PartyChat.getCore().getServer());

            Command partyCommand = PartyChat.getCore().getCommand("party");
            partyCommand.unregister(commandMap);
            partyCommand.setAliases(PartyChat.getDataHandler().getConfigStringList(PartyChat.getDataHandler().commandConfig, "party.aliases"));

            Command partyChatCommand = PartyChat.getCore().getCommand("partychat");
            partyChatCommand.unregister(commandMap);
            partyChatCommand.setAliases(PartyChat.getDataHandler().getConfigStringList(PartyChat.getDataHandler().commandConfig, "partychat.aliases"));

            commandMap.register("partychat", partyCommand);
            commandMap.register("partychat", partyChatCommand);
            Map<String, Command> shit = commandMap.getKnownCommands();
            //for (String string : shit.keySet()) {
            //    PartyChat.log(string + ": " + shit.get(string).getAliases());
            //}
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        // Register /party subcommands
        registerCommands();

        PartyChat.getCore().getCommand("partychat").setExecutor(new PartyChatCommand());
        PartyChat.getCore().getCommand("partychat").setTabCompleter(new PartyChatCommand());
    }

    @SuppressWarnings("ConstantConditions")
    private void registerCommand(PartyCommand command) {
        commandMap.put(command.getName(), command);
        PartyChat.getCore().getCommand("party").setExecutor(command);
        PartyChat.getCore().getCommand("party").setTabCompleter(command);
    }

    public PartyCommand getCommand(String name) {
        PartyCommand command = commandMap.get(name);
        if (command == null)
            command = commandMap.values().stream().filter(c -> c.getAliases().contains(name)).findAny().orElse(null);
        return command;
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
        registerCommands();
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
