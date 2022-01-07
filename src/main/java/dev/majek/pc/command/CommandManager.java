package dev.majek.pc.command;

import cloud.commandframework.Command;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.jda.JDA4CommandManager;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.AudienceProvider;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.paper.PaperCommandManager;
import dev.majek.pc.PartyChat;
import dev.majek.pc.command.exception.CooldownException;
import dev.majek.pc.command.party.PartyTest;
import dev.majek.pc.command.party.PartyTest2;
import dev.majek.pc.data.struct.user.DiscordSender;
import dev.majek.pc.data.struct.user.MinecraftSender;
import dev.majek.pc.service.SenderService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;

public class CommandManager {

  private final PaperCommandManager<MinecraftSender> paperCommandManager;
  private JDA4CommandManager<DiscordSender> discordCommandManager;

  public CommandManager() throws Exception {
    this.paperCommandManager = new PaperCommandManager<>(
        PartyChat.core(),
        AsynchronousCommandExecutionCoordinator.<MinecraftSender>newBuilder().build(),
        commandSender -> {
          if (commandSender instanceof ConsoleCommandSender) {
            return SenderService.sender().console();
          }

          Player player = (Player) commandSender;
          return SenderService.sender().player(player);
        },
        MinecraftSender::commandSender
    );

    this.registerExceptionHandlers();

    if (this.paperCommandManager.queryCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
      this.paperCommandManager.registerBrigadier();
      final CloudBrigadierManager<?, ?> brigManager = this.paperCommandManager.brigadierManager();
      if (brigManager != null) {
        brigManager.setNativeNumberSuggestions(false);
      }
    }

    if (this.paperCommandManager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
      this.paperCommandManager.registerAsynchronousCompletions();
    }

    /*
    this.paperCommandManager.setCommandSuggestionProcessor((context, strings) -> {
      String input;

      if (context.getInputQueue().isEmpty()) {
        input = "";
      } else {
        input = context.getInputQueue().peek();
      }

      input = input.toLowerCase();
      List<String> suggestions = new LinkedList<>();

      for (String suggestion : strings) {
        suggestion = suggestion.toLowerCase();

        if (suggestion.startsWith(input)) {
          suggestions.add(suggestion);
        }
      }

      return suggestions;
    });
     */

    if (PartyChat.jda() != null) {
      this.discordCommandManager = new JDA4CommandManager<>(
          PartyChat.jda(),
          (sender) -> "/",
          DiscordSender::permission,
          CommandExecutionCoordinator.simpleCoordinator(),
          jdaSender -> SenderService.sender().discord(jdaSender),
          DiscordSender::jdaSender
      );
    }

    PartyChat.error("Finished CommandManager<init>");
  }

  public PaperCommandManager<MinecraftSender> paper() {
    return this.paperCommandManager;
  }

  public JDA4CommandManager<DiscordSender> discord() {
    return this.discordCommandManager;
  }

  public void registerCommands() {
    Set<IPartyCommand> commands = Set.of(
        new PartyTest(),
        new PartyTest2()
    );

    commands.forEach(command -> {
      if (command instanceof DiscordCommand) {
        ((DiscordCommand) command).registerDiscord();
      }

      if (!command.isDisabled()) {
        command.register();
      }
    });
  }

  public void registerPartySubCommand(@NotNull UnaryOperator<Command.Builder<MinecraftSender>> builderModifier) {
    this.paperCommandManager.command(builderModifier.apply(this.rootPartyBuilder()));
  }

  private Command.@NotNull Builder<MinecraftSender> rootPartyBuilder() {
    return this.paperCommandManager.commandBuilder("party", "p")
        /* MinecraftHelp uses the MinecraftExtrasMetaKeys.DESCRIPTION meta, this is just so we give Bukkit a description
         * for our commands in the Bukkit and EssentialsX '/help' command */
        .meta(CommandMeta.DESCRIPTION, String.format("Main /party command. '/%s help'", "party"));
  }

  private void registerExceptionHandlers() {
    new MinecraftExceptionHandler<MinecraftSender>()
        .withDefaultHandlers()
        .withDecorator(component -> Component.text()
            /*.append(Component.text("/party")
                .hoverEvent(Component.text("Click for help."))
                .clickEvent(ClickEvent.runCommand(String.format("/%s help", "party"))))
            */
            .append(component)
            .build())
        .apply(this.paperCommandManager, AudienceProvider.nativeAudience());

    final var minecraftExtrasDefaultHandler =
        Objects.requireNonNull(this.paperCommandManager.getExceptionHandler(CommandExecutionException.class));
    this.paperCommandManager.registerExceptionHandler(CommandExecutionException.class, (sender, exception) -> {
      final Throwable cause = exception.getCause();

      if (cause instanceof CooldownException) {
        sender.sendMessage(Component.text("Command on cooldown!"));
        return;
      }

      PartyChat.error(cause.getMessage());

      minecraftExtrasDefaultHandler.accept(sender, exception);
    });
  }
}
