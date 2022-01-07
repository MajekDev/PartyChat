package dev.majek.pc.command.party;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import dev.majek.pc.PartyChat;
import dev.majek.pc.command.IPartyCommand;
import dev.majek.pc.data.struct.user.MinecraftSender;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class PartyTest2 implements IPartyCommand {

  @Override
  public @NotNull String name() {
    return "rename";
  }

  @Override
  public void register() {
    PartyChat.commandManager().registerPartySubCommand(builder ->
        builder.literal("rename")
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, Component.text("Hover Rename?"))
            .permission("partychat.rename")
            .handler(this::execute)
    );
    PartyChat.error("Registered Rename!");
  }

  @Override
  public void execute(final @NotNull CommandContext<MinecraftSender> context) {
    context.getSender().sendMessage(Component.text("/party rename!"));
  }
}
