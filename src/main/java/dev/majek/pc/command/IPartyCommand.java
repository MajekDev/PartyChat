package dev.majek.pc.command;

import cloud.commandframework.context.CommandContext;
import dev.majek.pc.PartyChat;
import dev.majek.pc.data.struct.user.MinecraftSender;
import org.jetbrains.annotations.NotNull;

public interface IPartyCommand {

  @NotNull String name();

  void register();

  void execute(final @NotNull CommandContext<MinecraftSender> context);

  default boolean isDisabled() {
    return PartyChat.dataHandler().commandConfig.getBoolean("party-subcommands." + name() + ".disabled");
  }
}
